package org.rabix.common.config;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.tree.UnionCombiner;
import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class ConfigModule extends AbstractModule {

  private final File configDir;
  private final Map<String, Object> overrides;

  public ConfigModule(File configDir, Map<String, Object> overrides) {
    Preconditions.checkNotNull(configDir);
    Preconditions.checkArgument(configDir.exists() && configDir.isDirectory());
    
    this.configDir = configDir;
    this.overrides = overrides;
  }

  @Override
  protected void configure() {
  }

  @Provides
  @Singleton
  @SuppressWarnings("unchecked")
  public Configuration provideConfig() {
    PropertiesConfiguration configuration = new PropertiesConfiguration();

    try {
      Iterator<File> iterator = FileUtils.iterateFiles(configDir, new String[] { "properties" }, true);
      while (iterator.hasNext()) {
        File configFile = iterator.next();
        configuration.load(configFile);
      }
      if (overrides != null) {
        MapConfiguration mapConfiguration = new MapConfiguration(overrides);
        
        CombinedConfiguration combinedConfiguration = new CombinedConfiguration(new UnionCombiner());
        combinedConfiguration.addConfiguration(mapConfiguration);
        combinedConfiguration.addConfiguration(configuration);
        return combinedConfiguration;
      }
      return configuration;
    } catch (ConfigurationException e) {
      throw new RuntimeException("Failed to load configuration properties", e);
    }
  }

}
