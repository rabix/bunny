package org.rabix.bindings.cwl.expression;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLRuntime;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLInlineJavascriptRequirement;
import org.rabix.bindings.cwl.expression.javascript.CWLExpressionJavascriptResolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class CWLExpressionResolver {

  public static String KEY_EXPRESSION_VALUE = "script";
  public static String KEY_EXPRESSION_LANGUAGE = "engine";

  private static String segSymbol = "\\w+";
  private static String segSingle = "\\['([^']|\\\\')+'\\]";
  private static String segDouble = "\\[\"([^\"]|\\\\\")+\"\\]";
  private static String segIndex = "\\[[0-9]+\\]";

  private static String segments = String.format("(.%s|%s|%s|%s)", segSymbol, segSingle, segDouble, segIndex);

  private static String paramRe = String.format("\\$\\((%s)%s*\\)", segSymbol, segments);

  private static Pattern segPattern = Pattern.compile(segments);
  private static Pattern pattern = Pattern.compile(paramRe);

  public static final ObjectMapper sortMapper = new ObjectMapper();

  static {
    sortMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
  }

  @SuppressWarnings({ "unchecked" })
  public static <T> T resolve(final Object expression, final CWLJob job, final Object self) throws CWLExpressionException {
    if (expression == null) {
      return null;
    }
    if (isExpressionObject(expression)) {
      String script = (String) ((Map<?, ?>) expression).get(KEY_EXPRESSION_VALUE);
      List<String> expressionLibs = Collections.<String>emptyList();
      CWLInlineJavascriptRequirement inlineJavascriptRequirement = job.getApp().getInlineJavascriptRequirement();
      if (inlineJavascriptRequirement != null) {
        expressionLibs = inlineJavascriptRequirement.getExpressionLib();
      }
      return (T) stripDollarSign(CWLExpressionJavascriptResolver.evaluate(job.getInputs(), self, script, job.getRuntime(), expressionLibs));
    }
    if (expression instanceof String) {
      if (job.isInlineJavascriptEnabled()) {
        List<String> expressionLibs = Collections.<String>emptyList();
        CWLInlineJavascriptRequirement inlineJavascriptRequirement = job.getApp().getInlineJavascriptRequirement();
        if (inlineJavascriptRequirement != null) {
          expressionLibs = inlineJavascriptRequirement.getExpressionLib();
        }
        return (T) stripDollarSign(process((String) expression, job.getInputs(), self, job.getRuntime(), expressionLibs));
      } else {
        Map<String, Object> vars = new HashMap<>();
        vars.put("inputs", job.getInputs());
        vars.put("self", self);

        CWLRuntime runtime = job.getRuntime();
        if (runtime != null) {
          vars.put("runtime", runtime.toMap());
        }
        return (T) stripDollarSign(paramInterpolate((String) expression, vars, true));
      }
    }
    return (T) expression;
  }

  private static Object stripDollarSign(Object value) {
    if (value instanceof String) {
      return ((String) value).replaceAll(Matcher.quoteReplacement("\\$"), "\\$");
    }
    return value;
  }

  public static boolean isExpressionObject(Object expression) {
    return expression instanceof Map<?,?>  && ((Map<?,?>) expression).containsKey(KEY_EXPRESSION_VALUE)  && ((Map<?,?>) expression).containsKey(KEY_EXPRESSION_LANGUAGE);
  }

  private static Object nextSegment(String remaining, Object vars) throws CWLExpressionException {
    if (vars == null) {
      return null;
    }
    if (!StringUtils.isEmpty(remaining)) {
      Matcher m = segPattern.matcher(remaining);
      if (m.find()) {
        if (m.group(0).startsWith(".")) {
          if(m.group(0).equals(".length") && vars instanceof List){
            return ((List<?>) vars).size();
          }
          return nextSegment(remaining.substring(m.end(0)), ((Map<?, ?>) vars).get(m.group(0).substring(1)));
        } else if (m.group(0).charAt(1) == '\"' || m.group(0).charAt(1) == '\'') {
          Character start = m.group(0).charAt(1);
          String key = m.group(0).substring(2, m.group(0).lastIndexOf(start));
          key = key.replace("\\'", "'");
          key = key.replace("\\\"", "\"");
          return nextSegment(remaining.substring(m.end(0)), ((Map<?, ?>) vars).get(key));
        } else {
          String key = m.group(0).substring(1, m.group(0).length());
          Integer keyInt = Integer.parseInt(key);

          Object remainingVars = null;
          if (vars instanceof List<?>) {
            if (((List<?>) vars).size() <= keyInt) {
              throw new CWLExpressionException("Could not get value from " + vars + " at position " + keyInt);
            }
            remainingVars = ((List<?>) vars).get(keyInt);
          } else if (vars instanceof Map<?,?>) {
            remainingVars = ((Map<?,?>) vars).get(keyInt);
          }
          return nextSegment(remaining.substring(m.end(0)), remainingVars);
        }
      }
    }
    return vars;
  }

  private static Object paramInterpolate(String ex, Map<String, Object> obj, boolean strip) throws CWLExpressionException {
    Matcher m = pattern.matcher(ex);
    if (m.find()) {
      Object leaf = nextSegment(m.group(0).substring(m.end(1) - m.start(0), m.group(0).length() - 1), obj.get(m.group(1)));
      if (strip && ex.trim().length() == m.group(0).length()) {
        return leaf;
      } else {
        try {
          String leafStr = sortMapper.writeValueAsString(leaf);
          if (leafStr.startsWith("\"")) {
            leafStr = leafStr.substring(1, leafStr.length() - 1);
          }
          return ex.substring(0, m.start(0)) + leafStr + paramInterpolate(ex.substring(m.end(0)), obj, false);
        } catch (JsonProcessingException e) {
          throw new CWLExpressionException("Failed to serialize " + leaf + " to JSON.", e);
        }
      }
    }
    return ex;
  }

  private static boolean isEscaped(char[] chars, int i) {
    if (i == 0)
      return false;
    return chars[i - 1] == '\\' && !isEscaped(chars, i - 1);
  }
  
  private static boolean startsExpression(char[] chars, int i) {
    return chars[i] == '$' && !isEscaped(chars, i) && i != chars.length && (chars[i + 1] == '{' || chars[i + 1] == '(');
  }

  private static Object process(String value, Object inputs, Object self, CWLRuntime runtime, List<String> engineConfigs) throws CWLExpressionException {
    char[] chars = value.trim().toCharArray();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (startsExpression(chars, i)) {
        String expression = new Seeker(chars).extractExpression(i + 1);
        if (expression == null)
          throw new CWLExpressionException("Expression left open: " + value.substring(i));

        Object resolved = CWLExpressionJavascriptResolver.evaluate(inputs, self, expression.toString(), runtime, engineConfigs);
        if (expression.length() == chars.length - 1) {
          return resolved;
        }
        i = i + expression.length();
        sb.append(resolved == null ? "null" : resolved.toString());
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static class Seeker {
    
    private StringBuilder sb;
    private char[] chars;

    public Seeker(char[] chars) {
      this.sb = new StringBuilder();
      this.chars = chars;
    }
    private static char close(char c) {
      if (c == '{')
        return '}';
      if (c == '(')
        return ')';
      return 0;
    }
    private String extractExpression(int start) {
      char open = chars[start];
      char close = close(open);
      int opened = 0;
      int i = start;
      while (i < chars.length) {
        char c = chars[i];
        sb.append(c);
        if (c == open && !isEscaped(chars, i)) {
          opened++;
        } else if (c == close && !isEscaped(chars, i)) {
          opened--;
        }
        if (opened == 0) {
          return sb.toString();
        }
        i = skipStringycontent(i);
        i++;
      }
      return null;
    }

    private int skipStringycontent(int start) {
      char c = chars[start];
      if (isEscaped(chars, start))
        return start;
      if (c == '/') {
        if (chars[start + 1] == '/')
          return skipUntil(start, '\n');
        if (chars[start + 1] == '*')
          return skipFullComments(start);
        return start;
      }
      if(c == '\'' || c == '\"')
        return skipUntil(start, c);
      return start;
    }

    private int skipFullComments(int start) {
      int i = start;
      do {
        i = skipUntil(i, '/');
      } while (chars[i - 1] != '*');
      return i;
    }

    private int skipUntil(int start, char goal) {
      int i = start + 1;
      while (i < chars.length) {
        char c = chars[i];
        sb.append(c);
        if (goal == c && !isEscaped(chars, i)) {
          return i;
        }
        i++;
      }
      return i;
    }
  }
}