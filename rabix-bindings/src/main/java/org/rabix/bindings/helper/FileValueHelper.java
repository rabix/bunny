package org.rabix.bindings.helper;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.model.DataType;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.transformer.FileTransformer;
import org.rabix.common.helper.CloneHelper;

import java.util.*;

public class FileValueHelper {

  /**
   * Creates copy of value (in common format) in which all FileValues are updated using fileTransformer
   * @param value
   * @param fileTransformer
   * @return copy of value with replaced FileValues
   */
  public static Object updateFileValues(Object value, FileTransformer fileTransformer) {
    if (value instanceof FileValue) {
      FileValue origFile = (FileValue) value;
      return fileTransformer.transform(origFile);
    } else if (value instanceof List) {
      List<Object> ret = new ArrayList<>();
      for (Object o : (List<?>) value) {
        ret.add(updateFileValues(o, fileTransformer));
      }
      return ret;
    } else if (value instanceof Map) {
      Map<Object, Object> ret = new HashMap<>();
      for (Object key : ((Map<?, ?>) value).keySet()) {
        ret.put(key, updateFileValues(((Map<?, ?>) value).get(key), fileTransformer));
      }
      return ret;
    }
    return CloneHelper.deepCopy(value);
  }

  /**
   * Parses value (in common format) and extracts all FileValue objects
   * @param value
   * @return List of FileValue objects
   */
  public static List<FileValue> getFilesFromValue(Object value) {
    List<FileValue> ret = new ArrayList<>();
    if (value instanceof List) {
      for (Object o : (List<?>) value) {
        ret.addAll(getFilesFromValue(o));
      }
    } else if (value instanceof FileValue) {
      ret.add((FileValue)value);
    } else if (value instanceof Map) {
      for (Object key : ((Map<?, ?>) value).keySet()) {
        ret.addAll(getFilesFromValue(((Map<?, ?>) value).get(key)));
      }
    }
    return ret;
  }

  /**
   * Reads the type of input value (in common format)
   * @param value
   * @return DataType object that represents value's type
   */
  public static DataType getDataTypeFromValue(Object value) {
    if (value==null)
      return new DataType(DataType.Type.ANY);

    // FILE
    if (value instanceof FileValue)
      return new DataType(DataType.Type.FILE);

    //ARRAY
    if (value instanceof List) {
      DataType arrayType = getDataTypeFromValue(((List<?>)value).get(0));
      return new DataType(DataType.Type.ARRAY, arrayType);
    }

    // RECORD
    if (value instanceof Map) {
      Map<String, DataType> subTypes = new HashMap<>();
      Map<?, ?> valueMap = (Map<?, ?>) value;
      for (Object key: valueMap.keySet()) {
        subTypes.put((String)key, getDataTypeFromValue(valueMap.get(key)));
      }
      return new DataType(DataType.Type.RECORD, subTypes);
    }

    // PRIMITIVE
    for (DataType.Type t : DataType.Type.values()) {
      if (t.primitiveType !=null && t.primitiveType.isInstance(value))
        return new DataType(t);
    }

    return new DataType(DataType.Type.ANY);
  }

  public static Map<String, Object> translateFileToCommon(Bindings bindings, FileValue file) {
    try {
      return (Map<String, Object>) bindings.translateToCommon(file);
    } catch (BindingException e) {
      e.printStackTrace();
      return null;
    }
  }

}
