package org.rabix.bindings.cwl.service;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.cwl.bean.CWLJob;

public interface CWLGlobService {

  List<File> glob(CWLJob job, File workingDir, Object glob) throws CWLGlobException;
  List<Path> glob(CWLJob job, Path workingDir, Object glob) throws CWLGlobException;
  
}
