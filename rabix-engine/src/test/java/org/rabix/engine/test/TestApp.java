package org.rabix.engine.test;

import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.ValidationReport;
import org.rabix.bindings.model.ScatterMethod;
import org.rabix.bindings.model.dag.DAGNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by luka on 23.1.17..
 */
public class TestApp extends Application {

  public List<TestPort> inputs = new ArrayList<>();
  public List<TestPort> outputs = new ArrayList<>();

  @Override
  public String serialize() {
    return "";
  }

  @Override
  public ApplicationPort getInput(String name) {
    return inputs.stream().filter(i -> i.getId().equals(name))
        .findFirst().orElse(null);
  }

  @Override
  public ApplicationPort getOutput(String name) {
    return outputs.stream().filter(i -> i.getId().equals(name))
        .findFirst().orElse(null);
  }

  @Override
  public List<? extends ApplicationPort> getInputs() {
    return inputs;
  }

  @Override
  public List<? extends ApplicationPort> getOutputs() {
    return outputs;
  }

  @Override
  public String getVersion() {
    return "test";
  }

  @Override
  public ValidationReport validate() {
    return null;
  }

  public DAGNode toDagNode(String id, ScatterMethod scatterMethod) {
    return new DAGNode(id,
        inputs.stream().map(in -> in.toInputPort(id)).collect(Collectors.toList()),
        outputs.stream().map(out -> out.toOutputPort(id)).collect(Collectors.toList()),
        scatterMethod, this, new HashMap<>(), ProtocolType.CWL);
  }
}
