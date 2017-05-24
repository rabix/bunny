package org.rabix.executor.rest.api.impl;

import javax.ws.rs.core.Response;

import org.rabix.backend.api.ExecutorService;
import org.rabix.executor.ExecutorStatus;
import org.rabix.executor.rest.api.ExecutorHTTPService;

import com.google.inject.Inject;

public class ExecutorHTTPServiceImpl implements ExecutorHTTPService {

  private final ExecutorService executorService;

  @Inject
  public ExecutorHTTPServiceImpl(ExecutorService executorService) {
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
