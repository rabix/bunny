package org.rabix.bindings.draft3.resolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.BindingWrongVersionException;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;

public class Draft3DocumentResolver {
  
public static Set<String> types = new HashSet<String>();
  
  static {
    types.add("null");
    types.add("boolean");
    types.add("int");
    types.add("long");
    types.add("float");
    types.add("double");
    types.add("string");
    types.add("File");
    types.add("record");
    types.add("enum");
    types.add("array");
    types.add("Any");
  }


  public static final String APP_STEP_KEY = "run";
  public static final String TYPE_KEY = "type";
  public static final String RESOLVER_REFERENCE_KEY = "$import";
  public static final String RESOLVER_REFERENCE_INCLUDE_KEY = "$include";
  public static final String GRAPH_KEY = "$graph";
  public static final String SCHEMA_KEY = "$schemas";
  public static final String NAMESPACES_KEY = "$namespaces";
  public static final String SCHEMADEF_KEY = "SchemaDefRequirement";
  public static final String CWL_VERSION_KEY = "cwlVersion";
  
  public static final String RESOLVER_JSON_POINTER_KEY = "$job";
  
  public static final String DOCUMENT_FRAGMENT_SEPARATOR = "#";
  
  private static final String DEFAULT_ENCODING = "UTF-8";

  private static boolean graphResolve = false;
  
  private static Map<String, String> namespaces = new HashMap<String, String>();
  private static Map<String, Map<String, Draft3DocumentResolverReference>> referenceCache = new HashMap<>();
  private static Map<String, LinkedHashSet<Draft3DocumentResolverReplacement>> replacements = new HashMap<>();
  
  public static String resolve(String appUrl) throws BindingException {
    String appUrlBase = appUrl;
    try {
      URI uri = URI.create(appUrl);
      if (uri.getScheme().equals(URIHelper.DATA_URI_SCHEME)) {
        appUrlBase = URIHelper.extractBase(appUrl);
      }
    } catch (IllegalArgumentException e) {

    }
    
    File file = null;
    JsonNode root = null;
    try {
      boolean isFile = URIHelper.isFile(appUrlBase);
      if (isFile) {
        file = new File(URIHelper.getURIInfo(appUrlBase));
      } else {
        file = new File(".");
      }
      root = JSONHelper.readJsonNode(URIHelper.getData(appUrlBase));
    } catch (IOException e) {
      throw new BindingException(e);
    }
    
    if(root.has(GRAPH_KEY)) {
      graphResolve = true;
    }
    
    if(root.has(NAMESPACES_KEY)) {
      populateNamespaces(root);
      ((ObjectNode) root).remove(NAMESPACES_KEY);
    }
    
    JsonNode cwlVersion = root.get(CWL_VERSION_KEY);
    if (cwlVersion == null || !(cwlVersion.asText().equals(ProtocolType.DRAFT3.appVersion))) {
      clearReplacements(appUrl);
      clearReferenceCache(appUrl);
      throw new BindingWrongVersionException("Document version is not " + ProtocolType.DRAFT3.appVersion);
    }
    
    traverse(appUrl, root, file, null, root);

    for (Draft3DocumentResolverReplacement replacement : getReplacements(appUrl)) {
      if (replacement.getParentNode().isArray()) {
        replaceArrayItem(appUrl, root, replacement);
      } else if (replacement.getParentNode().isObject()) {
        replaceObjectItem(appUrl, root, replacement);
      }
    }
    
    if (root.has(SCHEMA_KEY)) {
      throw new NotImplementedException("Feature not implemented");
    }
    
    if(graphResolve) {
      String fragment = URIHelper.extractFragment(appUrl).substring(1);
      
      clearReplacements(appUrl);
      clearReferenceCache(appUrl);
      
      removeFragmentIdentifier(appUrl, root, file, null, root, fragment);
      
      for (Draft3DocumentResolverReplacement replacement : getReplacements(appUrl)) {
        if (replacement.getParentNode().isArray()) {
          replaceArrayItem(appUrl, root, replacement);
        } else if (replacement.getParentNode().isObject()) {
          replaceObjectItem(appUrl, root, replacement);
        }
      }
      
      for (final JsonNode elem : root.get(GRAPH_KEY)) {
        if (elem.get("id").asText().equals(fragment)) {
          Map<String, Object> result = JSONHelper.readMap(elem);
          result.put(CWL_VERSION_KEY, cwlVersion);
          root = JSONHelper.convertToJsonNode(result);
          break;
        }
      }
      graphResolve = false;
    }
    else {
      if (!(root.get(CWL_VERSION_KEY).asText().equals(ProtocolType.DRAFT3.appVersion))) {
        clearReplacements(appUrl);
        clearReferenceCache(appUrl);
        throw new BindingException("Document version is not cwl:draft-3");
      }
    }

    clearReplacements(appUrl);
    clearReferenceCache(appUrl);
    return JSONHelper.writeObject(root);
  }
  
