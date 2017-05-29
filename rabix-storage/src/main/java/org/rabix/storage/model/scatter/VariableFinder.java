package org.rabix.storage.model.scatter;

import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.storage.model.VariableRecord;

import java.util.UUID;

/**
 * Created by luka on 29.5.17..
 */
public interface VariableFinder {

  VariableRecord find(String jobId, String portId, DAGLinkPort.LinkPortType type, UUID rootId);

  Object getValue(VariableRecord variableRecord);

}
