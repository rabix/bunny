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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((checksum == null) ? 0 : checksum.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    result = prime * result + ((properties == null) ? 0 : properties.hashCode());
    result = prime * result + ((secondaryFiles == null) ? 0 : secondaryFiles.hashCode());
    result = prime * result + ((size == null) ? 0 : size.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FileValue other = (FileValue) obj;
    if (checksum == null) {
      if (other.checksum != null)
        return false;
    } else if (!checksum.equals(other.checksum))
      return false;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.path))
      return false;
    if (properties == null) {
      if (other.properties != null)
        return false;
    } else if (!properties.equals(other.properties))
      return false;
    if (secondaryFiles == null) {
      if (other.secondaryFiles != null)
        return false;
    } else if (!secondaryFiles.equals(other.secondaryFiles))
      return false;
    if (size == null) {
      if (other.size != null)
        return false;
    } else if (!size.equals(other.size))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "FileValue [size=" + size + ", path=" + path + ", location=" + location + ", checksum=" + checksum + ", secondaryFiles=" + secondaryFiles + ", properties=" + properties + "]";
  }
  
}
