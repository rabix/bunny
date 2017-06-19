package org.rabix.engine.rest.api.impl;

import java.util.Collections;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.engine.rest.api.JobHTTPService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.JobServiceException;

import com.google.inject.Inject;

@Produces(MediaType.APPLICATION_JSON)
@Path("/v0/engine/jobs")
public class JobHTTPServiceImpl implements JobHTTPService {

  private final JobService jobService;

  @Inject
  public JobHTTPServiceImpl(JobService jobService) {
    this.jobService = jobService;
  }

  @Override
  @POST
  public Response create(Job job, @HeaderParam("batch") Integer batch) {
    try {
      if (batch != null) {
        for (int i=0;i<batch;i++) {
          jobService.start(job, null);
        }
        return ok("success");
      }
      return ok(jobService.start(job, null));
    } catch (Exception e) {
      return error();
    }
  }
  
  @Override
  @GET
  @Path("/{id}")
  public Response get(@PathParam("id") UUID id) {
    Job job = jobService.get(id);
    if (job == null) {
      return entityNotFound();
    }
    return ok(job);
  }
  
  @Override
  @PUT
  @Path("/{id}")
  public Response save(@PathParam("id") UUID id, Job job) {
    try {
      jobService.update(job);
    } catch (JobServiceException e) {
      return error();
    }
    return ok();
  }
  
  @Override
  @PUT
  @Path("/{id}/{status}")
  public Response update(@PathParam("id") UUID id, @PathParam("status") JobStatus status){
    try {
      Job job = jobService.get(id);
      job = Job.cloneWithStatus(job, status);
      
      jobService.update(job);
    } catch (JobServiceException e) {
      return error();
    }
    return ok();
  }
  
  private Response entityNotFound() {
    return Response.status(Status.NOT_FOUND).build();
  }
  
  private Response error() {
    return Response.status(Status.BAD_REQUEST).build();
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
