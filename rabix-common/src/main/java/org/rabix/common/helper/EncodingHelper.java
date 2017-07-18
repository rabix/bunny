package org.rabix.common.helper;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

public class EncodingHelper {

  public static Charset DEFAULT_ENCODING = Charset.forName("UTF-8");

  public static final String SHELL_CHARS_NEEDS_QUOTING = "|&;<>()$`\\ \"'\t\r\n*?[#~";
  public static final Pattern NEEDS_QUOTING_PATTERN = Pattern.compile("[" + Pattern.quote(SHELL_CHARS_NEEDS_QUOTING) + "]");

  public static String encodeBase64(String data) {
    return Base64.encodeBase64String(data.getBytes(DEFAULT_ENCODING));
  }

  public static String decodeBase64(String data) {
    byte[] dataBytes = Base64.decodeBase64(data);
    return new String(dataBytes, DEFAULT_ENCODING);
  }
  
  public static String shellQuote(String argument) {
    if (!NEEDS_QUOTING_PATTERN.matcher(argument).find() && !argument.equals("")) {
      return argument;
    }
    return "'" + argument.replace("'", "'\\''") + "'";
  }

}