  private static void populateNamespaces(JsonNode root) {
    Iterator<Entry<String, JsonNode>> fieldIterator = root.get(NAMESPACES_KEY).fields();
    while (fieldIterator.hasNext()) {
      Entry<String, JsonNode> fieldEntry = fieldIterator.next();
      namespaces.put(fieldEntry.getKey(), fieldEntry.getValue().asText());
    }
  }
  
  private static JsonNode traverse(String appUrl, JsonNode root, File file, JsonNode parentNode, JsonNode currentNode) throws BindingException {
    Preconditions.checkNotNull(currentNode, "current node id is null");

    boolean isInclude = currentNode.has(RESOLVER_REFERENCE_INCLUDE_KEY);
    
    if (isInclude) {
      String path = currentNode.get(RESOLVER_REFERENCE_INCLUDE_KEY).textValue();
      String content = loadContents(file, path);

      Draft3DocumentResolverReference reference = new Draft3DocumentResolverReference(false, new TextNode(content));
      getReferenceCache(appUrl).put(path, reference);
      getReplacements(appUrl).add(new Draft3DocumentResolverReplacement(parentNode, currentNode, path));
      return null;
    }
    
    namespace(currentNode);
    
    boolean isReference = currentNode.has(RESOLVER_REFERENCE_KEY);
    boolean appReference = currentNode.has(APP_STEP_KEY) && currentNode.get(APP_STEP_KEY).isTextual();
    boolean typeReference = currentNode.has(TYPE_KEY) && currentNode.get(TYPE_KEY).isTextual() && isTypeReference(currentNode.get(TYPE_KEY).textValue());
    boolean isJsonPointer = currentNode.has(RESOLVER_JSON_POINTER_KEY) && parentNode != null; // we skip the first level $job

    if (isReference || isJsonPointer || appReference || typeReference) {
      String referencePath = null;
      if (isReference) {
        referencePath = currentNode.get(RESOLVER_REFERENCE_KEY).textValue();
      } 
      else if (appReference) {
        referencePath = currentNode.get(APP_STEP_KEY).textValue();
      } 
      else if(typeReference) {
        referencePath = currentNode.get(TYPE_KEY).textValue();
      }
      else {
        referencePath = currentNode.get(RESOLVER_JSON_POINTER_KEY).textValue();
      }

      Draft3DocumentResolverReference reference = getReferenceCache(appUrl).get(referencePath);
      if (reference != null) {
        if (reference.isResolving()) {
          throw new BindingException("Circular dependency detected!");
        }
      } else {
        reference = new Draft3DocumentResolverReference();
        reference.setResolving(true);
        getReferenceCache(appUrl).put(referencePath, reference);

        JsonNode referenceDocumentRoot = findDocumentRoot(root, file, referencePath, isJsonPointer);
        ParentChild parentChild = findReferencedNode(referenceDocumentRoot, referencePath);
        JsonNode resolvedNode = traverse(appUrl, root, file, parentChild.parent, parentChild.child);
        if(resolvedNode == null) {
          return null;
        }
        
        reference.setResolvedNode(resolvedNode);
        reference.setResolving(false);
        getReferenceCache(appUrl).put(referencePath, reference);
      }
      if(appReference) {
        getReplacements(appUrl).add(new Draft3DocumentResolverReplacement(currentNode, currentNode.get("run"), referencePath));
      }
      else if(typeReference) {
        getReplacements(appUrl).add(new Draft3DocumentResolverReplacement(currentNode, currentNode.get("type"), referencePath));
      }
      else {
        getReplacements(appUrl).add(new Draft3DocumentResolverReplacement(parentNode, currentNode, referencePath));
      }
      return reference.getResolvedNode();
    } else if (currentNode.isContainerNode()) {
      for (JsonNode subnode : currentNode) {
        traverse(appUrl, root, file, currentNode, subnode);
      }
    }
    return currentNode;
  }
  
