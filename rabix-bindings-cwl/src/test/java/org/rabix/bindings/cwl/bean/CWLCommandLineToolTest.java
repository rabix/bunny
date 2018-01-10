package org.rabix.bindings.cwl.bean;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.cwl.CWLBindings;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.common.json.BeanSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class CWLCommandLineToolTest {

  @Test
  public void test1stTool() throws IOException {
    String inputJson = ResourceHelper.readResource(this.getClass(), "1st-tool.cwl");

    CWLJob cwlJob = BeanSerializer.deserialize(inputJson, CWLJob.class);
    cwlJob.setRuntime(new CWLRuntime(null, null, "/path/to/outdir", "/path/to/tmpdir", null, null));

    List<Object> expectedList = new LinkedList<Object>();
    expectedList.add("echo");
    expectedList.add("test");

    List<?> resultList;
    try {
      String encodedApp = URIHelper.createDataURI(BeanSerializer.serializeFull(cwlJob.getApp()));
      Job job = new Job(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "id", encodedApp, null, null, cwlJob.getInputs(), null, null, null, null);
      Bindings bindings = new CWLBindings();
      resultList = bindings.buildCommandLineObject(job, new File("."), (String s, Map<String, Object> config) -> s).getParts();
      Assert.assertNotNull(resultList);
      Assert.assertEquals(resultList.size(), expectedList.size());
      Assert.assertEquals(resultList, expectedList);
    } catch (BindingException e) {
      Assert.fail(e.getMessage());
    }
  }

}