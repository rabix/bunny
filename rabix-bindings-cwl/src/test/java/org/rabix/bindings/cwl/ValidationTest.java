package org.rabix.bindings.cwl;


import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.cwl.bean.CWLCommandLineTool;
import org.rabix.bindings.cwl.bean.CWLJobApp;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.common.json.BeanSerializer;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.File;
import java.util.Collections;
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

  @Test(expectedExceptions = IllegalStateException.class)
  public void testWrongType()  throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "bad-success-codes.cwl");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
  }

  @Test
  public void testDuplicateInputId()  throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "duplicate-input-id.cwl");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
    List<String> errors = app.validate().getErrors();
    assertEquals(errors.size(), 1, "Expecting one error");
    assertEquals(errors.get(0), "Duplicate input id: message");
  }

  @Test
  public void testNoExpression()  throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "no-expression.yml");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
    List<String> errors = app.validate().getErrors();
    assertEquals(errors.size(), 1, "Expecting one error");
    assertEquals(errors.get(0), "ExpressionTool must have an expression");
  }

  @Test
  public void testEmptyExpression()  throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "empty-expression.yml");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
    List<String> warnings = app.validate().getWarnings();
    assertEquals(warnings.size(), 1, "Expecting one error");
    assertEquals(warnings.get(0), "Expression is empty");
  }

  @Test
  public void testInvalidExpression()  throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "invalid-expression.yml");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
    List<String> errors = app.validate().getErrors();
    assertEquals(errors.size(), 1, "Expecting one error");
    assertEquals(errors.get(0), "ExpressionTool expression must be a string, got '{one=1}' instead");
  }

  @Test
  public void testNoBaseCommand()  throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "no-base-command.cwl");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
    List<String> warnings = app.validate().getWarnings();
    assertEquals(warnings.size(), 1, "Expecting one error");
    assertEquals(warnings.get(0), "Tool's 'baseCommand' is empty");
  }

  @Test
  public void testEmptyBaseCommand()  throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "empty-base-command.cwl");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
    List<String> warnings = app.validate().getWarnings();
    assertEquals(warnings.size(), 1, "Expecting one error");
    assertEquals(warnings.get(0), "Tool's 'baseCommand' is empty");
  }

  @Test
  public void testInvalidBaseCommand()  throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "invalid-base-command.cwl");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
    List<String> errors = app.validate().getErrors();
    assertEquals(errors.size(), 1, "Expecting one error");
    assertEquals(errors.get(0), "Tool's 'baseCommand' must be a string or a list of strings, got '17' instead");
  }

  @Test
  public void testBadArguments()  throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "bad-arguments.cwl");
    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);
    List<String> errors = app.validate().getErrors();
    assertEquals(errors.size(), 2, "Expecting two errors");

    assertEquals(errors.get(0),"CommandLineBinding in 'arguments' must have a 'valueFrom' present",
        "Expecting to detect invalid CommandLineBinding");
    assertEquals(errors.get(1), "Tool's 'arguments' must be a list of strings or CommandLineBindings, got '42' instead",
        "Expecting to detect argument of a wrong type");

  }

  @Test
  public void testEmptyWorkflow()  throws Exception {
    String appURL = "file://" + ResourceHelper.getResourcePath(this.getClass(), "empty-wf.yml");
    Bindings b = BindingsFactory.create(appURL);
    b.translateToDAG(new Job(appURL, Collections.emptyMap()));
  }

  @Test
  public void testDuplicateStepId()  throws Exception {
    String appURL = "file://" + ResourceHelper.getResourcePath(this.getClass(), "duplicate-step-id.cwl.yml");
    Bindings b = BindingsFactory.create(appURL);
    Application app = b.loadAppObject(appURL);

//    String inputJson = ResourceHelper.readResource(this.getClass(), "duplicate-step-id.cwl.yml");
//    CWLJobApp app = BeanSerializer.deserialize(inputJson, CWLJobApp.class);

    List<String> errors = app.validate().getErrors();
    assertEquals(errors.size(), 1, "Expecting one error");
    assertEquals(errors.get(0), "Duplicate step id: one");
  }

  @Test
  public void testWorkflowWithInvalidApp()  throws Exception {
    String appURL = "file://" + ResourceHelper.getResourcePath(this.getClass(), "workflow-with-invalid-app.cwl.yml");
    Bindings b = BindingsFactory.create(appURL);
    Application app = b.loadAppObject(appURL);

    List<String> errors = app.validate().getErrors();
    assertEquals(errors.size(), 1, "Expecting one error");
    assertEquals(errors.get(0), "ERROR from app in step 'one': Docker requirement contains neither 'dockerPull' nor 'imageId'.");
  }

  @Test(expectedExceptions = BindingException.class)
  public void testInvalidVersion()  throws Exception {
    String appURL = "file://" + ResourceHelper.getResourcePath(this.getClass(), "invalid-version.cwl");
    Bindings b = BindingsFactory.create(appURL);
    Application app = b.loadAppObject(appURL);
  }

  @Test(expectedExceptions = BindingException.class)
  public void testWorkflowWithInvalidAppVersion()  throws Exception {
    String appURL = "file://" + ResourceHelper.getResourcePath(this.getClass(), "workflow-with-invalid-app-version.cwl.yml");
    Bindings b = BindingsFactory.create(appURL);
    Application app = b.loadAppObject(appURL);
  }

  @Test(expectedExceptions = BindingException.class)
  public void testGiveMeYourWorst()  throws Exception {
    String appURL = "file://" + ResourceHelper.getResourcePath(this.getClass(), "bad.json");
    Bindings b = BindingsFactory.create(appURL);
    Application app = b.loadAppObject(appURL);
  }

  @Test(expectedExceptions = BindingException.class)
  public void testCyclicWorkflow()  throws Exception {
    String appURL = "file://" + ResourceHelper.getResourcePath(this.getClass(), "cyclic-workflow.cwl");
    Bindings b = BindingsFactory.create(appURL);
    b.translateToDAG(new Job(appURL, Collections.emptyMap()));
  }


}
