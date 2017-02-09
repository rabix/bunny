package org.rabix.bindings.cwl.processor;

import java.io.IOException;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.cwl.CWLValueTranslator;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;
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
    Map<String, Object> inputs = (Map<String, Object>) CWLValueTranslator.translateToCommon(cwlJob.getInputs());
    Job job = new Job("id", "id", "id", "id", encodedApp, null, null, inputs, null, null, null, null);
    try {
      job = FileValueHelper.mapInputFilePaths(job, new FilePathMapper() {
        @Override
        public String map(String path, Map<String, Object> config) throws FileMappingException {
          return path + ".temp";
        }
      });
      
      Assert.assertNotNull(cwlJob.getInputs());
      Assert.assertNotNull(job.getInputs().get("src"));
      Assert.assertTrue((job.getInputs().get("src") instanceof FileValue));
      
      FileValue src = (FileValue) job.getInputs().get("src");
      Assert.assertEquals(src.getPath(), "Hello.java.temp");
      
      
      Assert.assertNotNull(job.getInputs().get("dir"));
      Assert.assertTrue((job.getInputs().get("dir") instanceof DirectoryValue));
      DirectoryValue dir = (DirectoryValue) job.getInputs().get("dir");
      
      Assert.assertEquals(dir.getPath(), "hello_directory.temp");
      System.out.println(job.getInputs());
    } catch (BindingException e) {
      Assert.fail(e.getMessage());
    }
  }

}
