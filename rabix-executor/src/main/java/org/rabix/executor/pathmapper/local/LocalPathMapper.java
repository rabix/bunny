package org.rabix.executor.pathmapper.local;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.executor.config.StorageConfiguration;

import com.google.inject.Inject;

public class LocalPathMapper implements FilePathMapper {

  private final StorageConfiguration storageConfig;

  @Inject
  public LocalPathMapper(final StorageConfiguration storageConfig) {
    this.storageConfig = storageConfig;
  }

  @Override
  public String map(String path, Map<String, Object> config) throws FileMappingException {
    Path pathP = Paths.get(path);
    if (!pathP.isAbsolute()) {
      return storageConfig.getPhysicalExecutionBaseDir().toPath().resolve(pathP).toString();
    } else {
      return path;
    }
  }
}
