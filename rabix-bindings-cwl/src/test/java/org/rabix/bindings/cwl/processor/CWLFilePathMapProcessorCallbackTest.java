package org.rabix.bindings.cwl.processor;

import java.io.IOException;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.cwl.CWLBindings;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.common.json.BeanSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class CWLFilePathMapProcessorCallbackTest {

  @Test
  @SuppressWarnings("unchecked")
  public void testInitialWorkDirRequirement() throws IOException {
    String inputJson = ResourceHelper.readResource(this.getClass(), "../bean/1st-tool.cwl");

    CWLJob cwlJob = BeanSerializer.deserialize(inputJson, CWLJob.class);

    String encodedApp = URIHelper.createDataURI(BeanSerializer.serializeFull(cwlJob.getApp()));
    Job job = new Job("id", "id", "id", "id", encodedApp, null, null, cwlJob.getInputs(), null, null, null, null);
    try {
      Bindings bindings = new CWLBindings();
      
      job = bindings.mapInputFilePaths(job, new FilePathMapper() {
        @Override
        public String map(String path, Map<String, Object> config) throws FileMappingException {
          return path + ".temp";
        }
      });
      
      Assert.assertNotNull(job.getInputs());
      Assert.assertEquals(((Map<String, Object>) job.getInputs().get("src")).get("path"), "Hello.java.temp");
      Assert.assertEquals(((Map<String, Object>) job.getInputs().get("dir")).get("path"), "hello_directory.temp");
      System.out.println(job.getInputs());
    } catch (BindingException e) {
      Assert.fail(e.getMessage());
    }
  }

}
