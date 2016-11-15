package org.rabix.bindings.draft3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;

public class Draft3ValueTranslator {

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
      return Draft3FileValueHelper.createFileRaw((FileValue) commonValue);
    }
    return commonValue;
  }

  @SuppressWarnings("unchecked")
  public static Object translateToCommon(Object nativeValue) throws BindingException {
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
    if (Draft3SchemaHelper.isFileFromValue(nativeValue)) {
      return Draft3FileValueHelper.createFileValue(nativeValue);
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
