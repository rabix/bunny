package org.rabix.bindings.transformer;

import org.rabix.bindings.model.FileValue;

public interface FileTransformer {

  FileValue transform(FileValue fileValue);
  
}
