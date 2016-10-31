package org.rabix.bindings.cwl.bean.resource.requirement;

import org.rabix.bindings.cwl.bean.resource.CWLResource;
import org.rabix.bindings.cwl.bean.resource.CWLResourceType;

public class CWLShellCommandRequirement extends CWLResource {

  @Override
  public CWLResourceType getTypeEnum() {
    return CWLResourceType.SHELL_COMMAND_REQUIREMENT;
  }
  
}
