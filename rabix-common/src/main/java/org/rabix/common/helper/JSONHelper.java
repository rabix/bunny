package org.rabix.common.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

public class JSONHelper {

  public static final ObjectMapper mapper = new ObjectMapper();
  public static final ObjectMapper mapperWithoutIdentation = new ObjectMapper();
  
  static {
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    mapperWithoutIdentation.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
  }

  public static String transformToJSON(String data) {
    try {
      Yaml reader = new Yaml();
      return writeObject(reader.load(data));
    } catch (Exception e) {
      // it's not YAML (or it's not valid)
    }
    return data;
  }
  
  @SuppressWarnings("unchecked")
  public static Map<String, Object> readMap(String json) {
    return readObject(json, (Class<Map<String, Object>>) (Class<?>) Map.class);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> readMap(JsonNode node) {
    return readObject(node, (Class<Map<String, Object>>) (Class<?>) Map.class);
  }

  @SuppressWarnings("unchecked")
  public static <T> Map<String, T> readMap(String json, Class<T> valueClass) {
    Map<String, Object> untypedMap = readMap(json);
    if (String.class.equals(valueClass) || List.class.equals(valueClass) || valueClass.isPrimitive() || ClassUtils.wrapperToPrimitive(valueClass) != null) {
      return (Map<String, T>) untypedMap;
    }
    Map<String, Map<String, Object>> mapOfMaps = (Map<String, Map<String, Object>>) (Map<?, ?>) untypedMap;
    Map<String, T> typedMap = new HashMap<String, T>();
    for (Map.Entry<String, Map<String, Object>> entry : mapOfMaps.entrySet()) {
      T converted = null;
      if (entry.getValue() != null) {
        if (valueClass.isInstance(entry.getValue())) {
          converted = (T) entry.getValue();
        } else {
          converted = JSONHelper.convertToObject(entry.getValue(), valueClass);
        }
      }
      typedMap.put(entry.getKey(), converted);
    }
    return typedMap;
  }

  public static <T> Map<String, T> readMap(JsonNode node, Class<T> valueClass) {
    return readMap(writeObject(node), valueClass);
  }

  public static <T> T readObject(String json, Class<T> clazz) {
    if (json == null) {
      return null;
    }
    try {
      return mapper.readValue(json, clazz);
    } catch (IOException e) {
      throw new IllegalStateException("JSON: " + json, e);
    }
  }

  public static <T> T readObject(String json, TypeReference<T> valueTypeRef) {
    try {
      return mapper.readValue(json, valueTypeRef);
    } catch (IOException e) {
      throw new IllegalStateException("JSON: " + json, e);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> T readObject(String json, Class<T> clazz, String... path) {
    Map<String, Object> map = JSONHelper.readMap(json);
    Object ret = map;
    for (int i = 0; i < path.length; i++) {
      ret = ((Map) ret).get(path[i]);
      if (ret == null) {
        break;
      }
    }
    return (T) ret;
  }

  public static <T> T readObject(JsonNode node, Class<T> clazz) {
    try {
      return mapper.treeToValue(node, clazz);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static String writeObject(Object value) {
    try {
      return mapper.writeValueAsString(value);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static JsonNode readJsonNode(String json) {
    try {
      return mapper.readTree(json);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static JsonNode convertToJsonNode(Object value) {
    if (value == null) {
      return null;
    }
    return mapper.valueToTree(value);
  }

  public static <T> T convertToObject(Map<String, ?> jsonObject, Class<T> clazz) {
    if (jsonObject == null) {
      return null;
    }
    JsonNode node = JSONHelper.convertToJsonNode(jsonObject);
    return JSONHelper.readObject(node, clazz);
  }

  @SuppressWarnings("unchecked")
  public static <T> Map<String, T> convertToMap(Object value) {
    if (value == null) {
      return null;
    }
    JsonNode node = JSONHelper.convertToJsonNode(value);
    return readObject(node, Map.class);
  }
  
  @SuppressWarnings("unchecked")
  public static <T> List<T> convertToList(Object value) {
    if (value == null) {
      return null;
    }
    JsonNode node = JSONHelper.convertToJsonNode(value);
    return readObject(node, List.class);
  }
  
  public static String writeSortedWithoutIdentation(final JsonNode node) {
    try {
      final Object obj = mapperWithoutIdentation.treeToValue(node, Object.class);
      return mapperWithoutIdentation.writeValueAsString(obj);
    } catch (IOException e) {
      throw new IllegalStateException("JSON: " + node, e);
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
  
  /**
   * Use Jackson for transformation
   */
  public static Object transformPreserveNull(JsonNode node) {
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
        Object result = transformPreserveNull(subnodeEntry.getValue());
        resultMap.put(subnodeEntry.getKey(), result);
      }
      return resultMap;
    }
    return null;
  }

}