  private static void namespace(JsonNode currentNode) {
    Iterator<Entry<String, JsonNode>> fieldIterator = currentNode.fields();
    while (fieldIterator.hasNext()) {
      Entry<String, JsonNode> fieldEntry = fieldIterator.next();
      if(fieldEntry.getValue().isTextual() && namespaces.keySet().contains(fieldEntry.getValue().asText().split(":")[0])) {
        String prefix = namespaces.get(fieldEntry.getValue().asText().split(":")[0]);
        String namespacedValue = fieldEntry.getValue().asText().replace(fieldEntry.getValue().asText().split(":")[0] + ":", prefix);
        ((ObjectNode) currentNode).put(fieldEntry.getKey(), namespacedValue);
      }
    }
  }
  
  private static boolean isTypeReference(String type) {
    if(types.contains(type)) {
      return false;
    }
    return true;
  }

  @SuppressWarnings("deprecation")
  private static void replaceObjectItem(String appUrl, JsonNode root, Draft3DocumentResolverReplacement replacement) throws BindingException {
    JsonNode parent = replacement.getParentNode() == null ? root : replacement.getParentNode();

    Iterator<Entry<String, JsonNode>> fieldIterator = parent.fields();
    String fieldName = null;
    while (fieldIterator.hasNext()) {
      Entry<String, JsonNode> fieldEntry = fieldIterator.next();
      if (fieldEntry.getValue().equals(replacement.getReferenceNode())) {
        fieldName = fieldEntry.getKey();
        fieldIterator.remove();
        break;
      }
    }
    Draft3DocumentResolverReference reference = getReferenceCache(appUrl).get(replacement.getNormalizedReferencePath());
    if (reference != null) {
      ((ObjectNode) parent).put(fieldName, reference.getResolvedNode());
    } else {
      throw new BindingException("Cannot find reference " + replacement.getNormalizedReferencePath());
    }
  }

  private static void replaceArrayItem(String appUrl, JsonNode root, Draft3DocumentResolverReplacement replacement) throws BindingException {
    JsonNode parent = replacement.getParentNode() == null ? root : replacement.getParentNode();

    Iterator<JsonNode> nodeIterator = parent.elements();
    while (nodeIterator.hasNext()) {
      JsonNode subnode = nodeIterator.next();
      if (subnode.equals(replacement.getReferenceNode())) {
        nodeIterator.remove();
        break;
      }
    }
    if (parent.isArray()) {
      Draft3DocumentResolverReference reference = getReferenceCache(appUrl).get(replacement.getNormalizedReferencePath());
      if (reference != null) {
        ((ArrayNode) parent).add(reference.getResolvedNode());
      } else {
        throw new BindingException("Cannot find reference " + replacement.getNormalizedReferencePath());
      }
    }
  }

  private static JsonNode findDocumentRoot(JsonNode root, File file, String reference, boolean isJsonPointer) throws BindingException {
    JsonNode startNode = root;
    if (isJsonPointer) {
      startNode = startNode.get(RESOLVER_JSON_POINTER_KEY);
    }
    int start = reference.indexOf(DOCUMENT_FRAGMENT_SEPARATOR);

    if (start == 0) {
      return startNode;
    } else {
      String[] parts = reference.split(DOCUMENT_FRAGMENT_SEPARATOR);
      if (parts.length > 2) {
        throw new BindingException("Invalid reference " + reference);
      }
      String contents = loadContents(file, parts[0]);      
      try {
        return JSONHelper.readJsonNode(contents);
      } catch (Exception e) {
        throw new BindingException(e);
      }
    }
  }
  
