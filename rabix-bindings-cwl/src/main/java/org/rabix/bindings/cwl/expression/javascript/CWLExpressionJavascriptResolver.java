package org.rabix.bindings.cwl.expression.javascript;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.rabix.bindings.cwl.bean.CWLRuntime;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

import com.fasterxml.jackson.databind.JsonNode;

public class CWLExpressionJavascriptResolver {

  public final static int TIMEOUT_IN_SECONDS = 5;

  public final static String EXPR_CONTEXT_NAME = "inputs";
  public final static String EXPR_SELF_NAME = "self";
  public final static String EXPR_RUNTIME_NAME = "runtime";

  public final static int OPTIMIZATION_LEVEL = -1;
  public final static int MAX_STACK_DEPTH = 10;
  
  static Callable callable = new Callable() {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      return args[1];
    }
  };
  /**
   * Evaluate JS script (function or statement)
   */
  public static Object evaluate(Object context, Object self, String expr, CWLRuntime runtime, List<String> engineConfigs) throws CWLExpressionException {
    String trimmedExpr = StringUtils.trim(expr);
    if (trimmedExpr.startsWith("$")) {
      trimmedExpr = trimmedExpr.substring(1);
    }
    
    Context cx = Context.enter();
    cx.setOptimizationLevel(OPTIMIZATION_LEVEL);
    cx.setMaximumInterpreterStackDepth(MAX_STACK_DEPTH);
    cx.setClassShutter(new CWLExpressionDenyAllClassShutter());

    try {
      Scriptable globalScope = cx.initStandardObjects();
      if (engineConfigs != null) {
        for (int i = 0; i < engineConfigs.size(); i++) {
          Reader engineConfigReader = new StringReader(engineConfigs.get(i));
          cx.evaluateReader(globalScope, engineConfigReader, "engineConfig_" + i + ".js", 1, null);
        }
      }
      putToScope(EXPR_CONTEXT_NAME, context, cx, globalScope);
      putToScope(EXPR_SELF_NAME, self, cx, globalScope);
      putToScope(EXPR_RUNTIME_NAME, runtime, cx, globalScope);

      Scriptable resultScope = cx.newObject(globalScope);
      resultScope.setPrototype(globalScope);
      resultScope.setParentScope(globalScope);
      Object result = resolve(trimmedExpr, cx, globalScope);
      if (result == null || result instanceof Undefined) {
        return null;
      }
      return castResult(result);
    } catch (Exception e) {
      throw new CWLExpressionException(e.getMessage() + " encountered while resolving expression: " + expr, e);
    } finally {
      Context.exit();
    }
  }

  private static Object resolve(String trimmedExpr, Context cx, Scriptable resultScope) {
    String f = "$f=function()";
    if (trimmedExpr.startsWith("{")) {
      f += trimmedExpr;
    } else {
      f = f + "{return " + trimmedExpr + "}";
    }
    cx.evaluateString(resultScope, f, "script", 1, null);
    return cx.evaluateString(resultScope, "JSON.stringify($f());", "script", 1, null);
  }

  /**
   * Add object to execution scope
   */
  private static void putToScope(String name, Object value, Context cx, Scriptable scope) {
    if (value != null) {
      String selfJson = BeanSerializer.serializePartial(value);
      Object json = NativeJSON.parse(cx, scope, selfJson, callable);
      ScriptableObject.putProperty(scope, name, json);
    } else {
      ScriptableObject.putProperty(scope, name, null);
    }
  }

  /**
   * Cast result to proper Java object
   */
  private static Object castResult(Object result) {
    JsonNode node = JSONHelper.readJsonNode(result.toString());
    return JSONHelper.transform(node, false);
  }

}
