package org.rabix.bindings.model;

import java.util.List;
import java.util.Map;

public class DirectoryValue extends FileValue {

  private final List<FileValue> listing;
  
  public DirectoryValue(Long size, String path, String location, String checksum, List<FileValue> listing, List<FileValue> secondaryFiles, Map<String, Object> properties) {
    super(size, path, location, checksum, secondaryFiles, properties);
    this.listing = listing;
  }

  public List<FileValue> getListing() {
    return listing;
  }

  @Override
  public String toString() {
    return "DirectoryValue [listing=" + listing + ", size=" + size + ", path=" + path + ", location=" + location + ", checksum=" + checksum + ", secondaryFiles=" + secondaryFiles + ", properties=" + properties + "]";
  }
  
}
