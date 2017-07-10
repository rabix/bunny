package org.rabix.engine.test;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort;

/**
 * Created by luka on 23.1.17..
 */
public class TestPort extends ApplicationPort {

  public TestPort(@JsonProperty("id") String id,
                  @JsonProperty("default") Object defaultValue,
                  @JsonProperty("type") Object schema,
                  @JsonProperty("scatter") Boolean scatter,
                  @JsonProperty("linkMerge") String linkMerge,
                  @JsonProperty("description") String description) {
    super(id, defaultValue, schema, scatter, linkMerge, description);
  }

  @Override
  public boolean isList() {
    return schema.equals("vector");
  }

  public static TestPort simplePort(String id) {
    return new TestPort(id, null, "type", false, null, null);
  }

  public DAGLinkPort toInputPort(String appId) {
    String fullId = appId + "." + id;
    LinkMerge lm = linkMerge != null? LinkMerge.valueOf(linkMerge) : null;
    return new DAGLinkPort(fullId, fullId, DAGLinkPort.LinkPortType.INPUT, lm, scatter, defaultValue, null);
  }

  public DAGLinkPort toOutputPort(String appId) {
    String fullId = appId + "." + id;
    LinkMerge lm = linkMerge != null? LinkMerge.valueOf(linkMerge) : null;
    return new DAGLinkPort(fullId, fullId, DAGLinkPort.LinkPortType.OUTPUT, lm, scatter, defaultValue, null);
  }

  @Override
  public Object getBinding() {
    // TODO Auto-generated method stub
    return null;
  }

}
