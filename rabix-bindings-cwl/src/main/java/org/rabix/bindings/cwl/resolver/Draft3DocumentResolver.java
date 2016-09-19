package org.rabix.bindings.draft3.resolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

public class Draft3DocumentResolver {

  public static final String RESOLVER_REFERENCE_KEY = "import";
  public static final String RESOLVER_JSON_POINTER_KEY = "$job";
  
  public static final String DOCUMENT_FRAGMENT_SEPARATOR = "#";
  
  private static final String DEFAULT_ENCODING = "UTF-8";

  private static ConcurrentMap<String, String> cache = new ConcurrentHashMap<>(); 
  
  private static Map<String, Map<String, Draft3DocumentResolverReference>> referenceCache = new HashMap<>();
  private static Map<String, LinkedHashSet<Draft3DocumentResolverReplacement>> replacements = new HashMap<>();
  
  public static String resolve(String appUrl) throws BindingException {
    if (cache.containsKey(appUrl)) {
      return cache.get(appUrl);
    }
    
    File file = null;
    JsonNode root = null;
    try {
      boolean isFile = URIHelper.isFile(appUrl);
      if (isFile) {
        file = new File(URIHelper.getURIInfo(appUrl));
      } else {
        file = new File(".");
      }
      String input = JSONHelper.transformToJSON(URIHelper.getData(appUrl));
      root = JSONHelper.readJsonNode(input);
    } catch (IOException e) {
      throw new BindingException(e);
    }
    traverse(appUrl, root, file, null, root, true);

    for (Draft3DocumentResolverReplacement replacement : getReplacements(appUrl)) {
      if (replacement.getParentNode().isArray()) {
        replaceArrayItem(appUrl, root, replacement);
      } else if (replacement.getParentNode().isObject()) {
        replaceObjectItem(appUrl, root, replacement);
      }
    }
    
    cache.put(appUrl, JSONHelper.writeObject(root));

    clearReplacements(appUrl);
    clearReferenceCache(appUrl);;
    return cache.get(appUrl);
  }
  
  private static JsonNode traverse(String appUrl, JsonNode root, File file, JsonNode parentNode, JsonNode currentNode, boolean addReplacement) throws BindingException {
    Preconditions.checkNotNull(currentNode, "current node id is null");

    boolean isReference = currentNode.has(RESOLVER_REFERENCE_KEY);
    boolean isJsonPointer = currentNode.has(RESOLVER_JSON_POINTER_KEY) && parentNode != null; // we skip the first level $job

    if (isReference || isJsonPointer) {
      String referencePath = null;
      if (isReference) {
        referencePath = currentNode.get(RESOLVER_REFERENCE_KEY).textValue();
      } else {
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
        reference.setResolvedNode(traverse(appUrl, root, file, parentChild.parent, parentChild.child, true));
        reference.setResolving(false);
        getReferenceCache(appUrl).put(referencePath, reference);
      }
      if (addReplacement) {
        getReplacements(appUrl).add(new Draft3DocumentResolverReplacement(parentNode, currentNode, referencePath));
      }
      return reference.getResolvedNode();
    } else if (currentNode.isContainerNode()) {
      for (JsonNode subnode : currentNode) {
        traverse(appUrl, root, file, currentNode, subnode, addReplacement);
      }
    }
    return currentNode;
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
      String externalResource = parts[0];
      if (externalResource.startsWith("http")) {
        try {
          URL website = new URL(externalResource);
          URLConnection connection = website.openConnection();
          BufferedReader in = null;

          try {
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
              response.append(inputLine);
            }

            String linkContents = response.toString();
            return JSONHelper.readJsonNode(linkContents);
          } finally {
            if (in != null) {
              in.close();
            }
          }
        } catch (Exception e) {
          throw new BindingException("Couldn't fetch contents from " + externalResource);
        }
      } else {
        try {
          String filePath = new File(file.getParentFile(), externalResource).getCanonicalPath();
          String fileContents = FileUtils.readFileToString(new File(filePath), DEFAULT_ENCODING);
          return JSONHelper.readJsonNode(JSONHelper.transformToJSON(fileContents));
        } catch (IOException e) {
          throw new BindingException("Couldn't fetch contents from " + externalResource);
        }
      }
    }
  }

  private static ParentChild findReferencedNode(JsonNode rootNode, String absolutePath) {
    if (!absolutePath.contains(DOCUMENT_FRAGMENT_SEPARATOR)) {
      return new ParentChild(null, rootNode);
    }
    String subpath = absolutePath.substring(absolutePath.indexOf(DOCUMENT_FRAGMENT_SEPARATOR) + 1);
    String[] parts = subpath.split("/");

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
