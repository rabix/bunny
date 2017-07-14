package org.rabix.bindings.sb.resolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.BindingWrongVersionException;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

public class SBDocumentResolver {

  public static final String CWL_VERSION_KEY = "cwlVersion";
  
  public static final String RESOLVER_JSON_POINTER_KEY = "$job";
  
  public static final String DOCUMENT_FRAGMENT_SEPARATOR = "#";
  
  private static final String DEFAULT_ENCODING = "UTF-8";
  
  private static final Map<String, Map<String, JsonNode>> fragmentsCache = new HashMap<>();

  private static final Map<String, Map<String, SBDocumentResolverReference>> referenceCache = new HashMap<>();
  private static final Map<String, LinkedHashSet<SBDocumentResolverReplacement>> replacements = new HashMap<>();
  
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
    } catch (Exception e) {
      throw new BindingException(e);
    }

    JsonNode cwlVersion = root.get(CWL_VERSION_KEY);
    if (cwlVersion == null || !(cwlVersion.asText().equals(ProtocolType.SB.appVersion))){
      clearReplacements(appUrl);
      clearReferenceCache(appUrl);
      clearFragmentCache(appUrl);
      throw new BindingWrongVersionException("Document version is not " + ProtocolType.SB.appVersion);
    }
    
    if (root.isArray()) {
      Map<String, JsonNode> fragmentsCachePerUrl = getFragmentsCache(appUrl);
      for (JsonNode child : root) {
        fragmentsCachePerUrl.put(child.get("id").textValue(), child);
      }
      String fragment = URIHelper.extractFragment(appUrl);
      root = fragmentsCachePerUrl.get(fragment);
    }
    
    traverse(appUrl, root, file, null, root);

    for (SBDocumentResolverReplacement replacement : getReplacements(appUrl)) {
      if (replacement.getParentNode().isArray()) {
        replaceArrayItem(appUrl, root, replacement);
      } else if (replacement.getParentNode().isObject()) {
        replaceObjectItem(appUrl, root, replacement);
      }
    }
    
    if(!(root.get(CWL_VERSION_KEY).asText().equals(ProtocolType.SB.appVersion))) {
      clearReplacements(appUrl);
      clearReferenceCache(appUrl);
      clearFragmentCache(appUrl);
      throw new BindingException("Document version is not sbg:draft-2");
    }

    clearReplacements(appUrl);
    clearReferenceCache(appUrl);
    clearFragmentCache(appUrl);
    return JSONHelper.writeObject(root);
  }
  
  private static JsonNode traverse(String appUrl, JsonNode root, File file, JsonNode parentNode, JsonNode currentNode) throws BindingException {
    Preconditions.checkNotNull(currentNode, "current node id is null");


    boolean isJsonPointer = currentNode.has(RESOLVER_JSON_POINTER_KEY) && parentNode != null; // we skip the first level $job

    if (isJsonPointer) {
      String referencePath = currentNode.get(RESOLVER_JSON_POINTER_KEY).textValue();

      SBDocumentResolverReference reference = getReferenceCache(appUrl).get(referencePath);
      if (reference != null) {
        if (reference.isResolving()) {
          throw new BindingException("Circular dependency detected!");
        }
      } else {
        reference = new SBDocumentResolverReference();
        reference.setResolving(true);
        getReferenceCache(appUrl).put(referencePath, reference);

        Map<String, JsonNode> fragmentsCachePerUrl = getFragmentsCache(appUrl);
        
        ParentChild parentChild = null;
        JsonNode referenceDocumentRoot = null;
        if (fragmentsCachePerUrl != null && fragmentsCachePerUrl.containsKey(referencePath)) {
          parentChild = new ParentChild(root, fragmentsCachePerUrl.get(referencePath));
        } else {
          referenceDocumentRoot = findDocumentRoot(root, file, referencePath, isJsonPointer);
          parentChild = findReferencedNode(referenceDocumentRoot, referencePath);
        }
        JsonNode resolvedNode = traverse(appUrl, root, file, parentChild.parent, parentChild.child);
        if (resolvedNode == null) {
          return null;
        }
        reference.setResolvedNode(resolvedNode);
        reference.setResolving(false);
        getReferenceCache(appUrl).put(referencePath, reference);
      }
      getReplacements(appUrl).add(new SBDocumentResolverReplacement(parentNode, currentNode, referencePath));
      return reference.getResolvedNode();
    } else if (currentNode.isContainerNode()) {
      for (JsonNode subnode : currentNode) {
        traverse(appUrl, root, file, currentNode, subnode);
      }
    }
    return currentNode;
  }

  @SuppressWarnings("deprecation")
  private static void replaceObjectItem(String appUrl, JsonNode root, SBDocumentResolverReplacement replacement) throws BindingException {
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
    SBDocumentResolverReference reference = getReferenceCache(appUrl).get(replacement.getNormalizedReferencePath());
    if (reference != null) {
      ((ObjectNode) parent).put(fieldName, reference.getResolvedNode());
    } else {
      throw new BindingException("Cannot find reference " + replacement.getNormalizedReferencePath());
    }
  }

  private static void replaceArrayItem(String appUrl, JsonNode root, SBDocumentResolverReplacement replacement) throws BindingException {
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
      SBDocumentResolverReference reference = getReferenceCache(appUrl).get(replacement.getNormalizedReferencePath());
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
    if (path.startsWith("ftp")) {
      try {
        return URIHelper.getData(path);
      } catch (IOException e) {
        throw new BindingException(e);
      }
    }
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
  
  private synchronized static Set<SBDocumentResolverReplacement> getReplacements(String url) {
    LinkedHashSet<SBDocumentResolverReplacement> replacementsPerUrl = replacements.get(url);
    if (replacementsPerUrl == null) {
      replacementsPerUrl = new LinkedHashSet<SBDocumentResolverReplacement>();
      replacements.put(url, replacementsPerUrl);
    }
    return replacementsPerUrl;
  }
  
  private synchronized static void clearReplacements(String url) {
    replacements.remove(url);
  }
  
  private synchronized static Map<String, SBDocumentResolverReference> getReferenceCache(String url) {
    Map<String, SBDocumentResolverReference> referenceCachePerUrl = referenceCache.get(url);
    if (referenceCachePerUrl == null) {
      referenceCachePerUrl = new HashMap<String, SBDocumentResolverReference>();
      referenceCache.put(url, referenceCachePerUrl);
    }
    return referenceCachePerUrl;
  }
  
  private synchronized static Map<String, JsonNode> getFragmentsCache(String url) {
    Map<String, JsonNode> fragmentsCachePerUrl = fragmentsCache.get(url);
    if (fragmentsCachePerUrl == null) {
      fragmentsCachePerUrl = new HashMap<String, JsonNode>();
      fragmentsCache.put(url, fragmentsCachePerUrl);
    }
    return fragmentsCachePerUrl;
  }
  
  private synchronized static void clearReferenceCache(String url) {
    referenceCache.remove(url);
  }
  
  private synchronized static void clearFragmentCache(String url) {
    fragmentsCache.remove(url);
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
