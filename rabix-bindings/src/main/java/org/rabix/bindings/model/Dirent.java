package org.rabix.bindings.model;

public class Dirent {

  private final String entry;
  private final Object entryname;
  private final Boolean writable;
  
  public Dirent(String entry, Object entryname, Boolean writable) {
    this.entry = entry;
    this.entryname = entryname;
    this.writable = writable;
  }
  
}
