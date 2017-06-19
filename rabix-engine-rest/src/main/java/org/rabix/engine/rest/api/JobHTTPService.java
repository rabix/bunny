package org.rabix.engine.rest.api;

import java.util.UUID;

import javax.ws.rs.core.Response;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;

public interface JobHTTPService {

  public Response create(Job job, Integer batch);
  
  public Response save(UUID id, Job job);
  
  public Response get(UUID id);
  
  public Response update(UUID id, JobStatus status);
  
}
