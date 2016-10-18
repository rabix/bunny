package org.rabix.bindings.sb;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class SBWorflowTests {
  
  @Test
  public void testNestedWorkflowDeserialization() throws IOException, BindingException {
    String inputJson = ResourceHelper.readResource(this.getClass(), "grep-nested-wf-job.json");
    String appJson = ResourceHelper.readResource(this.getClass(), "grep-nested-wf.json");
    String appURI = URIHelper.createDataURI(appJson);
    
    Map<String, Object> inputs = JSONHelper.readMap(inputJson);
    
    Job job = new Job(appURI, inputs);
    job = Job.cloneWithName(job, "root.blah.1");
    Bindings bindings = new SBBindings();
    DAGNode node = bindings.translateToDAG(job);
    assertEquals(((DAGContainer) node).getChildren().get(0).getId(), "root.blah.1.grep");
  }
}
