package org.rabix.common.helper;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import org.apache.commons.codec.binary.Base64;

public class EncodingHelper {

  public static Charset DEFAULT_ENCODING = Charset.forName("UTF-8");

  public static final String SHELL_CHARS_NEEDS_QUOTING = "|&;<>()$`\\ \"'\t\r\n*?[#~";
  public static final Pattern NEEDS_QUOTING_PATTERN = Pattern.compile("[" + Pattern.quote(SHELL_CHARS_NEEDS_QUOTING) + "]");

  public static final Escaper SHELL_ESCAPE;
  static {
    final Escapers.Builder builder = Escapers.builder();
    builder.addEscape('$', "\\$");
    builder.addEscape('`', "\\`");
    builder.addEscape('\\', "\\\\");
    builder.addEscape('"', "\\\"");
    SHELL_ESCAPE = builder.build();
  }
  
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
    if (!NEEDS_QUOTING_PATTERN.matcher(argumentStr).find()) {
      return argumentStr;
    }
    return '"' + SHELL_ESCAPE.escape(argumentStr) + '"';
  }
  
}
