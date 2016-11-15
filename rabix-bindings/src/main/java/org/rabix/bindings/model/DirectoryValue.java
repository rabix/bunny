package org.rabix.bindings.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DirectoryValue extends FileValue {

  /**
   * 
   */
  private static final long serialVersionUID = -9220350014933846210L;
  
  @JsonProperty("listing")
  private List<FileValue> listing;
  
  public DirectoryValue(Long size, String path, String location, String checksum, List<FileValue> listing, List<FileValue> secondaryFiles, Map<String, Object> properties, String name, String format) {
    super(size, path, location, checksum, secondaryFiles, properties, name, format, null);
    this.listing = listing;
  }
  
  @JsonCreator
  public DirectoryValue(@JsonProperty("size") Long size, @JsonProperty("path") String path,
      @JsonProperty("location") String location, @JsonProperty("name") String name,
      @JsonProperty("dirname") String dirname, @JsonProperty("nameroot") String nameroot,
      @JsonProperty("nameext") String nameext, @JsonProperty("contents") String contents,
      @JsonProperty("checksum") String checksum, @JsonProperty("secondaryFiles") List<FileValue> secondaryFiles,
      @JsonProperty("properties") Map<String, Object> properties, @JsonProperty("listing") List<FileValue> listing,
      @JsonProperty("format") String format) {
    super(size, path, location, name, dirname, nameroot, nameext, contents, checksum, secondaryFiles, properties, format);
    this.listing = listing;
  }

  public static DirectoryValue cloneWithPath(DirectoryValue fileValue, String path) {
    return new DirectoryValue(fileValue.size, path, fileValue.location, fileValue.name, fileValue.dirname,
        fileValue.nameroot, fileValue.nameext, fileValue.contents, fileValue.checksum, fileValue.secondaryFiles,
        fileValue.properties, fileValue.listing, fileValue.format);
  }

  public static DirectoryValue cloneWithSecondaryFiles(DirectoryValue fileValue, List<FileValue> secondaryFiles) {
    return new DirectoryValue(fileValue.size, fileValue.path, fileValue.location, fileValue.name, fileValue.dirname,
        fileValue.nameroot, fileValue.nameext, fileValue.contents, fileValue.checksum, secondaryFiles,
        fileValue.properties, fileValue.listing, fileValue.format);
  }
  
  public List<FileValue> getListing() {
    return listing;
  }
  
  @JsonProperty("$type")
  public FileType getType() {
    return FileType.Directory;
  }

  @SuppressWarnings("unchecked")
  public static boolean isDirectoryValue(Object value) {
    if (value == null) {
      return false;
    }
    if (value instanceof Map<?, ?>) {
      return ((Map<String, Object>) value).containsKey("$type") && ((Map<String, Object>) value).get("$type").equals(FileType.Directory.toString());
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public static FileValue fromMap(Object value) {
    if (isFileValue(value)) {
      return FileValue.fromMap(value);
    }
    if (!isDirectoryValue(value)) {
      return null;
    }
    Map<String, Object> map = (Map<String, Object>) value;
    Long size = map.get("size") != null ? new Long((Integer) map.get("size")) : null;
    String path = (String) map.get("path");
    String location = (String) map.get("location");
    String name = (String) map.get("name");
    String format = (String) map.get("format");
    String dirname = (String) map.get("dirname");
    String nameroot = (String) map.get("nameroot");
    String nameext = (String) map.get("nameext");
    String contents = (String) map.get("contents");
    String checksum = (String) map.get("checksum");
    Map<String, Object> properties = (Map<String, Object>) map.get("properties");
    List<FileValue> secondaryFiles = new ArrayList<>();
    
    if (map.containsKey("secondaryFiles")) {
      for (Map<String, Object> secondaryFile : (List<Map<String,Object>>)map.get("secondaryFiles")) {
        secondaryFiles.add(fromMap(secondaryFile));
      }
    }
    List<FileValue> listing = null;
    if (map.containsKey("listing")) {
      listing = new ArrayList<>();
      for (Map<String, Object> listingObj : (List<Map<String, Object>>) map.get("listing")) {
        listing.add(fromMap(listingObj));
      }
    }
    return new DirectoryValue(size, path, location, name, dirname, nameroot, nameext, contents, checksum, secondaryFiles, properties, listing, format);
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
