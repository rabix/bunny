package org.rabix.executor.rest.api.impl;

import javax.ws.rs.core.Response;

import org.rabix.backend.api.WorkerService;
import org.rabix.executor.ExecutorStatus;
import org.rabix.executor.rest.api.ExecutorHTTPService;
import org.rabix.executor.service.impl.WorkerServiceImpl.LocalWorker;

import com.google.inject.Inject;

public class ExecutorHTTPServiceImpl implements ExecutorHTTPService {

  private final WorkerService executorService;

  @Inject
  public ExecutorHTTPServiceImpl(@LocalWorker WorkerService executorService) {
    this.executorService = executorService;
  }

  public Response status() {
    if (executorService.isStopped()) {
      return ok(ExecutorStatus.STOPPED);
    }
    return ok(ExecutorStatus.RUNNING);
  }

  public Response shutdown() {
    executorService.shutdown(false);
    return ok();
  }

  public Response shutdownNow() {
    executorService.shutdown(true);
    return ok();
  }

  public Response ok(Object items) {
    if (items == null) {
      return ok();
    }
    return Response.ok().entity(items).build();
  }

  public Response ok() {
    return Response.ok().build();
  }

}
