package org.rabix.engine.stub;

import org.rabix.transport.backend.Backend;
import org.rabix.transport.mechanism.TransportPluginException;


public interface BackendStubFactory {

  <T extends Backend> BackendStub<?, ?, ?> create(T backend) throws TransportPluginException;

}
