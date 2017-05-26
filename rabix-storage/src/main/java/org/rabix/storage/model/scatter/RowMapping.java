package org.rabix.storage.model.scatter;

import java.util.List;

public class RowMapping {

  private final int row;
  private final List<PortMapping> portMappings;
  
  public RowMapping(int row, List<PortMapping> portMappings) {
    this.row = row;
    this.portMappings = portMappings;
  }
  
  public int getIndex() {
    return row;
  }
  
  public Object getValue(String portId) {
    for (PortMapping portMapping : portMappings) {
      if (portMapping.getPortId().equals(portId)) {
        return portMapping.getValue();
      }
    }
    return null;
  }
  
  public List<PortMapping> getPortMappings() {
    return portMappings;
  }

  @Override
  public String toString() {
    return "RowMapping [row=" + row + ", portMappings=" + portMappings + "]";
  }
  
}
