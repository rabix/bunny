package org.rabix.bindings.cwl.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;

public class CWLDirectoryValueHelper extends CWLBeanHelper {

  private static final String KEY_NAME = "basename";
  private static final String KEY_PATH = "path";
  private static final String KEY_NAMEROOT = "nameroot";
  private static final String KEY_NAMEEXT = "nameext";
  private static final String KEY_LOCATION = "location";
  private static final String KEY_SIZE = "size";
  private static final String KEY_FORMAT = "format";
  private static final String KEY_CHECKSUM = "checksum";
  private static final String KEY_METADATA = "metadata";
  private static final String KEY_SECONDARY_FILES = "secondaryFiles";
  
  private static final String KEY_LISTING = "listing";
  
  public static void setNameroot(String nameroot, Object raw) {
    setValue(KEY_NAMEROOT, nameroot, raw);
  }
  
  public static void setNameext(String nameext, Object raw) {
    setValue(KEY_NAMEEXT, nameext, raw);
  }
  
  public static void setDirectoryType(Object raw) {
    setValue(CWLSchemaHelper.KEY_JOB_TYPE, CWLSchemaHelper.TYPE_JOB_DIRECTORY, raw);
  }

  public static String getFormat(Object raw) {
    return getValue(KEY_FORMAT, raw);
  }
  
  public static void setFormat(String format, Object raw) {
    setValue(KEY_FORMAT, format, raw);
  }
  
  public static String getName(Object raw) {
    return getValue(KEY_NAME, raw);
  }

  public static void setName(String name, Object raw) {
    setValue(KEY_NAME, name, raw);
  }
  
  public static List<Object> getListing(Object raw) {
    return getValue(KEY_LISTING, raw);
  }

  public static void setListing(List<Object> listing, Object raw) {
    setValue(KEY_LISTING, listing, raw);
  }

  public static void setSize(Long size, Object raw) {
    setValue(KEY_SIZE, size, raw);
  }

  public static Long getSize(Object raw) {
    Object number = getValue(KEY_SIZE, raw);
    if (number == null) {
      return null;
    }
    if (number instanceof Integer) {
      return new Long(number.toString());
    }
    return (Long) number;
  }

  public static void setChecksum(File file, Object raw, HashAlgorithm hashAlgorithm) {
    if (!file.exists()) {
      throw new RuntimeException("Missing file " + file);
    }
    String checksum = ChecksumHelper.checksum(file, hashAlgorithm);
    if (checksum != null) {
      setValue(KEY_CHECKSUM, checksum, raw);
    }
  }

  public static String getChecksum(Object raw) {
    return getValue(KEY_CHECKSUM, raw);
  }

  public static String getPath(Object raw) {
    String path = getValue(KEY_PATH, raw);
    if (path == null) {
      path = URIHelper.getURIInfo((String) getValue(KEY_LOCATION, raw));
      if (path == null) {
        path = getValue(KEY_LOCATION, raw);
      }
      setPath(path, raw);
    }
    return path;
  }

  public static void setPath(String path, Object raw) {
    setValue(KEY_PATH, path, raw);
    setLocation(path, raw);
  }
  
  public static String getLocation(Object raw) {
    return getValue(KEY_LOCATION, raw);
  }

  public static void setLocation(String location, Object raw) {
    setValue(KEY_LOCATION, location, raw);
  }
  
  public static void setMetadata(Object metadata, Object raw) {
    setValue(KEY_METADATA, metadata, raw);
  }
  
  public static Map<String, Object> getMetadata(Object raw) {
    return getValue(KEY_METADATA, raw);
  }
  
  public static void setSecondaryFiles(List<?> secondaryFiles, Object raw) {
    setValue(KEY_SECONDARY_FILES, secondaryFiles, raw);
  }

  public static List<Map<String, Object>> getSecondaryFiles(Object raw) {
    return getValue(KEY_SECONDARY_FILES, raw);
  }
  
