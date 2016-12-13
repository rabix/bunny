package org.rabix.tes.command.line.service.impl;

import java.io.File;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.tes.command.line.service.TESCommandLineException;
import org.rabix.tes.command.line.service.TESCommandLineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TESFinalizeService implements TESCommandLineService {

  private final static Logger logger = LoggerFactory.getLogger(TESFinalizeService.class);
  
  public static final String ERROR_LOG = "job.err.log";
  
  public void execute(Job job, File workingDir) throws TESCommandLineException {
    Bindings bindings;
    try {
      bindings = BindingsFactory.create(job);
      job = bindings.preprocess(job, workingDir, null);
      
      String standardErrorLog = bindings.buildCommandLineObject(job, workingDir, new FilePathMapper() {
        @Override
        public String map(String path, Map<String, Object> config) throws FileMappingException {
          return path;
        }
      }).getStandardError();
      
      if (standardErrorLog == null) {
        standardErrorLog = ERROR_LOG;
      }

      if (!bindings.isSuccessful(job, 0)) { // TODO change exitCode
        System.exit(-1);
        return;
      }
      
      job = bindings.postprocess(job, workingDir, HashAlgorithm.SHA1, null);
      
      System.out.println(JSONHelper.writeObject(job.getOutputs()));
    } catch (BindingException e) {
      logger.error("Failed to use Bindings", e);
      throw new TESCommandLineException("Failed to use Bindings", e);
    }
  }
  
}
