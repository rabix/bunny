package org.rabix.bindings.model.requirement;

public class CustomRequirement extends Requirement {

  private String type;
  private Object data;
  
  public CustomRequirement(String type, Object customData) {
    this.type = type;
    this.data = customData;
  }
  
  @Override
  public boolean isCustom() {
    return true;
  }

  @Override
  public Object getData() {
    return data;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return "CustomRequirement [type=" + type + ", data=" + data + "]";
  }

}
