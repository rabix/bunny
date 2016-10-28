package org.rabix.bindings.model;

import java.util.List;
import java.util.Map;

public class DirectoryValue extends FileValue {

  private final List<FileValue> listing;
  
  public DirectoryValue(Long size, String path, String location, String checksum, List<FileValue> listing, List<FileValue> secondaryFiles, Map<String, Object> properties, String name) {
    super(size, path, location, checksum, secondaryFiles, properties, name);
    this.listing = listing;
  }

  public List<FileValue> getListing() {
    return listing;
  }

  @Override
  public String toString() {
    return "DirectoryValue [listing=" + listing + ", size=" + size + ", path=" + path + ", location=" + location + ", checksum=" + checksum + ", secondaryFiles=" + secondaryFiles + ", properties=" + properties + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    DirectoryValue that = (DirectoryValue) o;

    return listing != null ? listing.equals(that.listing) : that.listing == null;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (listing != null ? listing.hashCode() : 0);
    return result;
  }
}
