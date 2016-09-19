package org.rabix.bindings.draft3.expression.javascript;

import org.mozilla.javascript.ClassShutter;

public class Draft3ExpressionDenyAllClassShutter implements ClassShutter {

  @Override
  public boolean visibleToScripts(String arg0) {
    return false;
  }

}
