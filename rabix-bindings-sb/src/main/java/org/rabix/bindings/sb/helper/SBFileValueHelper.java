package org.rabix.bindings.sb.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.model.FileValue;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;

public class SBFileValueHelper extends SBBeanHelper {

  private static final String KEY_NAME = "name";
  private static final String KEY_PATH = "path";
  private static final String KEY_LOCATION = "location";
  private static final String KEY_SIZE = "size";
  private static final String KEY_CHECKSUM = "checksum";
  private static final String KEY_METADATA = "metadata";
  private static final String KEY_CONTENTS = "contents";
  private static final String KEY_ORIGINAL_PATH = "originalPath";
  private static final String KEY_SECONDARY_FILES = "secondaryFiles";
  private static final String KEY_DIRNAME = "dirname";

  private static final int CONTENTS_NUMBER_OF_BYTES = 65536;

  public static void setFileType(Object raw) {
    setValue(SBSchemaHelper.KEY_JOB_TYPE, SBSchemaHelper.TYPE_JOB_FILE, raw);
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

  public static void setChecksum(Path actual, Object raw, HashAlgorithm hashAlgorithm) {
    if (!Files.exists(actual)) {
      throw new RuntimeException("Missing file " + actual);
    }
    String checksum = ChecksumHelper.checksum(actual, hashAlgorithm);
    if (checksum != null) {
      setValue(KEY_CHECKSUM, checksum, raw);
    }
  }

  public static void setChecksum(String checksum, Object raw) {
    setValue(KEY_CHECKSUM, checksum, raw);
  }

  public static String getDirname(Object raw) {
    return getValue(KEY_DIRNAME, raw);
  }

  public static void setDirname(String name, Object raw) {
    setValue(KEY_DIRNAME, name, raw);
  }

  public static void setContents(Object raw) throws IOException {
    String contents = loadContents(raw);
    setValue(KEY_CONTENTS, contents, raw);
  }

  public static String getContents(Object raw) {
    return getValue(KEY_CONTENTS, raw);
  }

  private static void setContents(String contents, Map<String, Object> raw) {
    setValue(KEY_CONTENTS, contents, raw);
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
    } else if (SBSchemaHelper.isFileFromValue(value)) {
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
    String path = SBFileValueHelper.getPath(fileData);

    InputStream is = null;
    try {
      File file = new File(path);
      is = new FileInputStream(file);
      int bufferSize = file.length() > 0 && file.length() < CONTENTS_NUMBER_OF_BYTES ? (int) file.length() : CONTENTS_NUMBER_OF_BYTES;
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
    String path = SBFileValueHelper.getPath(value);
    String name = SBFileValueHelper.getName(value);
    String location = SBFileValueHelper.getLocation(value);
    String checksum = SBFileValueHelper.getChecksum(value);
    String contents = SBFileValueHelper.getContents(value);
    String dirname = SBFileValueHelper.getDirname(value);
    Long size = SBFileValueHelper.getSize(value);

    Map<String, Object> properties = new HashMap<>();
    properties.put(SBBindingHelper.KEY_SBG_METADATA, SBFileValueHelper.getMetadata(value));

    List<FileValue> secondaryFiles = new ArrayList<>();
    List<Map<String, Object>> secondaryFileValues = SBFileValueHelper.getSecondaryFiles(value);
    if (secondaryFileValues != null) {
      for (Map<String, Object> secondaryFileValue : secondaryFileValues) {
        secondaryFiles.add(createFileValue(secondaryFileValue));
      }
    }

    FileValue ret = new FileValue(size, path, location, checksum, secondaryFiles, properties, name, null, contents);
    ret.setDirname(dirname);
    return ret;
  }

  public static Map<String, Object> createFileRaw(FileValue fileValue) {
    Map<String, Object> raw = new HashMap<>();

    setFileType(raw);
    setPath(fileValue.getPath(), raw);
    setName(fileValue.getName(), raw);
    setLocation(fileValue.getLocation(), raw);
    setChecksum(fileValue.getChecksum(), raw);
    setSize(fileValue.getSize(), raw);
    setContents(fileValue.getContents(), raw);
    setDirname(fileValue.getDirname(), raw);

    Map<String, Object> properties = fileValue.getProperties();
    if (properties != null) {
      setMetadata(properties.get(SBBindingHelper.KEY_SBG_METADATA), raw);
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

  public static void buildMissingInfo(Object value, HashAlgorithm alg, Path workDir) throws IOException, URISyntaxException {
    String path = getPath(value);
    String location = getLocation(value);
    Path actual = null;

    if (path == null) {
      if (location != null) {
        URI uri = createFullURI(location, workDir);
        location = uri.toString();
        actual = Paths.get(uri);
        if (!actual.isAbsolute()) {
          actual = workDir.resolve(actual).toAbsolutePath();
        }
        path = actual.toString();
      } else {
        return;
      }
    }

    if (location == null) {
      actual = workDir.resolve(path).toAbsolutePath();
      location = actual.toUri().toString();
    } else {
      actual = Paths.get(createFullURI(location, workDir));
    }
    if (!Paths.get(path).isAbsolute()) {
      path = workDir.resolve(path).toAbsolutePath().toString();
    }

    String name = getName(value);
    if (name == null) {
      setNames(actual, value);
    } else {
      if (!path.endsWith(name)) {
        path = Paths.get(path).resolveSibling(name).toString();
      }
    }

    setPath(path.replace(" ", "\\ "), value);
    setLocation(location, value);

    if (getSize(value) == null && Files.exists(actual)) {
      setSize(Files.size(actual), value);
      if (alg != null)
        setChecksum(actual, value, alg);
    }

    List<Map<String, Object>> secondaryFiles = getSecondaryFiles(value);
    if (secondaryFiles != null) {
      for (Map<String, Object> secondaryFileValue : secondaryFiles) {
        buildMissingInfo(secondaryFileValue, alg, workDir);
      }
    }
    if (name != null && !actual.endsWith(name)) {
      setPath(Paths.get(path).resolveSibling(name).toString(), value);
    }
  }

  private static URI createFullURI(String val, Path parent) throws URISyntaxException {
    URI uri = URI.create(val);
    if (uri.getScheme() == null) {
      uri = new URI("file", val, null);
    }
    if (uri.isOpaque()) {
      uri = new URI("file", parent.resolve(uri.getSchemeSpecificPart()).toAbsolutePath().toString(), null);
    }
    return uri;
  }

  private static void setNames(Path path, Object value) throws IOException {
    String name = path.getFileName().toString();
    if (getName(value) == null)
      setName(name, value);
    if (path.getParent() != null)
      setDirname(path.getParent().toString(), value);
  }

  public static void setChecksum(File file, Map<String, Object> fileData, HashAlgorithm hashAlgorithm) {
    setChecksum(file.toPath(), fileData, hashAlgorithm);
  }
}
