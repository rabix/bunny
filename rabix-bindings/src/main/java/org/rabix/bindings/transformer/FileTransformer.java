package org.rabix.bindings.transformer;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.FileValue;

public interface FileTransformer {

  FileValue transform(FileValue fileValue) throws BindingException;
  
}