  /**
   * Creates {@link DirectoryValue} from Directory object
   * TODO: discuss checksum
   * 
   * @param value   Directory object
   * @return        DirectoryValue object
   */
  public static DirectoryValue createDirectoryValue(Object value) {
    String path = CWLFileValueHelper.getPath(value);
    String format = CWLFileValueHelper.getFormat(value);
    String name = CWLFileValueHelper.getName(value);
    String location = CWLFileValueHelper.getLocation(value);
    Long size = CWLFileValueHelper.getSize(value);
    
    Map<String, Object> properties = new HashMap<>();
    properties.put(CWLBindingHelper.KEY_SBG_METADATA, CWLFileValueHelper.getMetadata(value));

    List<FileValue> secondaryFiles = new ArrayList<>();
    List<Map<String, Object>> secondaryFileValues = CWLFileValueHelper.getSecondaryFiles(value);
    if (secondaryFileValues != null) {
      for (Map<String, Object> secondaryFileValue : secondaryFileValues) {
        if (CWLSchemaHelper.isFileFromValue(secondaryFileValue)) {
          secondaryFiles.add(CWLFileValueHelper.createFileValue(secondaryFileValue));
          continue;
        }
        if (CWLSchemaHelper.isDirectoryFromValue(secondaryFileValue)) {
          secondaryFiles.add(createDirectoryValue(secondaryFileValue));
          continue;
        }
      }
    }
    
    List<Object> listing = getListing(value);
    List<FileValue> listingFileValues = new ArrayList<>();
    if (listing != null) {
      for (Object listingObj : listing) {
        if (CWLSchemaHelper.isFileFromValue(listingObj)) {
          listingFileValues.add(CWLFileValueHelper.createFileValue(listingObj));
          continue;
        }
        if (CWLSchemaHelper.isDirectoryFromValue(listingObj)) {
          listingFileValues.add(createDirectoryValue(listingObj));
          continue;
        }
      }
    }
    return new DirectoryValue(size, path, location, null, listingFileValues, secondaryFiles, properties, name, format);
  }

  public static Map<String, Object> createDirectoryRaw(DirectoryValue fileValue) {
    Map<String, Object> raw = new HashMap<>();
    
    setDirectoryType(raw);
    setPath(fileValue.getPath(), raw);
    setName(fileValue.getName(), raw);
    setSize(fileValue.getSize(), raw);
    setFormat(fileValue.getFormat(), raw);
    
    Map<String, Object> properties = fileValue.getProperties();
    if (properties != null) {
      setMetadata(properties.get(CWLBindingHelper.KEY_SBG_METADATA), raw);
    }
    
    List<FileValue> secondaryFileValues = fileValue.getSecondaryFiles();
    if (secondaryFileValues != null) {
      List<Map<String, Object>> secondaryFilesRaw = new ArrayList<>();
      for (FileValue secondaryFileValue : secondaryFileValues) {
        if (secondaryFileValue instanceof DirectoryValue) {
          secondaryFilesRaw.add(createDirectoryRaw((DirectoryValue) secondaryFileValue));
        } else {
          secondaryFilesRaw.add(CWLFileValueHelper.createFileRaw(secondaryFileValue));          
        }
      }
      setSecondaryFiles(secondaryFilesRaw, raw);
    }
    
    List<FileValue> listingFiles = fileValue.getListing();
    if (listingFiles != null) {
      List<Object> listingRaw = new ArrayList<>();
      for (FileValue listingFile : listingFiles) {
        if (listingFile instanceof DirectoryValue) {
          listingRaw.add(createDirectoryRaw((DirectoryValue) listingFile));
        } else {
          listingRaw.add(CWLFileValueHelper.createFileRaw(listingFile));          
        }
      }
      setListing(listingRaw, raw);
    }
    return raw;
  }
  
  public static boolean isDirectoryLiteral(Object fileRaw) {
    if (fileRaw == null) {
      return false;
    }
    if (CWLSchemaHelper.isDirectoryFromValue(fileRaw)) {
      String location = getLocation(fileRaw);
      String path = getPath(fileRaw);
      return location == null && path == null;
    }
    return false;
  }
  
  
}
