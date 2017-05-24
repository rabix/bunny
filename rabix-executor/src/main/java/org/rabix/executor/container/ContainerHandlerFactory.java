package org.rabix.executor.container;

import org.rabix.backend.api.callback.ExecutorStatusCallback;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.LocalContainerRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.executor.config.DockerConfigation;
import org.rabix.executor.config.StorageConfiguration;
import org.rabix.executor.container.impl.CompletedContainerHandler;
import org.rabix.executor.container.impl.DockerContainerHandler;
import org.rabix.executor.container.impl.DockerContainerHandler.DockerClientLockDecorator;
import org.rabix.executor.container.impl.LocalContainerHandler;

public class ContainerHandlerFactory {

  public static ContainerHandler create(Job job, Requirement requirement, DockerClientLockDecorator dockerClient, ExecutorStatusCallback statusCallback, StorageConfiguration storageConfig, DockerConfigation dockerConfig) throws ContainerException {
    if (requirement instanceof DockerContainerRequirement) {
      return new DockerContainerHandler(job, (DockerContainerRequirement) requirement, storageConfig, dockerConfig, statusCallback, dockerClient);
    }
    if (requirement instanceof LocalContainerRequirement) {
      return new LocalContainerHandler(job, storageConfig);
    }
    return new CompletedContainerHandler(job);
  }
  
}
