package org.rabix.bindings.draft3.conformance;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.draft3.Draft3Bindings;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.helper.ResourceHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class ConformanceCommandLineTest {

  @Test(enabled = false)
  public void testCmdLine1() throws IOException {
    try {
      String inputsStr = ResourceHelper.readResource(this.getClass(), "bwa-mem-job.json");
      Map<String, Object> inputs = JSONHelper.readMap(inputsStr);
      
      String commandLineToolStr = ResourceHelper.readResource(this.getClass(), "bwa-mem-tool.cwl");
      
      List<Object> expectedList = new LinkedList<Object>();
      expectedList.add("bwa");
      expectedList.add("mem");
      expectedList.add("-t");
      expectedList.add("4");
      expectedList.add("-I");
      expectedList.add("1,2,3,4");
      expectedList.add("-m");
      expectedList.add("3");
      expectedList.add("rabix/tests/test-files/chr20.fa");
      expectedList.add("rabix/tests/test-files/example_human_Illumina.pe_1.fastq");
      expectedList.add("rabix/tests/test-files/example_human_Illumina.pe_2.fastq");

      Job job = new Job(commandLineToolStr, inputs);
      List<?> resultList = new Draft3Bindings().buildCommandLineObject(job, null, null).getParts();
      Assert.assertNotNull(resultList);
      Assert.assertEquals(resultList.size(), expectedList.size());
      Assert.assertEquals(resultList, expectedList);
    } catch (BindingException e) {
      Assert.fail(e.getMessage());
    }
  }
  
  @Test(enabled = false)
  public void testCmdLine2() throws IOException {
    try {
      String inputsStr = ResourceHelper.readResource(this.getClass(), "bwa-mem-job.json");
      Map<String, Object> inputs = JSONHelper.readMap(inputsStr);
      
      String commandLineToolStr = ResourceHelper.readResource(this.getClass(), "binding-test.cwl");
      
      List<Object> expectedList = new LinkedList<Object>();
      expectedList.add("bwa");
      expectedList.add("mem");
      expectedList.add("rabix/tests/test-files/chr20.fa");
      expectedList.add("-XXX");
      expectedList.add("-YYY");
      expectedList.add("rabix/tests/test-files/example_human_Illumina.pe_1.fastq");
      expectedList.add("-YYY");
      expectedList.add("rabix/tests/test-files/example_human_Illumina.pe_2.fastq");

      Job job = new Job(commandLineToolStr, inputs);
      List<?> resultList = new Draft3Bindings().buildCommandLineObject(job, null, null).getParts();
      Assert.assertNotNull(resultList);
      Assert.assertEquals(resultList.size(), expectedList.size());
      Assert.assertEquals(resultList, expectedList);
    } catch (BindingException e) {
      Assert.fail(e.getMessage());
    }
  }
  
  @Test
  public void testCmdLine3() throws IOException {
    try {
      String inputsStr = ResourceHelper.readResource(this.getClass(), "cat-job.json");
      Map<String, Object> inputs = JSONHelper.readMap(inputsStr);
      
      String commandLineToolStr = URIHelper.createDataURI(ResourceHelper.readResource(this.getClass(), "cat1-tool.cwl"));
      
      List<Object> expectedList = new LinkedList<Object>();
      expectedList.add("cat");
      expectedList.add("hello.txt");

      Job job = new Job(commandLineToolStr, inputs);
      List<?> resultList = new Draft3Bindings().buildCommandLineObject(job, null, null).getParts();
      Assert.assertNotNull(resultList);
      Assert.assertEquals(resultList.size(), expectedList.size());
      Assert.assertEquals(resultList, expectedList);
    } catch (BindingException e) {
      Assert.fail(e.getMessage());
    }
  }
  
  @Test
  public void testCmdLine4() throws IOException {
    try {
      String inputsStr = ResourceHelper.readResource(this.getClass(), "cat-n-job.json");
      Map<String, Object> inputs = JSONHelper.readMap(inputsStr);
      
      String commandLineToolStr = URIHelper.createDataURI(ResourceHelper.readResource(this.getClass(), "cat1-tool.cwl"));
      
      List<Object> expectedList = new LinkedList<Object>();
      expectedList.add("cat");
      expectedList.add("-n");
      expectedList.add("hello.txt");

      Job job = new Job(commandLineToolStr, inputs);
      List<?> resultList = new Draft3Bindings().buildCommandLineObject(job, null, null).getParts();
      Assert.assertNotNull(resultList);
      Assert.assertEquals(resultList.size(), expectedList.size());
      Assert.assertEquals(resultList, expectedList);
    } catch (BindingException e) {
      Assert.fail(e.getMessage());
    }
  }
  
  @Test
  public void testCmdLine5() throws IOException {
    try {
      String inputsStr = ResourceHelper.readResource(this.getClass(), "cat-job.json");
      Map<String, Object> inputs = JSONHelper.readMap(inputsStr);
      
      String commandLineToolStr = URIHelper.createDataURI(ResourceHelper.readResource(this.getClass(), "cat2-tool.cwl"));
      
      List<Object> expectedList = new LinkedList<Object>();
      expectedList.add("cat");

      Job job = new Job(commandLineToolStr, inputs);
      List<?> resultList = new Draft3Bindings().buildCommandLineObject(job, null, null).getParts();
      Assert.assertNotNull(resultList);
      Assert.assertEquals(resultList.size(), expectedList.size());
      Assert.assertEquals(resultList, expectedList);
    } catch (BindingException e) {
      Assert.fail(e.getMessage());
    }
  }
  
  @Test
  public void testCmdLine6() throws IOException {
    try {
      String inputsStr = ResourceHelper.readResource(this.getClass(), "cat-job.json");
      Map<String, Object> inputs = JSONHelper.readMap(inputsStr);
      
      String commandLineToolStr = URIHelper.createDataURI(ResourceHelper.readResource(this.getClass(), "cat3-tool.cwl"));
      
      Job job = new Job(commandLineToolStr, inputs);
      String cmdLine = new Draft3Bindings().buildCommandLineObject(job, null, null).build();
      Assert.assertNotNull(cmdLine);
      Assert.assertEquals("cat hello.txt > output.txt", cmdLine);
    } catch (BindingException e) {
      Assert.fail(e.getMessage());
    }
  }
  
  @Test
  public void testCmdLine7() throws IOException {
    try {
      String inputsStr = ResourceHelper.readResource(this.getClass(), "cat-job.json");
      Map<String, Object> inputs = JSONHelper.readMap(inputsStr);
      
      String commandLineToolStr = URIHelper.createDataURI(ResourceHelper.readResource(this.getClass(), "cat4-tool.cwl"));
      
      Job job = new Job(commandLineToolStr, inputs);
      String cmdLine = new Draft3Bindings().buildCommandLineObject(job, null, null).build();
      Assert.assertNotNull(cmdLine);
      Assert.assertEquals("cat < hello.txt > output.txt", cmdLine);
    } catch (BindingException e) {
      Assert.fail(e.getMessage());
    }
  }

}
