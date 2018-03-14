package org.rabix.bindings.sb.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.bean.resource.requirement.SBExpressionEngineRequirement;
import org.rabix.bindings.sb.expression.javascript.SBExpressionJavascriptResolver;
import org.rabix.bindings.sb.expression.jsonpointer.SBExpressionJSPointerResolver;

public class SBExpressionResolver {

  public static String KEY_REQUIREMENT_ID = "id";
  public static String KEY_EXPRESSION_CONFIG = "engineConfig";

  public static final String JSON_POINTER_ENGINE = "cwl:JsonPointer";

  public static <T> T evaluate(final String expression) throws SBExpressionException {
    return evaluate(expression, null, null, null);
  }

  public static <T> T evaluate(final String expression, boolean includeTemplates) throws SBExpressionException {
    return evaluate(expression, null, null, includeTemplates);
  }

  public static <T> T evaluate(final String expression, final SBJob context, final Object self)
      throws SBExpressionException {
    return evaluate(expression, context, self, false);
  }

  public static <T> T evaluate(final String expression, final SBJob context, final Object self, boolean includeTemplates)
      throws SBExpressionException {
    return evaluate(expression, context, self, includeTemplates, null);
  }

  public static <T> T evaluate(final String expression, final SBJob context, final Object self, String language)
      throws SBExpressionException {
    return evaluate(expression, context, self, false, language);
  }

  @SuppressWarnings({ "unchecked" })
  public static <T> T evaluate(final String expression, final SBJob context, final Object self, boolean includeTemplates,
      String language) throws SBExpressionException {
    if (language == null) {
      language = SBExpressionLanguage.JAVASCRIPT.getDefault(); // assume it's JavaScript
    }

    Object transformedContext = transformContext(context, language);
    SBExpressionLanguage expressionLanguage = SBExpressionLanguage.convert(language);
    switch (expressionLanguage) {
    case JSON_POINTER:
      return (T) stripDollarSign(SBExpressionJSPointerResolver.evaluate(transformedContext, self, expression));
    default:
      List<String> engineConfigs = fetchEngineConfigs(context, language);
      return (T) stripDollarSign(SBExpressionJavascriptResolver.evaluate(transformedContext, self, expression, engineConfigs, includeTemplates));
    }
  }

  private static Object stripDollarSign(Object value) {
    if (value instanceof String) {
      return ((String) value).replaceAll(Matcher.quoteReplacement("\\$"), "\\$");
    }
    return value;
  }

  /**
   * By reference CWL implementation, context is equals to 'inputs' section from the Job
   * Out implementation uses whole Job as a context
   */
  private static Object transformContext(SBJob context, String language) {
    if (context == null) {
      return null;
    }

    if (!language.equals("#cwl-js-engine") && !language.equals("cwl-js-engine")) {
      return context.getInputs();
    }
    return context;
  }

  /**
   * Fetch engine configuration from requirements
   */
  private static List<String> fetchEngineConfigs(SBJob context, String language) {
    if (context == null) {
      return null;
    }
    List<String> result = new ArrayList<>();
    List<SBExpressionEngineRequirement> requirements = context.getApp().getExpressionEngineRequirements();
    if (requirements != null) {
      for (SBExpressionEngineRequirement requirement : requirements) {
        List<String> engineConfiguration = requirement.getEngineConfigs(language);
        if (engineConfiguration != null) {
          result.addAll(engineConfiguration);
        }
      }
    }
    return result;
  }

}