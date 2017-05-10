package org.rabix.bindings.cwl;


import org.rabix.bindings.cwl.bean.CWLJobApp;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.common.json.BeanSerializer;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.List;

@Test(groups = { "functional" })
public class ValidationTest {

  @Test(expectedExceptions = IllegalStateException.class)
  public void testNoClass() throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "no-class.cwl");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testNonStringClass()  throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "non-string-class.cwl");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
  }
  @Test(expectedExceptions = IllegalStateException.class)
  public void testInvalidClass()  throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "invalid-class.cwl");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
  }

  @Test
  public void testDuplicateInputId()  throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "duplicate-input-id.cwl");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
    List<String> errors = app.validate();
    assertEquals(errors.size(), 1, "Expecting one error");
    assertEquals(errors.get(0), "Duplicate input id: message");
  }

  //@Test
  public void testDuplicateStepId()  throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "duplicate-step-id.cwl.yml");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
    List<String> errors = app.validate();
    assertEquals(errors.size(), 1, "Expecting one error");
    assertEquals(errors.get(0), "Duplicate step id: one");
  }

}
