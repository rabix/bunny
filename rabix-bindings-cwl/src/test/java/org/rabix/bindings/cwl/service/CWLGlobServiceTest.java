package org.rabix.bindings.cwl.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.rabix.bindings.Bindings;
import org.rabix.bindings.cwl.CWLBindings;
import org.rabix.bindings.cwl.CWLValueTranslator;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.common.json.BeanSerializer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class CWLGlobServiceTest {

  private File workingDir;

  @BeforeMethod
  public void before() throws IOException {
    File baseDir = new File("target/worker/workingDir_" + System.currentTimeMillis());
    if (baseDir.exists()) {
      baseDir.delete();
    }

    baseDir.mkdirs();
    workingDir = new File(baseDir, "/workingDir");
    workingDir.mkdirs();

    File file1 = new File(workingDir, "file1.txt");
    file1.createNewFile();

    File dir1 = new File(workingDir, "dir1");
    dir1.mkdir();
    
    File file2 = new File(dir1, "file2.txt");
    file2.createNewFile();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testCommandLineTool() throws Exception {
    String inputJson = ResourceHelper.readResource(CWLGlobServiceTest.class, "cwl-glob-test-json.json");

    CWLJob cwlJob = BeanSerializer.deserialize(inputJson, CWLJob.class);
    String encodedApp = URIHelper.createDataURI(BeanSerializer.serializeFull(cwlJob.getApp()));
    
    Map<String, Object> inputs = (Map<String, Object>) CWLValueTranslator.translateToCommon(cwlJob.getInputs());
    Job job = new Job("id", "id", "id", "id", encodedApp, null, null, inputs, null, null, null, null);

    Bindings bindings = new CWLBindings();
    job = bindings.postprocess(job, workingDir, null, null);

    System.out.println(JSONHelper.writeObject(job.getOutputs()));
    Assert.assertTrue(job.getOutputs() instanceof Map<?, ?>);
    Assert.assertTrue((job.getOutputs()).containsKey("directory"));
    Assert.assertTrue((job.getOutputs()).containsKey("directory_file"));
    Assert.assertTrue((job.getOutputs()).containsKey("single"));
  }

}
