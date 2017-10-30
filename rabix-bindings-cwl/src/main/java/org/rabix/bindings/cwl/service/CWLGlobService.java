package org.rabix.bindings.cwl.service;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

import org.rabix.bindings.cwl.bean.CWLJob;

public interface CWLGlobService {

  Set<File> glob(CWLJob job, File workingDir, Object glob) throws CWLGlobException;
  Set<Path> glob(CWLJob job, Path workingDir, Object glob) throws CWLGlobException;
  
}
