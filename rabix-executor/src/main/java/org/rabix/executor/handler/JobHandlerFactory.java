package org.rabix.executor.handler;

import org.rabix.backend.api.engine.EngineStub;
import org.rabix.bindings.model.Job;

public interface JobHandlerFactory {

  JobHandler createHandler(Job job, EngineStub<?,?,?> engineStub);
  
}
