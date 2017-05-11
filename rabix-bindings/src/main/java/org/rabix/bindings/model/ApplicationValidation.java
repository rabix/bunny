package org.rabix.bindings.model;

import java.util.Collections;
import java.util.List;

/**
 * Created by luka on 11.5.17..
 */
public class ApplicationValidation {
  private List<String> errors;
  private List<String> warnings;

  public ApplicationValidation() {
    errors = Collections.emptyList();
    warnings = Collections.emptyList();
  }

  public ApplicationValidation(List<String> errors, List<String> warnings) {
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
