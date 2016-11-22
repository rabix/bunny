package org.rabix.bindings.draft2;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.rabix.bindings.Bindings;
import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.common.json.BeanSerializer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class Draft2ResultCollectionServiceTest {

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

    File file2 = new File(workingDir, "file2.txt");
    file2.createNewFile();

    File file3 = new File(workingDir, "file3.dat");
    file3.createNewFile();

    File file4 = new File(workingDir, "file4.dat");
    file4.createNewFile();
  }

  @Test
  public void testCommandLineTool() throws Exception {
    String inputJson = ResourceHelper.readResource(Draft2ResultCollectionServiceTest.class, "output-collection-job.json");

    Draft2Job draft2Job = BeanSerializer.deserialize(inputJson, Draft2Job.class);
    String encodedApp = URIHelper.createDataURI(BeanSerializer.serializeFull(draft2Job.getApp()));
    Job job = new Job("id", "id", "id", "id", encodedApp, null, null, draft2Job.getInputs(), null, null, null, null);
    
    Bindings bindings = new Draft2Bindings();
    job = bindings.postprocess(job, workingDir, null, null);
    
    Assert.assertTrue(job.getOutputs() instanceof Map<?,?>);
    Assert.assertTrue((job.getOutputs()).containsKey("single"));
    Assert.assertTrue((job.getOutputs()).containsKey("array"));
    Assert.assertTrue((job.getOutputs()).containsKey("record"));
  }
  
  @Test
  public void testExpressionTool() throws Exception {
    String inputJson = ResourceHelper.readResource(Draft2ResultCollectionServiceTest.class, "bean/expression-job.json");

    Draft2Job draft2Job = BeanSerializer.deserialize(inputJson, Draft2Job.class);
    String encodedApp = URIHelper.createDataURI(BeanSerializer.serializeFull(draft2Job.getApp()));
    Job job = new Job("id", "id", "id", "id", encodedApp, null, null, draft2Job.getInputs(), null, null, null, null);
    
    Bindings bindings = new Draft2Bindings();
    job = bindings.postprocess(job, workingDir, null, null);
    
    Assert.assertTrue(job.getOutputs() instanceof Map<?,?>);
    Assert.assertTrue((job.getOutputs()).containsKey("output"));
  }

}
