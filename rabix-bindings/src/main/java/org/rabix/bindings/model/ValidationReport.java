package org.rabix.bindings.model;

import java.util.Collections;
import java.util.List;

/**
 * Created by luka on 11.5.17..
 */
public class Validation {

  public enum Severity {
    WARNING, ERROR
  }

  public class Item {
    String message;
    Severity severity;

    public Item(String message, Severity severity) {
      this.message = message;
      this.severity = severity;
    }
  }

  private List<Item> messages;

  public Validation() {
    errors = Collections.emptyList();
    warnings = Collections.emptyList();
  }

  public Validation(List<String> errors, List<String> warnings) {
    this.errors = errors;
    this.warnings = warnings;
  }

  public List<String> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  public List<String> getWarnings() {
    return Collections.unmodifiableList(warnings);
  }

}
