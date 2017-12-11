package org.rabix.engine.stub;

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

  protected boolean enableControlMessages;
  protected boolean cleanup;


  public static interface HeartbeatCallback {
    void save(HeartbeatInfo info) throws Exception;
  }

  public void start(HeartbeatCallback heartbeatCallback, ReceiveCallback<Job> receiveCallback, ErrorCallback errorCallback) {
    transportPlugin.startReceiver(receiveFromBackendQueue, Job.class, receiveCallback, errorCallback);

    transportPlugin.startReceiver(receiveFromBackendHeartbeatQueue, HeartbeatInfo.class,
            (entity, onHandled) -> {
              logger.trace("Got heartbeat info from {}", entity.getId());
              try {
                heartbeatCallback.save(entity);
                onHandled.run();
              } catch (Exception e) {
                logger.error("Failed to update heartbeat", e);
                throw new TransportPluginException(e);
              }
            }, error -> logger.error("Failed to receive message.", error));
  }

  public void stop() {
    if(cleanup){
      transportPlugin.stopReceiver(receiveFromBackendHeartbeatQueue);
      transportPlugin.stopReceiver(receiveFromBackendQueue);
    }
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
    if (enableControlMessages) {
      transportPlugin.send(sendToBackendControlQueue, controlMessage);
    }
  }
}
