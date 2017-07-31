package org.rabix.bindings.cwl.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;

public class CWLFileValueHelper extends CWLBeanHelper {

  private static final String KEY_NAME = "basename";
  private static final String KEY_DIRNAME = "dirname";
  private static final String KEY_NAMEROOT = "nameroot";
  private static final String KEY_NAMEEXT = "nameext";
  private static final String KEY_PATH = "path";
  private static final String KEY_LOCATION = "location";
  private static final String KEY_SIZE = "size";
  private static final String KEY_FORMAT = "format";
  private static final String KEY_CHECKSUM = "checksum";
  private static final String KEY_METADATA = "metadata";
  private static final String KEY_CONTENTS = "contents";
  private static final String KEY_ORIGINAL_PATH = "originalPath";
  private static final String KEY_SECONDARY_FILES = "secondaryFiles";

  private static final int CONTENTS_NUMBER_OF_BYTES = 65536;

  public static void setFileType(Object raw) {
    setValue(CWLSchemaHelper.KEY_JOB_TYPE, CWLSchemaHelper.TYPE_JOB_FILE, raw);
  }

  public static void setDirType(Object raw) {
    setValue(CWLSchemaHelper.KEY_JOB_TYPE, CWLSchemaHelper.TYPE_JOB_DIRECTORY, raw);
  }

  public static String getFormat(Object raw) {
    return getValue(KEY_FORMAT, raw);
  }
  
  public static void setNameroot(String nameroot, Object raw) {
    setValue(KEY_NAMEROOT, nameroot, raw);
  }
  
  public static void setFormat(String format, Object raw) {
    setValue(KEY_FORMAT, format, raw);
  }
  
  public static String getNameroot(Object raw) {
    return getValue(KEY_NAMEROOT, raw);
  }
  
  public static void setNameext(String nameext, Object raw) {
    setValue(KEY_NAMEEXT, nameext, raw);
  }
  
  public static String getNameext(Object raw) {
    return getValue(KEY_NAMEEXT, raw);
  }
  
  public static String getName(Object raw) {
    return getValue(KEY_NAME, raw);
  }

  public static void setName(String name, Object raw) {
    setValue(KEY_NAME, name, raw);
  }
  
  public static String getDirname(Object raw) {
    return getValue(KEY_DIRNAME, raw);
  }

