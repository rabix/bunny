package org.rabix.executor.service.impl;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.executor.config.StorageConfiguration;
import org.rabix.executor.container.ContainerException;
import org.rabix.executor.container.impl.DockerContainerHandler.DockerClientLockDecorator;
import org.rabix.executor.service.FilePermissionService;

import com.google.inject.Inject;

public class NoOpFilePermissionServiceImpl implements FilePermissionService {


  @Inject
  public NoOpFilePermissionServiceImpl(DockerClientLockDecorator dockerClient, StorageConfiguration storageConfiguration, Configuration configuration) {
  }

  @Override
  public void execute(Job job) throws ContainerException {
  }
}
