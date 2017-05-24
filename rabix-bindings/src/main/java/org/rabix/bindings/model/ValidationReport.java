package org.rabix.bindings.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by luka on 11.5.17..
 */
public class ValidationReport {

  public enum Severity {
    WARNING, ERROR
  }

  public static class Item {
    final String message;
    final Severity severity;

    public Item(String message, Severity severity) {
      this.message = message;
      this.severity = severity;
    }

    public String getMessage() {
      return message;
    }

    public Severity getSeverity() {
      return severity;
    }
  }

  private final List<Item> items;

  public ValidationReport() {
    items = Collections.emptyList();
  }

  public ValidationReport(List<String> errors, List<String> warnings) {
    items = new ArrayList<>();
    items.addAll(messagesToItems(errors, Severity.ERROR));
    items.addAll(messagesToItems(warnings, Severity.WARNING));
  }

  public ValidationReport(List<Item> items) {
    this.items = items;
  }

  public List<String> getMessagesWithSeverity(Severity s) {
    return items.stream().filter(item -> item.severity == s)
        .map(item -> item.message).collect(Collectors.toList());
  }

  public List<String> getErrors() {
    return getMessagesWithSeverity(Severity.ERROR);
  }

  public List<String> getWarnings() {
    return getMessagesWithSeverity(Severity.WARNING);
  }

  public List<Item> getItems() {
    return Collections.unmodifiableList(items);
  }

  public static List<Item> messagesToItems(List<String> messages, Severity severity) {
    return messages.stream()
        .map(message -> new ValidationReport.Item(message, severity))
        .collect(Collectors.toList());
  }

  public static Item error(String message) {
    return new Item(message, Severity.ERROR);
  }

  public static Item warning(String message) {
    return new Item(message, Severity.WARNING);
  }

  public static Item item(String message, Severity s) {
    return new Item(message, s);
  }

}
