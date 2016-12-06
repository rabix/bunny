package org.rabix.tes.service;

import org.rabix.bindings.model.Job;
import org.rabix.tes.service.impl.TESStorageServiceImpl.LocalFileStorage;
import org.rabix.tes.service.impl.TESStorageServiceImpl.SharedFileStorage;

public interface TESStorageService {

  enum StorageType {
    sharedFile
  }
  
  boolean isSupported() throws TESServiceException;
  
  SharedFileStorage getStorageInfo() throws TESServiceException;
  
  Job stageInputFiles(Job job, final LocalFileStorage localFileStorage, final SharedFileStorage sharedFileStorage);
  
}
