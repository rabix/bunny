package org.rabix.bindings.draft2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;

public class Draft2ValueTranslator {

  @SuppressWarnings("unchecked")
  public static Object translateToSpecific(Object commonValue) throws BindingException {
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
      throw new BindingException("Failed to translate DirectoryValue " + commonValue);
    }
    if (commonValue instanceof FileValue) {
      return Draft2FileValueHelper.createFileRaw((FileValue) commonValue);
    }
    return commonValue;
  }

  @SuppressWarnings("unchecked")
  public static Object translateToCommon(Object nativeValue) {
    if (nativeValue == null) {
      return null;
    }
    if (nativeValue instanceof List<?>) {
      List<Object> list = new ArrayList<>();
      for (Object singleNativeValue : (List<?>) nativeValue) {
        list.add(translateToCommon(singleNativeValue));
      }
      return list;
    }
    if (Draft2SchemaHelper.isFileFromValue(nativeValue)) {
      return Draft2FileValueHelper.createFileValue(nativeValue);
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
