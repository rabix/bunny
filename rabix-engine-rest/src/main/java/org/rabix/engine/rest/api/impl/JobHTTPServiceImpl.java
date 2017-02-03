package org.rabix.engine.rest.api.impl;

import java.util.Collections;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.rabix.bindings.model.Job;
import org.rabix.engine.rest.api.JobHTTPService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.engine.rest.service.JobService;

import com.google.inject.Inject;

public class JobHTTPServiceImpl implements JobHTTPService {

  private final JobService jobService;

  @Inject
  public JobHTTPServiceImpl(JobService jobService) {
    this.jobService = jobService;
  }
  
  @Override
  public Response create(Job job) {
    try {
      return ok(jobService.start(job, null));
    } catch (Exception e) {
      return internalError("Error while creating a job.");
    }
  }
  
  @Override
  public Response get() {
    return ok(jobService.get());
  }
  
  @Override
  public Response get(String id) {
    UUID uuid;
    try {
      uuid = UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      return badRequest("Invalid job id. IDs must be UUID");
    }
    Job job = jobService.get(uuid);
    if (job == null) {
      return entityNotFound();
    }
    return ok(job);
  }
  
  @Override
  public Response save(String id, Job job) {
    try {
      jobService.update(job);
    } catch (JobServiceException e) {
      return internalError("Error while updating a job.");
    }
    return ok();
  }
  
  private Response entityNotFound() {
    return Response.status(Status.NOT_FOUND).build();
  }
  
  private Response badRequest(String message) {
    return Response.status(Status.BAD_REQUEST).entity(message).build();
  }

  private Response internalError(String message) {
    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).build();
  }
  
  private Response ok() {
    return Response.ok(Collections.emptyMap()).build();
  }
  
  private Response ok(Object items) {
    if (items == null) {
      return ok();
    }
    return Response.ok().entity(items).build();
  }
}
