package org.rabix.bindings.draft3.bean.resource.requirement;

import org.rabix.bindings.draft3.bean.resource.Draft3Resource;
import org.rabix.bindings.draft3.bean.resource.Draft3ResourceType;

public class Draft3ShellCommandRequirement extends Draft3Resource {

  @Override
  public Draft3ResourceType getType() {
    return Draft3ResourceType.SHELL_COMMAND_REQUIREMENT;
  }
  
}
