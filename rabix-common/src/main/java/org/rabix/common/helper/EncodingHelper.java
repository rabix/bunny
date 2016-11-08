package org.rabix.common.helper;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;

public class EncodingHelper {

  public static Charset DEFAULT_ENCODING = Charset.forName("UTF-8");
  
  public static String encodeBase64(String data) {
    return Base64.encodeBase64String(data.getBytes(DEFAULT_ENCODING));
  }

  public static String decodeBase64(String data) {
    byte[] dataBytes = Base64.decodeBase64(data);
    return new String(dataBytes, DEFAULT_ENCODING);
  }
  
  public static Object shellQuote(Object argument) {
    if (argument == null) {
      return null;
    }
    if (!(argument instanceof String)) {
      return argument;
    }
    String argumentStr = (String) argument;
    if (argumentStr.contains("'")) {
      argumentStr = argumentStr.replace("'", "\\'");
      return "'" + argumentStr + "'";
    }
    return argument;
  }
  
}
