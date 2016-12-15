package org.rabix.executor.service.impl;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.common.SystemEnvironmentHelper;
import org.rabix.executor.service.JobFitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class JobFitterImpl implements JobFitter {

  private static final Logger logger = LoggerFactory.getLogger(JobFitterImpl.class);
  
  private Long availableCores;
  private Long availableMemory;

  private boolean isEnabled;
  private int runningProcesses = 0;

  @Inject
  public JobFitterImpl(Configuration configuration) {
    this.isEnabled = configuration.getBoolean("resource.fitter.enabled", false);
    
    this.availableMemory = SystemEnvironmentHelper.getTotalPhysicalMemorySizeInMB();
    this.availableCores = SystemEnvironmentHelper.getNumberOfCores();
  }
  
  @Override
  public synchronized boolean tryToFit(Job job) throws BindingException {
    if (!isEnabled) {
      return true;
    }
    Bindings bindings = BindingsFactory.create(job);
    if (bindings.isSelfExecutable(job)) {
      return true;
    }
    ResourceRequirement resourceRequirement = bindings.getResourceRequirement(job);

    boolean cpuFits = true;
    Long cpu = resourceRequirement.getCpuMin();
    if (cpu != null && cpu > availableCores) {
      cpuFits = false;
    }

    boolean memoryFits = true;
    Long memory = resourceRequirement.getMemMinMB();
    if (memory != null && memory > availableMemory) {
      memoryFits = false;
    }
    
    if ((!cpuFits || !memoryFits) && runningProcesses > 0) {
      return false;
    }
    
    runningProcesses++;
    availableCores -= cpu != null ? cpu : 0;
    availableMemory -= memory != null ? memory : 0;
    logger.info("Job {} fits. Number of running processes {}.", job.getId(), runningProcesses);
    return true;
  }

  @Override
  public synchronized void free(Job job) throws BindingException {
    if (!isEnabled) {
      return;
    }

    Bindings bindings = BindingsFactory.create(job);
    if (bindings.isSelfExecutable(job)) {
      return;
    }
    
    ResourceRequirement resourceRequirement = bindings.getResourceRequirement(job);

    runningProcesses--;
    availableCores += resourceRequirement.getCpuMin() != null ? resourceRequirement.getCpuMin() : 0;
    availableMemory += resourceRequirement.getMemMinMB() != null ? resourceRequirement.getMemMinMB() : 0;

    logger.info("Job {} freed reqsources. Number of running processes {}.", job.getId(), runningProcesses);
  }
  
}
