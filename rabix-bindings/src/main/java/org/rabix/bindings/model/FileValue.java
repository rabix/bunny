package org.rabix.bindings.model;

import java.util.List;
import java.util.Map;

public class FileValue {

  protected final Long size;
  protected final String path;
  protected final String location;
  
  protected final String name;
  protected final String checksum;
  protected final List<FileValue> secondaryFiles;
  protected final Map<String, Object> properties;
  
  public FileValue(Long size, String path, String location, String checksum, List<FileValue> secondaryFiles, Map<String, Object> properties, String name) {
    super();
    this.size = size;
    this.path = path;
    this.name = name;
    this.location = location;
    this.checksum = checksum;
    this.secondaryFiles = secondaryFiles;
    this.properties = properties;
  }
  
  public static FileValue cloneWithPath(FileValue fileValue, String path) {
    return new FileValue(fileValue.size, path, fileValue.location, fileValue.checksum, fileValue.secondaryFiles, fileValue.properties, fileValue.name);
  }
  
  public static FileValue cloneWithProperties(FileValue fileValue, Map<String, Object> properties) {
    return new FileValue(fileValue.size, fileValue.path, fileValue.location, fileValue.checksum, fileValue.secondaryFiles, properties, fileValue.name);
  }
  
  public static FileValue cloneWithSecondaryFiles(FileValue fileValue, List<FileValue> secondaryFiles) {
    return new FileValue(fileValue.size, fileValue.path, fileValue.location, fileValue.checksum, secondaryFiles, fileValue.properties, fileValue.name);
  }

  public Long getSize() {
    return size;
  }

  public String getPath() {
    return path;
  }
  
  public String getName() {
    return name;
  }
  
  public String getLocation() {
    return location;
  }
  
  public String getChecksum() {
    return checksum;
  }

  public List<FileValue> getSecondaryFiles() {
    return secondaryFiles;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FileValue fileValue = (FileValue) o;

    if (size != null ? !size.equals(fileValue.size) : fileValue.size != null) return false;
    if (path != null ? !path.equals(fileValue.path) : fileValue.path != null) return false;
    if (location != null ? !location.equals(fileValue.location) : fileValue.location != null) return false;
    if (name != null ? !name.equals(fileValue.name) : fileValue.name != null) return false;
    if (checksum != null ? !checksum.equals(fileValue.checksum) : fileValue.checksum != null) return false;
    if (secondaryFiles != null ? !secondaryFiles.equals(fileValue.secondaryFiles) : fileValue.secondaryFiles != null)
      return false;
    return properties != null ? properties.equals(fileValue.properties) : fileValue.properties == null;

  }

  @Override
  public int hashCode() {
    int result = size != null ? size.hashCode() : 0;
    result = 31 * result + (path != null ? path.hashCode() : 0);
    result = 31 * result + (location != null ? location.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "FileValue [size=" + size + ", path=" + path + ", location=" + location + ", checksum=" + checksum + ", secondaryFiles=" + secondaryFiles + ", properties=" + properties + "]";
  }
  
}
