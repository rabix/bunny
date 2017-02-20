package org.rabix.bindings.draft3.expression;

import java.io.IOException;

import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.bean.resource.requirement.Draft3InlineJavascriptRequirement;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.common.json.BeanSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class Draft3ExpressionResolverTest {

  @Test
  public void test() {
    try {
      String inputJson = ResourceHelper.readResource(this.getClass(), "draft3-bwa-mem-job.json");
      Draft3Job job = BeanSerializer.deserialize(inputJson, Draft3Job.class);

      Assert.assertEquals(Draft3ExpressionResolver.<Object>resolve(1, job, null), 1);
      Assert.assertEquals(Draft3ExpressionResolver.<Object>resolve("$(inputs['min_std_max_min'][0])", job, null), 1);
      Assert.assertEquals(Draft3ExpressionResolver.resolve("abc$(inputs['min_std_max_min'])efg", job, null), "abc[1,2,3,4]efg");
      Assert.assertEquals(Draft3ExpressionResolver.resolve(Draft3ExpressionResolver.resolve("${ return inputs.reference.path + '.tmp' }", job, null), job, null),"${ return inputs.reference.path + '.tmp' }");
      job.getApp().getRequirements().add(new Draft3InlineJavascriptRequirement());
      Assert.assertEquals(Draft3ExpressionResolver.resolve("${ return inputs.reference.path + '.tmp' }", job, null),"rabix/tests/test-files/chr20.fa.tmp");
      Assert.assertEquals(Draft3ExpressionResolver.resolve("abc$(inputs['min_std_max_min'])efg", job, null), "abc[1,2,3,4]efg");
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test(expectedExceptions = { Draft3ExpressionException.class })
  public void testIndexOutOfBounds() throws Draft3ExpressionException {
    String inputJson;
    try {
      inputJson = ResourceHelper.readResource(this.getClass(), "draft3-bwa-mem-job.json");
      Draft3Job job = BeanSerializer.deserialize(inputJson, Draft3Job.class);
      
      Assert.assertEquals(Draft3ExpressionResolver.<Object>resolve("$(inputs['min_std_max_min'][5])", job, null), 1);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

}
