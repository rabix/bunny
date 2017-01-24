package org.rabix.engine.test;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

import java.util.Map;

/**
 * Created by luka on 23.1.17..
 */
public class DummyConfigModule extends AbstractModule {

  Map<String, Object> config;

  public DummyConfigModule(Map<String, Object> config) {
    this.config = config;
  }

  @Override
  protected void configure() {

  }

  @Provides
  @Singleton
  public Configuration provideConfig() {
    return new MapConfiguration(config);
  }
}
