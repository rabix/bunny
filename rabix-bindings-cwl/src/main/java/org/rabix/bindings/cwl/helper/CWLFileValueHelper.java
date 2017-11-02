package org.rabix.bindings.cwl.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.cwl.CWLProcessor;
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

  public static boolean isDirType(Object raw) {
    return getValue(CWLSchemaHelper.KEY_JOB_TYPE, raw).equals(CWLSchemaHelper.TYPE_JOB_DIRECTORY);
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

  public static void setChecksum(Path file, Object raw, HashAlgorithm hashAlgorithm) {
    if (!Files.exists(file)) {
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
    return getValue(KEY_PATH, raw);
  }

  public static void setPath(String path, Object raw) {
    setValue(KEY_PATH, path, raw);
    if (isDirType(raw)) {
      List<Object> listing = CWLDirectoryValueHelper.getListing(raw);
      if (listing != null)
        listing.forEach(file -> setPath(Paths.get(path).resolve(getName(file)).toString(), file));
    }
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
    String path = getPath(fileData);
    Path pathP = Paths.get(path);
    if (!Files.exists(pathP)) {
      return new String(Files.readAllBytes(Paths.get(URI.create(getLocation(fileData)))), "UTF-8");
    }
    InputStream is = null;
    try {
      return new String(Files.readAllBytes(pathP), "UTF-8");
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
    String path = getPath(value);
    String name = getName(value);
    String format = getFormat(value);
    String location = getLocation(value);
    String checksum = getChecksum(value);
    String dirname = getDirname(value);
    String nameroot = getNameroot(value);
    String nameext = getNameext(value);
    String contents = getContents(value);

    Long size = getSize(value);

    if (path != null) {
      File file = new File(path);
      if (name == null) {
        name = file.getName();
      }
      if (dirname == null) {
        File parent = file.getParentFile();
        dirname = parent != null ? parent.getPath() : null;
      }
      if (nameroot == null) {
        nameroot = getBasename(file.getName());
      }
      if (nameext == null) {
        nameext = getNameext(file.getName());
      }
    }

    Map<String, Object> properties = new HashMap<>();
    properties.put(CWLBindingHelper.KEY_SBG_METADATA, getMetadata(value));

    List<FileValue> secondaryFiles = new ArrayList<>();
    List<Map<String, Object>> secondaryFileValues = getSecondaryFiles(value);
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
    if (parts.length > 2) {
      return String.join(".", Arrays.copyOfRange(parts, 0, parts.length - 1));
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

  public static void buildMissingInfo(Object value, HashAlgorithm alg, Path workDir) throws IOException, URISyntaxException {
    String path = getPath(value);
    String location = getLocation(value);
    Path actual = null;

    if (path == null) {
      if (location != null) {
        URI uri = URI.create(location);
        if (uri.getScheme() == null) {
          uri = new URI("file", location, null);
        }
        if (uri.isOpaque()) {
          uri = new URI("file", workDir.resolve(uri.getSchemeSpecificPart()).toAbsolutePath().toString(), null);
        }
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
      actual = workDir.resolve(path);
      location = actual.toUri().toString();
    } else {
      actual = Paths.get(URI.create(location));
    }
    if(!Paths.get(path).isAbsolute()){
      path=workDir.resolve(path).toAbsolutePath().toString();
    }
    
//    if(!Files.exists(actual) && Files.exists(workDir.resolve(path))){
//      actual = workDir.resolve(path);
//    }
    
    String name = getName(value);
    if (name == null) {
      setNames(actual, value);
    } else {
      if (!path.endsWith(name)) {
        path = Paths.get(path).resolveSibling(name).toString();
      }
    }
    
    setPath(path, value);
    setLocation(location, value);
    
    if (getSize(value) == null)
      setSize(Files.size(actual), value);

    if (CWLSchemaHelper.isDirectoryFromValue(value)) {
      setListing(actual, value, alg, workDir);
    } else {
      if (alg != null) {
        setChecksum(actual, value, alg);
      }
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

  private static void setNames(Path path, Object value) throws IOException {
    String name = path.getFileName().toString();
    if (getName(value) == null)
      setName(name, value);

    int dotIndex = name.lastIndexOf(".");
    if (dotIndex != -1) {
      if (getNameext(value) == null)
        setNameext(name.substring(dotIndex), value);
      if (getNameroot(value) == null)
        setNameroot(name.substring(0, dotIndex), value);
    }
    if (path.getParent() != null)
      setDirname(path.getParent().toString(), value);
  }

  private static void setListing(Path path, Object value, HashAlgorithm hash, Path workDir) throws IOException, URISyntaxException {
    List<Object> listing = new ArrayList<>();
    for (Path childFile : Files.list(path).toArray(Path[]::new)) {
      switch (childFile.getFileName().toString()) {
        case CWLProcessor.JOB_FILE:
        case CWLProcessor.RESULT_FILENAME:
        case CWLProcessor.RESERVED_EXECUTOR_CMD_LOG_FILE_NAME:
        case CWLProcessor.RESERVED_EXECUTOR_ERROR_LOG_FILE_NAME:
          continue;
        default:
          break;
      }
      listing.add(pathToRawFile(childFile, hash, workDir));
    }
    CWLDirectoryValueHelper.setListing(listing, value);
  }

  public static Map<String, Object> pathToRawFile(Path file, HashAlgorithm hash, Path workDir) throws IOException, URISyntaxException {
    Map<String, Object> fileValue = new HashMap<>();

    if (Files.isDirectory(file)) {
      setDirType(fileValue);
    } else {
      setFileType(fileValue);
    }
//    setPath(file.toString(), fileValue);
    setLocation(file.toUri().toString(), fileValue);
    buildMissingInfo(fileValue, hash, workDir);
    return fileValue;
  }
}
