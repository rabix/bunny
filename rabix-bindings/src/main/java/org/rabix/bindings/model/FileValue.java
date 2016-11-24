package org.rabix.bindings.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "$type")
@JsonSubTypes({ @Type(value = FileValue.class, name = "File"),
    @Type(value = DirectoryValue.class, name = "Directory") })
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileValue implements Serializable {

  public static enum FileType {
    File, Directory
  }
  
  /**
   * 
   */
  private static final long serialVersionUID = 8722098821128237856L;

  @JsonProperty("size")
  protected Long size;
  @JsonProperty("path")
  protected String path;
  @JsonProperty("location")
  protected String location;

  @JsonProperty("name")
  protected String name;
  @JsonProperty("dirname")
  protected String dirname;
  @JsonProperty("nameroot")
  protected String nameroot;
  @JsonProperty("nameext")
  protected String nameext;
  @JsonProperty("contents")
  protected String contents;
  @JsonProperty("format")
  protected String format;
  
  @JsonProperty("checksum")
  protected String checksum;
  @JsonProperty("secondaryFiles")
  protected List<FileValue> secondaryFiles;
  @JsonProperty("properties")
  protected Map<String, Object> properties;

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
  
  public FileValue(Long size, String path, String location, String checksum, List<FileValue> secondaryFiles, Map<String, Object> properties, String name, String format, String contents) {
    this(size, path, location, checksum, secondaryFiles, properties, name);
    this.format = format;
    this.contents = contents;
  }

  @JsonCreator
  public FileValue(@JsonProperty("size") Long size, @JsonProperty("path") String path,
      @JsonProperty("location") String location, @JsonProperty("name") String name,
      @JsonProperty("dirname") String dirname, @JsonProperty("nameroot") String nameroot,
      @JsonProperty("nameext") String nameext, @JsonProperty("contents") String contents,
      @JsonProperty("checksum") String checksum, @JsonProperty("secondaryFiles") List<FileValue> secondaryFiles,
      @JsonProperty("properties") Map<String, Object> properties, @JsonProperty("format") String format) {
    super();
    this.size = size;
    this.path = path;
    this.location = location;
    this.name = name;
    this.format = format;
    this.dirname = dirname;
    this.nameroot = nameroot;
    this.nameext = nameext;
    this.contents = contents;
    this.checksum = checksum;
    this.secondaryFiles = secondaryFiles;
    this.properties = properties;
  }

  public static FileValue cloneWithPath(FileValue fileValue, String path) {
    return new FileValue(fileValue.size, path, fileValue.location, fileValue.name, fileValue.dirname, fileValue.nameroot, fileValue.nameext, fileValue.contents, fileValue.checksum, fileValue.secondaryFiles,
        fileValue.properties, fileValue.format);
  }

  public static FileValue cloneWithProperties(FileValue fileValue, Map<String, Object> properties) {
    return new FileValue(fileValue.size, fileValue.path, fileValue.location, fileValue.name, fileValue.dirname, fileValue.nameroot, fileValue.nameext, fileValue.contents, fileValue.checksum, fileValue.secondaryFiles,
        properties, fileValue.format);
  }

  public static FileValue cloneWithSecondaryFiles(FileValue fileValue, List<FileValue> secondaryFiles) {
    return new FileValue(fileValue.size, fileValue.path, fileValue.location, fileValue.name, fileValue.dirname, fileValue.nameroot, fileValue.nameext, fileValue.contents, fileValue.checksum, secondaryFiles,
        fileValue.properties, fileValue.format);
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDirname() {
    return dirname;
  }

  public void setDirname(String dirname) {
    this.dirname = dirname;
  }

  public String getNameroot() {
    return nameroot;
  }

  public void setNameroot(String nameroot) {
    this.nameroot = nameroot;
  }

  public String getNameext() {
    return nameext;
  }

  public void setNameext(String nameext) {
    this.nameext = nameext;
  }

  public String getContents() {
    return contents;
  }

  public void setContents(String contents) {
    this.contents = contents;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getChecksum() {
    return checksum;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }

  public List<FileValue> getSecondaryFiles() {
    return secondaryFiles;
  }

  public void setSecondaryFiles(List<FileValue> secondaryFiles) {
    this.secondaryFiles = secondaryFiles;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  @JsonProperty("$type")
  public FileType getType() {
    return FileType.File;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    FileValue fileValue = (FileValue) o;

    if (size != null ? !size.equals(fileValue.size) : fileValue.size != null)
      return false;
    if (path != null ? !path.equals(fileValue.path) : fileValue.path != null)
      return false;
    if (location != null ? !location.equals(fileValue.location) : fileValue.location != null)
      return false;
    if (name != null ? !name.equals(fileValue.name) : fileValue.name != null)
      return false;
    if (checksum != null ? !checksum.equals(fileValue.checksum) : fileValue.checksum != null)
      return false;
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
    return "FileValue [size=" + size + ", path=" + path + ", location=" + location + ", checksum=" + checksum
        + ", secondaryFiles=" + secondaryFiles + ", properties=" + properties + "]";
  }

  @SuppressWarnings("unchecked")
  public static boolean isFileValue(Object value) {
    if (value == null) {
      return false;
    }
    if (value instanceof Map<?, ?>) {
      return ((Map<String, Object>) value).containsKey("$type") && ((Map<String, Object>) value).get("$type").equals(FileType.File.toString());
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public static FileValue fromMap(Object value) {
    if (!isFileValue(value)) {
      return null;
    }
    Map<String, Object> map = (Map<String, Object>) value;
    
    Long size = null;
    Number sizeFromMap = (Number) map.get("size");
    if (sizeFromMap instanceof Integer) {
      size = new Long((Integer) sizeFromMap);
    } else {
      size = (Long) sizeFromMap;
    }
    String path = (String) map.get("path");
    String format = (String) map.get("format");
    String location = (String) map.get("location");
    String name = (String) map.get("name");
    String dirname = (String) map.get("dirname");
    String nameroot = (String) map.get("nameroot");
    String nameext = (String) map.get("nameext");
    String contents = (String) map.get("contents");
    String checksum = (String) map.get("checksum");
    Map<String, Object> properties = (Map<String, Object>) map.get("properties");
    
    List<FileValue> secondaryFiles = null;
    if (map.containsKey("secondaryFiles")) {
      secondaryFiles = new ArrayList<>();
      for (Map<String, Object> secondaryFile : (List<Map<String, Object>>) map.get("secondaryFiles")) {
        if (DirectoryValue.isDirectoryValue(secondaryFile)) {
          secondaryFiles.add(DirectoryValue.fromMap(secondaryFile));
        } else {
          secondaryFiles.add(fromMap(secondaryFile));
        }
      }
    }
    return new FileValue(size, path, location, name, dirname, nameroot, nameext, contents, checksum, secondaryFiles, properties, format);
  }
  
  @SuppressWarnings("unchecked")
  public static Object deserialize(Object value) {
    if (value instanceof Map<?, ?>) {
      if (DirectoryValue.isDirectoryValue(value)) {
        return DirectoryValue.fromMap(value);
      }
      if (FileValue.isFileValue(value)) {
        return FileValue.fromMap(value);
      }
      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        entry.setValue(deserialize(entry.getValue()));
      }
      return value;
    }
    if (value instanceof List<?>) {
      List<Object> newList = new ArrayList<>();
      for (Object item : (List<?>) value) {
        newList.add(deserialize(item));
      }
      return newList;
    }
    return value;
  }
  
}
