package org.rabix.common.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;

public class InternalSchemaHelper {

  public static final String ROOT_NAME = "root";
  public static final String SEPARATOR = ".";
  public static final String SLASH_SEPARATOR = "/";
  
  public static String concatenateIds(String id1, String id2) {
    return id1 + SEPARATOR + id2;
  }

  public static String scatterId(String id, int row) {
    return id + SEPARATOR + row;
  }

  public static String getJobIdFromScatteredId(String id) {
    return id.substring(0, id.lastIndexOf(SEPARATOR));
  }
  
  public static String getParentId(String id) {
    String result = null;
    String[] idParts = id.split("\\" + SEPARATOR);
    if (idParts.length == 1) {
      return result;
    }
    return id.substring(0, id.lastIndexOf(SEPARATOR));
  }

  public static String normalizeId(String id) {
    String result = id;
    String[] idParts = id.split("\\" + SEPARATOR);
    if (idParts.length == 0) {
      return result;
    }

    result = "";
    for (int index = 0; index < idParts.length; index++) {
      String idPart = idParts[index];
      try {
        Integer.parseInt(idPart);
      } catch (Exception e) {
        result += idPart + ((index != idParts.length - 1) ? SEPARATOR : "");
      }
    }
    if (result.endsWith(SEPARATOR)) {
      return result.substring(0, result.lastIndexOf(SEPARATOR));
    }
    return result;
  }

  public static String getLastPart(String id) {
    String[] idParts = id.split("\\" + SEPARATOR);
    if (idParts.length == 0) {
      return id;
    }
    return idParts[idParts.length - 1];
  }
  
  public static Integer getScatteredNumber(String id) {
    String lastPart = getLastPart(id);
    try {
      return Integer.parseInt(lastPart);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Use Jackson for transformation
   */
  public static Object transform(JsonNode node) {
    if (node instanceof NullNode) {
      return null;
    }
    if (node instanceof MissingNode) {
      return null;
    }
    if (node instanceof IntNode) {
      return ((IntNode) node).intValue();
    }
    if (node instanceof BigIntegerNode) {
      return ((BigIntegerNode) node).bigIntegerValue();
    }
    if (node instanceof BinaryNode) {
      return ((BinaryNode) node).binaryValue();
    }
    if (node instanceof BooleanNode) {
      return ((BooleanNode) node).booleanValue();
    }
    if (node instanceof DecimalNode) {
      return ((DecimalNode) node).decimalValue();
    }
    if (node instanceof DoubleNode) {
      return ((DoubleNode) node).doubleValue();
    }
    if (node instanceof LongNode) {
      return ((LongNode) node).longValue();
    }
    if (node instanceof NumericNode) {
      return ((NumericNode) node).numberValue();
    }
    if (node instanceof POJONode) {
      return ((POJONode) node).getPojo();
    }
    if (node instanceof TextNode) {
      return ((TextNode) node).textValue();
    }
    if (node instanceof ArrayNode) {
      List<Object> resultList = new ArrayList<>();
      for (JsonNode subnode : node) {
        Object result = transform(subnode);
        if (result != null) {
          resultList.add(result);
        }
      }
      return resultList;
    }
    if (node instanceof ObjectNode) {
      Map<String, Object> resultMap = new HashMap<String, Object>();
      Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();

      while (iterator.hasNext()) {
        Map.Entry<String, JsonNode> subnodeEntry = iterator.next();
        Object result = transform(subnodeEntry.getValue());
        if (result != null) {
          resultMap.put(subnodeEntry.getKey(), result);
        }
      }
      return resultMap;
    }
    return null;
  }

}
