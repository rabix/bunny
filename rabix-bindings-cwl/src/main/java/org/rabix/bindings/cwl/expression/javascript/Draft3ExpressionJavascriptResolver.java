package org.rabix.bindings.draft3.expression.javascript;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.rabix.bindings.draft3.expression.Draft3ExpressionException;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

import com.fasterxml.jackson.databind.JsonNode;

public class Draft3ExpressionJavascriptResolver {

  public final static int TIMEOUT_IN_SECONDS = 5;

  public final static String EXPR_CONTEXT_NAME = "inputs";
  public final static String EXPR_SELF_NAME = "self";

  public final static int OPTIMIZATION_LEVEL = -1;
  public final static int MAX_STACK_DEPTH = 10;

  /**
   * Evaluate JS script (function or statement)
   */
  public static Object evaluate(Object context, Object self, String expr, List<String> engineConfigs) throws Draft3ExpressionException {
    String trimmedExpr = StringUtils.trim(expr);
    if (trimmedExpr.startsWith("$")) {
      trimmedExpr = trimmedExpr.substring(1);
    }
    
    String function = trimmedExpr;
    if (trimmedExpr.startsWith("{")) {
      function = "(function()%expr)()";
      function = function.replace("%expr", trimmedExpr);
    }

    Context cx = Context.enter();
    cx.setOptimizationLevel(OPTIMIZATION_LEVEL);
    cx.setMaximumInterpreterStackDepth(MAX_STACK_DEPTH);
    cx.setClassShutter(new Draft3ExpressionDenyAllClassShutter());

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

      Scriptable resultScope = cx.newObject(globalScope);
      resultScope.setPrototype(globalScope);
      resultScope.setParentScope(globalScope);

      Object result = cx.evaluateString(resultScope, function, "script", 1, null);
      if (result == null || result instanceof Undefined) {
        return null;
      }
      
      Object wrappedResult = Context.javaToJS(result, globalScope);
      putToScope("$result", wrappedResult, cx, globalScope);
      ScriptableObject.putProperty(globalScope, "$result", wrappedResult);

      String finalFunction = "(function() { " + "           var result = $result;"
          + "           var type = result instanceof Array? \"array\" : typeof result;"
          + "           return JSON.stringify({ \"result\" : result, \"type\" : type }); " + "     })()";

      Scriptable wrapScope = cx.newObject(globalScope);
      wrapScope.setPrototype(globalScope);
      wrapScope.setParentScope(globalScope);
      result = cx.evaluateString(wrapScope, finalFunction, "script", 1, null);
      return castResult(result);
    } catch (Exception e) {
      String msg = String.format("Failed evaluating expression %s.", expr);
      throw new Draft3ExpressionException(msg, e);
    } finally {
      Context.exit();
    }
  }

  /**
   * Add object to execution scope
   */
  private static void putToScope(String name, Object value, Context cx, Scriptable scope) {
    if (value != null) {
      String selfJson = BeanSerializer.serializePartial(value);

      Object json = NativeJSON.parse(cx, scope, selfJson, new org.mozilla.javascript.Callable() {
        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
          return args[1];
        }
      });
      ScriptableObject.putProperty(scope, name, json);
    } else {
      ScriptableObject.putProperty(scope, name, null);
    }
  }

  /**
   * Cast result to proper Java object
   */
  private static Object castResult(Object result) {
    if (result == null) {
      return null;
    }
    JsonNode node = JSONHelper.readJsonNode(result.toString());
    return JSONHelper.transform(node.get("result"));
  }

}