  private static String loadContents(File file, String path) throws BindingException {
    if (path.startsWith("http")) {
      try {
        URL website = new URL(path);
        URLConnection connection = website.openConnection();
        BufferedReader in = null;

        try {
          in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

          StringBuilder response = new StringBuilder();
          String inputLine;
          while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
          }
          return response.toString();
        } finally {
          if (in != null) {
            in.close();
          }
        }
      } catch (Exception e) {
        throw new BindingException("Couldn't fetch contents from " + path);
      }
    } else {
      try {
        String filePath = new File(file.getParentFile(), path).getCanonicalPath();
        return FileUtils.readFileToString(new File(filePath), DEFAULT_ENCODING);
      } catch (IOException e) {
        throw new BindingException("Couldn't fetch contents from " + path);
      }
    }
  }

  private static ParentChild findReferencedNode(JsonNode rootNode, String absolutePath) {
    if (!absolutePath.contains(DOCUMENT_FRAGMENT_SEPARATOR)) {
      return new ParentChild(null, rootNode);
    }
    String subpath = absolutePath.substring(absolutePath.indexOf(DOCUMENT_FRAGMENT_SEPARATOR) + 1);
    String[] parts = subpath.split("/");

    if(rootNode.has("$graph")) {
      JsonNode objects = rootNode.get("$graph");
      JsonNode child = null;
      JsonNode parent = objects;
      for(final JsonNode elem: objects) {
        if(elem.get("id").asText().equals(parts[0])) {
          child = elem;
          break;
        }
      }
      return new ParentChild(parent, child);
    }
    else if (rootNode.has("class") && rootNode.get("class").asText().equals(SCHEMADEF_KEY)) {
      JsonNode objects = rootNode.get("types");
      JsonNode child = null;
      for(final JsonNode elem: objects) {
        if(elem.get("name").asText().equals(parts[0])) {
          child = elem;
          break;
        }
      }
      return new ParentChild(null, child);
    }
    
    JsonNode parent = null;
    JsonNode child = rootNode;
    for (String part : parts) {
      if (StringUtils.isEmpty(part)) {
        continue;
      }
      parent = child;
      child = child.get(part);
    }
    return new ParentChild(parent, child);
  }
  
  private static JsonNode removeFragmentIdentifier(String appUrl, JsonNode root, File file, JsonNode parentNode, JsonNode currentNode, String fragment) throws BindingException {
    Preconditions.checkNotNull(currentNode, "current node id is null");
    if(currentNode.isTextual() && currentNode.asText().startsWith(DOCUMENT_FRAGMENT_SEPARATOR)) {
      Draft3DocumentResolverReference reference = new Draft3DocumentResolverReference();
      reference.setResolvedNode(JsonNodeFactory.instance.textNode(currentNode.asText().replace(fragment + "/", "")));
      getReferenceCache(appUrl).put(currentNode.asText(), reference);
      getReplacements(appUrl).add(new Draft3DocumentResolverReplacement(parentNode, currentNode, currentNode.asText()));
      
    }
    else if (currentNode.isContainerNode()) {
      for (JsonNode subnode : currentNode) {
        removeFragmentIdentifier(appUrl, root, file, currentNode, subnode, fragment);
      }
    }
    return currentNode;
  }
  
  private synchronized static Set<Draft3DocumentResolverReplacement> getReplacements(String url) {
    LinkedHashSet<Draft3DocumentResolverReplacement> replacementsPerUrl = replacements.get(url);
    if (replacementsPerUrl == null) {
      replacementsPerUrl = new LinkedHashSet<Draft3DocumentResolverReplacement>();
      replacements.put(url, replacementsPerUrl);
    }
    return replacementsPerUrl;
  }
  
  private synchronized static void clearReplacements(String url) {
    replacements.remove(url);
  }
  
  private synchronized static Map<String, Draft3DocumentResolverReference> getReferenceCache(String url) {
    Map<String, Draft3DocumentResolverReference> referenceCachePerUrl = referenceCache.get(url);
    if (referenceCachePerUrl == null) {
      referenceCachePerUrl = new HashMap<String, Draft3DocumentResolverReference>();
      referenceCache.put(url, referenceCachePerUrl);
    }
    return referenceCachePerUrl;
  }
  
  private synchronized static void clearReferenceCache(String url) {
    referenceCache.remove(url);
  }
  
  private static class ParentChild {
    JsonNode parent;
    JsonNode child;

    ParentChild(JsonNode parent, JsonNode child) {
      this.parent = parent;
      this.child = child;
    }

    @Override
    public String toString() {
      return "ParentChild [parent=" + parent + ", child=" + child + "]";
    }
  }

}
