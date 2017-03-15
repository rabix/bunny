package org.rabix.engine.stub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.rabix.bindings.model.Job;
import org.rabix.common.engine.control.EngineControlMessage;
import org.rabix.engine.service.JobService;
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

  protected boolean enableControlMesages;
  
  private ExecutorService executorService = Executors.newFixedThreadPool(2);
  
  public static interface HeartbeatCallback {
    void save(HeartbeatInfo info) throws Exception;
  }
  
  public void start(HeartbeatCallback heartbeatCallback, ReceiveCallback<Job> receiveCallback, ErrorCallback errorCallback) {
    transportPlugin.startReceiver(receiveFromBackendQueue, Job.class, receiveCallback, errorCallback);

    transportPlugin.startReceiver(receiveFromBackendHeartbeatQueue, HeartbeatInfo.class,
        new ReceiveCallback<HeartbeatInfo>() {
          @Override
          public void handleReceive(HeartbeatInfo entity) throws TransportPluginException {
            logger.trace("Got heartbeat info from {}", entity.getId());
            try {
              heartbeatCallback.save(entity);
            } catch (Exception e) {
              logger.error("Failed to update heartbeat", e);
              throw new TransportPluginException(e);
            }
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
  
  public void send(Object message) {
    this.transportPlugin.send(sendToBackendQueue, message);
  }

  public Backend getBackend() {
    return backend;
  }

  public void send(EngineControlMessage controlMessage) {
    if (enableControlMesages) {
      transportPlugin.send(sendToBackendControlQueue, controlMessage);
    }
  }

}
