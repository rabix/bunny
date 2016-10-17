package org.rabix.bindings.cwl.bean;

import java.io.IOException;
import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.cwl.CWLBindings;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleInputFileRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.common.json.BeanSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class CWLInitialWorkDirRequirementTest {

  @Test
  public void testInitialWorkDirRequirement() throws IOException {
    String inputJson = ResourceHelper.readResource(this.getClass(), "1st-tool.cwl");

    CWLJob cwlJob = BeanSerializer.deserialize(inputJson, CWLJob.class);

    try {
      String encodedApp = URIHelper.createDataURI(BeanSerializer.serializeFull(cwlJob.getApp()));
      Job job = new Job("id", "id", "id", "id", encodedApp, null, null, cwlJob.getInputs(), null, null, null, null);
      Bindings bindings = new CWLBindings();
      
      List<Requirement> requirements = bindings.getRequirements(job);
      
      FileRequirement fileRequirement = getRequirement(requirements, FileRequirement.class);
      Assert.assertNotNull(fileRequirement);
      
      Assert.assertNotNull(fileRequirement.getFileRequirements());
      Assert.assertEquals(fileRequirement.getFileRequirements().size(), 1);
      Assert.assertTrue(fileRequirement.getFileRequirements().get(0) instanceof SingleInputFileRequirement);
    } catch (BindingException e) {
      Assert.fail(e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends Requirement> T getRequirement(List<Requirement> requirements, Class<T> clazz) {
    for (Requirement requirement : requirements) {
      if (requirement.getClass().equals(clazz)) {
        return (T) requirement;
      }
    }
    return null;
  }
  
}