  public static void setDirname(String name, Object raw) {
    setValue(KEY_DIRNAME, name, raw);
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
  
  public static void setChecksum(String checksum, Object raw) {
    setValue(KEY_CHECKSUM, checksum, raw);
  }

  public static void setContents(Object raw) throws IOException {
    String contents = loadContents(raw);
    setValue(KEY_CONTENTS, contents, raw);
  }
  
  public static void setContents(String contents, Object raw) {
    setValue(KEY_CONTENTS, contents, raw);
  }

  public static String getContents(Object raw) {
    return getValue(KEY_CONTENTS, raw);
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
  
  public static void setOriginalPath(String path, Object raw) {
    setValue(KEY_ORIGINAL_PATH, path, raw);
  }
  
  public static String getOriginalPath(Object raw) {
    return getValue(KEY_ORIGINAL_PATH, raw);
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
   * Extract paths from unknown data
   */
  public static Set<String> flattenPaths(Object value) {
    Set<String> paths = new HashSet<>();
    if (value == null) {
      return paths;
    } else if (CWLSchemaHelper.isFileFromValue(value)) {
      paths.add(getPath(value));

      List<Map<String, Object>> secondaryFiles = getSecondaryFiles(value);
      if (secondaryFiles != null) {
        paths.addAll(flattenPaths(secondaryFiles));
      }
      return paths;
    } else if (value instanceof List<?>) {
      for (Object subvalue : ((List<?>) value)) {
        paths.addAll(flattenPaths(subvalue));
      }
      return paths;
    } else if (value instanceof Map<?, ?>) {
      for (Object subvalue : ((Map<?, ?>) value).values()) {
        paths.addAll(flattenPaths(subvalue));
      }
    }
    return paths;
  }
  
  /**
   * Load first CONTENTS_NUMBER_OF_BYTES bytes from file
   */
  private static String loadContents(Object fileData) throws IOException {
    String path = CWLFileValueHelper.getPath(fileData);

    InputStream is = null;
    try {
      File file = new File(path);
      is = new FileInputStream(file);
      int bufferSize = file.length() > 0 && file.length() < CONTENTS_NUMBER_OF_BYTES ? (int) file.length(): CONTENTS_NUMBER_OF_BYTES;
      byte[] buffer = new byte[bufferSize];
      is.read(buffer);
      return new String(buffer, "UTF-8");
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // do nothing
        }
      }
    }
  }
  
  public static FileValue createFileValue(Object value) {
    String path = CWLFileValueHelper.getPath(value);
    String name = CWLFileValueHelper.getName(value);
    String format = CWLFileValueHelper.getFormat(value);
    String location = CWLFileValueHelper.getLocation(value);
    String checksum = CWLFileValueHelper.getChecksum(value);
    String dirname = CWLFileValueHelper.getDirname(value);
    String nameroot = CWLFileValueHelper.getNameroot(value);
    String nameext = CWLFileValueHelper.getNameext(value);
    String contents = CWLFileValueHelper.getContents(value);
    
    Long size = CWLFileValueHelper.getSize(value);
    
    if (path == null) { // TODO remove
      setPath(getLocation(value), value);
    }
    if (location == null) { // TODO remove
      setLocation(getPath(value), value);
    }
    
    if(path != null) {
      File file = new File(path);
      if(name == null) {
        name = file.getName();
      }
      if(dirname == null) {
        File parent = file.getParentFile();
        dirname = parent != null ? parent.getPath(): null;
      }
      if(nameroot == null) {
        nameroot = getBasename(file.getName());
      }
      if(nameext == null) {
        nameext = getNameext(file.getName());
      }
    }
    
    Map<String, Object> properties = new HashMap<>();
    properties.put(CWLBindingHelper.KEY_SBG_METADATA, CWLFileValueHelper.getMetadata(value));

    List<FileValue> secondaryFiles = new ArrayList<>();
    List<Map<String, Object>> secondaryFileValues = CWLFileValueHelper.getSecondaryFiles(value);
    if (secondaryFileValues != null) {
      for (Map<String, Object> secondaryFileValue : secondaryFileValues) {
        if (CWLSchemaHelper.isFileFromValue(secondaryFileValue)) {
          secondaryFiles.add(createFileValue(secondaryFileValue));
          continue;
        }
        if (CWLSchemaHelper.isDirectoryFromValue(secondaryFileValue)) {
          secondaryFiles.add(CWLDirectoryValueHelper.createDirectoryValue(secondaryFileValue));
          continue;
        }
      }
    }
    return new FileValue(size, path, location, name, dirname, nameroot, nameext, contents, checksum, secondaryFiles, properties, format);
  }
  
  public static Map<String, Object> createFileRaw(FileValue fileValue) {
    Map<String, Object> raw = new HashMap<>();
    
    setFileType(raw);
    setPath(fileValue.getPath(), raw);
    setName(fileValue.getName(), raw);
    setFormat(fileValue.getFormat(), raw);
    setLocation(fileValue.getLocation(), raw);
    setChecksum(fileValue.getChecksum(), raw);
    setSize(fileValue.getSize(), raw);
    setDirname(fileValue.getDirname(), raw);
    setNameroot(fileValue.getNameroot(), raw);
    setNameext(fileValue.getNameext(), raw);
    setContents(fileValue.getContents(), raw);
    
    Map<String, Object> properties = fileValue.getProperties();
    if (properties != null) {
      setMetadata(properties.get(CWLBindingHelper.KEY_SBG_METADATA), raw);
    }
    
    List<FileValue> secondaryFileValues = fileValue.getSecondaryFiles();
    if (secondaryFileValues != null) {
      List<Map<String, Object>> secondaryFilesRaw = new ArrayList<>();
      for (FileValue secondaryFileValue : secondaryFileValues) {
        if (secondaryFileValue instanceof DirectoryValue) {
          secondaryFilesRaw.add(CWLDirectoryValueHelper.createDirectoryRaw((DirectoryValue) secondaryFileValue));
        } else {
          secondaryFilesRaw.add(createFileRaw(secondaryFileValue));
        }
      }
      setSecondaryFiles(secondaryFilesRaw, raw);
    }
    return raw;
  }
  
  public static boolean isFileLiteral(Object fileRaw) {
    if (fileRaw == null) {
      return false;
    }
    if (CWLSchemaHelper.isFileFromValue(fileRaw)) {
      String location = getLocation(fileRaw);
      String path = getPath(fileRaw);
      return location == null && path == null;
    }
    return false;
  }
  
  private static String getBasename(String filename) {
    String[] parts = StringUtils.split(filename, ".");
    if(parts.length > 2) {
      return String.join(".", Arrays.copyOfRange(parts, 0, parts.length-1));
    }
    return parts[0];
  }
  
  private static String getNameext(String filename) {
    int dotIndex = filename.lastIndexOf(".");
    if (dotIndex != -1) {
      return filename.substring(dotIndex);
    }
    return null;
  }
}
