package org.rabix.bindings.cwl;


import org.rabix.bindings.cwl.bean.CWLJobApp;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.common.json.BeanSerializer;
import org.testng.annotations.Test;

import java.util.List;

@Test(groups = { "functional" })
public class ValidationTest {

  @Test(expectedExceptions = IllegalStateException.class)
  public void testNoClass() throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "no-class.cwl");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
    List<String> errors = app.validate();
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testInvalidClass()  throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "non-string-class.cwl");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
    List<String> errors = app.validate();

    inputJson = ResourceHelper.readResource(this.getClass(), "invalid-class.cwl");
    app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
    errors = app.validate();
  }

}
