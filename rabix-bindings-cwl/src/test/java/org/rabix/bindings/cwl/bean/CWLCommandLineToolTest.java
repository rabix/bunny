package org.rabix.bindings.cwl.bean;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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

    List<Object> expectedList = new LinkedList<Object>();
    expectedList.add("echo");
    expectedList.add("test");

    List<?> resultList;
    try {
      String encodedApp = URIHelper.createDataURI(BeanSerializer.serializeFull(cwlJob.getApp()));
      Job job = new Job("id", "id", "id", "id", encodedApp, null, null, cwlJob.getInputs(), null, null, null, null);
      Bindings bindings = new CWLBindings();
      resultList = bindings.buildCommandLineParts(job, null, null);
      Assert.assertNotNull(resultList);
      Assert.assertEquals(resultList.size(), expectedList.size());
      Assert.assertEquals(resultList, expectedList);
    } catch (BindingException e) {
      Assert.fail(e.getMessage());
    }
  }

}