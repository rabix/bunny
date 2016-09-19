package org.rabix.bindings.cwl.expression.javascript;

import org.mozilla.javascript.ClassShutter;

public class CWLExpressionDenyAllClassShutter implements ClassShutter {

  @Override
  public boolean visibleToScripts(String arg0) {
    return false;
  }

}
