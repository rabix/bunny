package org.rabix.executor;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.common.config.ConfigModule;
import org.rabix.executor.config.StorageConfiguration;
import org.rabix.executor.config.impl.LocalStorageConfiguration;
import org.rabix.executor.pathmapper.InputFileMapper;
import org.rabix.executor.pathmapper.OutputFileMapper;
import org.rabix.executor.pathmapper.local.LocalPathMapper;

import com.google.inject.AbstractModule;

public class LocalStorageModule extends AbstractModule {

  private ConfigModule configModule;
  
  public LocalStorageModule(ConfigModule configModule) {
    this.configModule = configModule;
  }
  
  @Override
  protected void configure() {
    Configuration configuration = configModule.provideConfig();
    String rootDir = configuration.getString("backend.execution.directory.name"); 
    bind(StorageConfiguration.class).toInstance(new LocalStorageConfiguration(configModule.provideConfig(), rootDir));
    bind(FilePathMapper.class).annotatedWith(InputFileMapper.class).to(LocalPathMapper.class);
    bind(FilePathMapper.class).annotatedWith(OutputFileMapper.class).to(LocalPathMapper.class);
  }

}
