package org.rabix.backend.api.engine;

import org.rabix.backend.api.WorkerService;
import org.rabix.bindings.model.Job;
import org.rabix.common.engine.control.EngineControlFreeMessage;
import org.rabix.common.engine.control.EngineControlMessage;
import org.rabix.common.engine.control.EngineControlStopMessage;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.HeartbeatInfo;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class EngineStub<Q extends TransportQueue, B extends Backend, T extends TransportPlugin<Q>> {

  public final static long DEFAULT_HEARTBEAT_TIME = 60000L;

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected B backend;
  protected T transportPlugin;

  protected ScheduledExecutorService scheduledHeartbeatService = Executors.newSingleThreadScheduledExecutor();

  protected Q sendToBackendQueue;
  protected Q sendToBackendControlQueue;
  protected Q receiveFromBackendQueue;
  protected Q receiveFromBackendHeartbeatQueue;

  protected Long heartbeatTimeMills;

  protected WorkerService executorService;

  public void start() {
    transportPlugin.startReceiver(sendToBackendQueue, Job.class,
            (job, onHandled) -> executorService.submit(job, job.getRootId()),
            error -> logger.error("Failed to receive message.", error));

    transportPlugin.startReceiver(sendToBackendControlQueue, EngineControlMessage.class, (controlMessage, onHandled) -> {
      switch (controlMessage.getType()) {
        case STOP:
          List<UUID> ids = new ArrayList<>();
          ids.add(((EngineControlStopMessage)controlMessage).getId());
          executorService.cancel(ids, controlMessage.getRootId());
          break;
        case FREE:
          executorService.freeResources(controlMessage.getRootId(), ((EngineControlFreeMessage)controlMessage).getConfig());
          break;
        default:
          break;
      }
    }, error -> logger.error("Failed to execute control message.", error));

    scheduledHeartbeatService.scheduleAtFixedRate(
        () -> transportPlugin.send(receiveFromBackendHeartbeatQueue, new HeartbeatInfo(backend.getId(), System.currentTimeMillis())),
        0, heartbeatTimeMills, TimeUnit.MILLISECONDS);
  }

  public void stop() {
    scheduledHeartbeatService.shutdown();
  }

  public void send(Job job) {
    transportPlugin.send(receiveFromBackendQueue, job);
  }

}
