package org.rabix.common.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class ResourceHelper {

  public static String readResource(String resourceName) throws IOException {
    return readResource(null, resourceName);
  }

  public static String readResource(Class<?> clazz, String resourceName) throws IOException {
    InputStream stream = getResourceAsStream(clazz, resourceName);
    if (stream == null) {
      throw new IllegalArgumentException("Resource not found - \"" + resourceName + "\"");
    }
    return toString(stream);
  }

  public static InputStream getResourceAsStream(Class<?> clazz, String resource) {
    return clazz == null ? ClassLoader.getSystemResourceAsStream(resource) : clazz.getResourceAsStream(resource);
  }

  public static String getResourcePath(Class<?> clazz, String fileName) {
    URL resource = clazz.getResource(fileName);
    return resource == null ? null : resource.getPath();
  }

  private static String toString(InputStream inputStream) throws IOException {
    InputStreamReader input = new InputStreamReader(inputStream);
    StringBuilder output = new StringBuilder();

    char[] buffer = new char[1024 * 4];
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.append(buffer, 0, n);
    }
    return output.toString();
  }

}
