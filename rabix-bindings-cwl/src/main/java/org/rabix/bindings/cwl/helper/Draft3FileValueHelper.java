package org.rabix.bindings.draft3.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.model.FileValue;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;

public class Draft3FileValueHelper extends Draft3BeanHelper {

  private static final String KEY_NAME = "name";
  private static final String KEY_PATH = "path";
  private static final String KEY_SIZE = "size";
  private static final String KEY_FORMAT = "format";
  private static final String KEY_CHECKSUM = "checksum";
  private static final String KEY_METADATA = "metadata";
  private static final String KEY_CONTENTS = "contents";
  private static final String KEY_ORIGINAL_PATH = "originalPath";
  private static final String KEY_SECONDARY_FILES = "secondaryFiles";

  private static final int CONTENTS_NUMBER_OF_BYTES = 65536;

  public static void setFileType(Object raw) {
    setValue(Draft3SchemaHelper.KEY_JOB_TYPE, Draft3SchemaHelper.TYPE_JOB_FILE, raw);
  }

  public static Object getFormat(Object raw) {
    return getValue(KEY_FORMAT, raw);
  }
  
  public static String getName(Object raw) {
    return getValue(KEY_NAME, raw);
  }

  public static void setName(String name, Object raw) {
    setValue(KEY_NAME, name, raw);
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

  public static String getContents(Object raw) {
    return getValue(KEY_CONTENTS, raw);
  }

  public static String getChecksum(Object raw) {
    return getValue(KEY_CHECKSUM, raw);
  }

  public static String getPath(Object raw) {
    return getValue(KEY_PATH, raw);
  }

  public static void setPath(String path, Object raw) {
    setValue(KEY_PATH, path, raw);
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
    } else if (Draft3SchemaHelper.isFileFromValue(value)) {
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
    String path = Draft3FileValueHelper.getPath(fileData);

    InputStream is = null;
    try {
      File file = new File(path);
      is = new FileInputStream(file);
      byte[] buffer = new byte[Math.min(CONTENTS_NUMBER_OF_BYTES, (int) file.length())];
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
    String path = Draft3FileValueHelper.getPath(value);
    String checksum = Draft3FileValueHelper.getChecksum(value);
    Long size = Draft3FileValueHelper.getSize(value);
    
    Map<String, Object> properties = new HashMap<>();
    properties.put(Draft3BindingHelper.KEY_SBG_METADATA, Draft3FileValueHelper.getMetadata(value));

    List<FileValue> secondaryFiles = new ArrayList<>();
    List<Map<String, Object>> secondaryFileValues = Draft3FileValueHelper.getSecondaryFiles(value);
    if (secondaryFileValues != null) {
      for (Map<String, Object> secondaryFileValue : secondaryFileValues) {
        secondaryFiles.add(createFileValue(secondaryFileValue));
      }
    }
    return new FileValue(size, path, checksum, secondaryFiles, properties);
  }
  
  public static Map<String, Object> createFileRaw(FileValue fileValue) {
    Map<String, Object> raw = new HashMap<>();
    
    setFileType(raw);
    setPath(fileValue.getPath(), raw);
    setChecksum(fileValue.getChecksum(), raw);
    setSize(fileValue.getSize(), raw);
    
    Map<String, Object> properties = fileValue.getProperties();
    if (properties != null) {
      setMetadata(properties.get(Draft3BindingHelper.KEY_SBG_METADATA), raw);
    }
    
    List<FileValue> secondaryFileValues = fileValue.getSecondaryFiles();
    if (secondaryFileValues != null) {
      List<Map<String, Object>> secondaryFilesRaw = new ArrayList<>();
      for (FileValue secondaryFileValue : secondaryFileValues) {
        secondaryFilesRaw.add(createFileRaw(secondaryFileValue));
      }
      setSecondaryFiles(secondaryFilesRaw, raw);
    }
    return raw;
  }
}
