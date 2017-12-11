package org.rabix.engine.service.impl;

import com.google.inject.Inject;
import org.rabix.bindings.model.Job;
import org.rabix.engine.service.JobService;
import org.rabix.transport.mechanism.TransportPlugin.ReceiveCallback;
import org.rabix.transport.mechanism.TransportPluginException;

public class JobReceiverImpl implements ReceiveCallback<Job> {

  @Inject
  private JobService jobService;

  public JobReceiverImpl() {}

  @Override
  public void handleReceive(Job entity, Runnable onUpdated) throws TransportPluginException {
    try {
      jobService.update(entity, onUpdated);
    } catch (Exception e) {
      throw new TransportPluginException("Failed to update Job", e);
    }
  }
}
