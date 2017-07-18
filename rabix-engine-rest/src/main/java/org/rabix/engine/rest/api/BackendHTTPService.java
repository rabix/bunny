package org.rabix.engine.rest.api;

import javax.ws.rs.core.Response;

import org.rabix.transport.backend.Backend;

public interface BackendHTTPService {

  Response create(Backend job);

}
