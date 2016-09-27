package org.rabix.engine.rest.backend.stub;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.rabix.bindings.model.Job;
import org.rabix.common.engine.control.EngineControlMessage;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.HeartbeatInfo;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPlugin.ErrorCallback;
import org.rabix.transport.mechanism.TransportPlugin.ReceiveCallback;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.TransportQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BackendStub<Q extends TransportQueue, B extends Backend, T extends TransportPlugin<Q>> {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  protected JobService jobService;
  
  protected B backend;
  protected T transportPlugin;

  protected Q sendToBackendQueue;
  protected Q sendToBackendControlQueue;
  protected Q receiveFromBackendQueue;
  protected Q receiveFromBackendHeartbeatQueue;

  private ExecutorService executorService = Executors.newFixedThreadPool(2);
  
  public void start(final Map<String, Long> heartbeatInfo) {
    transportPlugin.startReceiver(receiveFromBackendQueue, Job.class, new ReceiveCallback<Job>() {
      @Override
      public void handleReceive(Job job) throws TransportPluginException {
        try {
          jobService.update(job);
        } catch (JobServiceException e) {
          throw new TransportPluginException("Failed to update Job", e);
        }
      }
    }, new ErrorCallback() {
      @Override
      public void handleError(Exception error) {
        logger.error("Failed to receive message.", error);
      }
    });

    transportPlugin.startReceiver(receiveFromBackendHeartbeatQueue, HeartbeatInfo.class,
        new ReceiveCallback<HeartbeatInfo>() {
          @Override
          public void handleReceive(HeartbeatInfo entity) throws TransportPluginException {
            logger.debug("Got heartbeat info from {}", entity.getId());
            heartbeatInfo.put(entity.getId(), entity.getTimestamp());
          }
        }, new ErrorCallback() {
          @Override
          public void handleError(Exception error) {
            logger.error("Failed to receive message.", error);
          }
        });
  }
  
  public void stop() {
    executorService.shutdownNow();
  }

  public void send(Job job) {
    this.transportPlugin.send(sendToBackendQueue, job);
  }

  public Backend getBackend() {
    return backend;
  }

  public void send(EngineControlMessage controlMessage) {
    transportPlugin.send(sendToBackendControlQueue, controlMessage);
  }

}
