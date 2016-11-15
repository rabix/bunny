package org.rabix.bindings.cwl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.cwl.helper.CWLDirectoryValueHelper;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;

public class CWLValueTranslator {

  @SuppressWarnings("unchecked")
  public static Object translateToSpecific(Object commonValue) {
    if (commonValue == null) {
      return null;
    }
    if (commonValue instanceof List<?>) {
      List<Object> list = new ArrayList<>();
      for (Object singleCommonValue : (List<?>) commonValue) {
        list.add(translateToSpecific(singleCommonValue));
      }
      return list;
    }
    if (commonValue instanceof Map<?, ?>) {
      Map<String, Object> map = new HashMap<>();
      for (Entry<String, Object> entry : ((Map<String, Object>) commonValue).entrySet()) {
        map.put(entry.getKey(), translateToSpecific(entry.getValue()));
      }
      return map;
    }
    if (commonValue instanceof DirectoryValue) {
      return CWLDirectoryValueHelper.createDirectoryRaw((DirectoryValue) commonValue);
    }
    if (commonValue instanceof FileValue) {
      return CWLFileValueHelper.createFileRaw((FileValue) commonValue);
    }
    return commonValue;
  }

  @SuppressWarnings("unchecked")
  public static Object translateToCommon(Object nativeValue) {
    if (nativeValue == null) {
      return null;
    }
    if (CWLSchemaHelper.isDirectoryFromValue(nativeValue)) {
      return CWLDirectoryValueHelper.createDirectoryValue(nativeValue);
    }
    if (CWLSchemaHelper.isFileFromValue(nativeValue)) {
      return CWLFileValueHelper.createFileValue(nativeValue);
    }
    if (nativeValue instanceof List<?>) {
      List<Object> list = new ArrayList<>();
      for (Object singleNativeValue : (List<?>) nativeValue) {
        list.add(translateToCommon(singleNativeValue));
      }
      return list;
    }
    if (nativeValue instanceof Map<?, ?>) {
      Map<String, Object> map = new HashMap<>();
      for (Entry<String, Object> entry : ((Map<String, Object>) nativeValue).entrySet()) {
        map.put(entry.getKey(), translateToCommon(entry.getValue()));
      }
      return map;
    }
    return nativeValue;
  }

}
