package org.rabix.bindings.draft2.reference;

import java.io.IOException;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.draft2.resolver.Draft2DocumentResolver;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.helper.ResourceHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;

@Test(groups = { "functional" })
public class Draft2ReferenceResolverTest {

  @Test
  public void test() throws Exception {
    String filePath = Draft2ReferenceResolverTest.class.getResource("count-lines1-wf.cwl.json").getFile();
    String fileURI = URIHelper.createURI(URIHelper.FILE_URI_SCHEME, filePath);
    
    JsonNode resolvedRoot = readJsonNode("count-lines1-wf-resolved.cwl.json");
    Assert.assertEquals(Draft2DocumentResolver.resolve(fileURI), resolvedRoot);
  }

  @Test
  public void testRegular() throws Exception {
    String filePath = Draft2ReferenceResolverTest.class.getResource("regular.json").getFile();
    String fileURI = URIHelper.createURI(URIHelper.FILE_URI_SCHEME, filePath);
    
    JsonNode resolvedRoot = readJsonNode("regular-resolved.json");
    Assert.assertEquals(Draft2DocumentResolver.resolve(fileURI), resolvedRoot);
  }

  @Test
  public void testRegularJsonPointer() throws Exception {
    String filePath = Draft2ReferenceResolverTest.class.getResource("regular-json-pointer.json").getFile();
    String fileURI = URIHelper.createURI(URIHelper.FILE_URI_SCHEME, filePath);
    
    JsonNode resolvedRoot = readJsonNode("regular-json-pointer-resolved.json");
    Assert.assertEquals(Draft2DocumentResolver.resolve(fileURI), resolvedRoot);
  }

  @Test(expectedExceptions = { BindingException.class })
  public void testCircular() throws Exception {
    String filePath = Draft2ReferenceResolverTest.class.getResource("circular.json").getFile();
    String fileURI = URIHelper.createURI(URIHelper.FILE_URI_SCHEME, filePath);
    
    Draft2DocumentResolver.resolve(fileURI);
  }
  
  private JsonNode readJsonNode(String inputFile) throws IOException {
    String input = ResourceHelper.readResource(this.getClass(), inputFile);
    return JSONHelper.readJsonNode(input);
  }
  
}
