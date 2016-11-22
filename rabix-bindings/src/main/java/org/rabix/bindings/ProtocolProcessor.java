package org.rabix.bindings;

import java.io.File;

import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;

public interface ProtocolProcessor {

  Job preprocess(Job job, File workingDir, FilePathMapper logFilesPathMapper) throws BindingException;

  Job postprocess(Job job, File workingDir, HashAlgorithm hashAlgorithm, FilePathMapper logFilePathMapper) throws BindingException;

  Object transformInputs(Object value, Job job, Object transform) throws BindingException;

  boolean isSuccessful(Job job, int statusCode) throws BindingException;

}
