package org.rabix.common.jvm;

import java.util.HashSet;
import java.util.Set;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

public class ClasspathScanner {

  private static FastClasspathScanner scanner = new FastClasspathScanner("org.rabix");

  @SuppressWarnings("unchecked")
  public static <T> Set<Class<T>> scanInterfaceImplementations(Class<T> interfaceClass) {
    Set<Class<T>> matched = new HashSet<>();
    scanner.matchClassesImplementing(interfaceClass, c -> matched.add((Class<T>) c)).scan();
    return matched;
  }

  @SuppressWarnings("unchecked")
  public static <T> Set<Class<T>> scanSubclasses(Class<T> parentClass) {
    Set<Class<T>> matched = new HashSet<>();
    scanner.matchSubclassesOf(parentClass, c -> matched.add((Class<T>) c)).scan();
    return matched;
  }

}
