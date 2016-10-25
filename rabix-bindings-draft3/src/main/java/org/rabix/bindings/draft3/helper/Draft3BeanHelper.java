package org.rabix.bindings.draft3.helper;

import java.util.List;
import java.util.Map;

public class Draft3BeanHelper {

  public static <T> T getValue(String key, Object raw) {
    return getValue(key, raw, null);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getValue(String key, Object raw, T defaultValue) {
    if (raw == null) {
      return null;
    }
    if (raw instanceof Map<?, ?>) {
      T value = (T) ((Map<?, ?>) raw).get(key);
      if (value != null) {
        return value;
      }
    }
    if (raw instanceof List<?>) {
      for (Object rawItem : (List<?>) raw) {
        T result = getValue(key, rawItem, defaultValue);
        if (result != null) {
          return result;
        }
      }
    }
    return defaultValue;
  }

  @SuppressWarnings("unchecked")
  public static void setValue(String key, Object value, Object raw) {
    if (raw == null) {
      return;
    }
    if (raw instanceof Map<?, ?>) {
      ((Map<String, Object>) raw).put(key, value);
    }
  }
  
}
