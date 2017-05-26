package org.rabix.storage.model.scatter;

public class PortMapping {

  private final String portId;
  private final Object value;
  
  public PortMapping(String portId, Object value) {
    this.portId = portId;
    this.value = value;
  }
  
  public String getPortId() {
    return portId;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "PortMapping [portId=" + portId + ", value=" + value + "]";
  }

}
