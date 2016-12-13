package org.rabix.backend.local.tes.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.rabix.backend.local.tes.client.TESHttpClient;
import org.rabix.backend.local.tes.model.TESServiceInfo;
import org.rabix.backend.local.tes.service.TESServiceException;
import org.rabix.backend.local.tes.service.TESStorageService;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.transformer.FileTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class LocalTESStorageServiceImpl implements TESStorageService {

  private final static Logger logger = LoggerFactory.getLogger(LocalTESStorageServiceImpl.class);

  private static String STORAGE_BASE_DIR = "baseDir";
  private static String STORAGE_TYPE_KEY = "storageType";

  private TESHttpClient tesHttpClient;
  
  @Inject
  public LocalTESStorageServiceImpl(TESHttpClient tesHttpClient) {
    this.tesHttpClient = tesHttpClient;
  }

  @Override
  public SharedFileStorage getStorageInfo() throws TESServiceException {
    try {
      TESServiceInfo serviceInfo = tesHttpClient.getServiceInfo();
      String storageTypeStr = serviceInfo.getStorageConfig().get(STORAGE_TYPE_KEY);
      
      StorageType storageType = StorageType.valueOf(storageTypeStr);
      if (storageType.equals(StorageType.sharedFile)) {
        return new SharedFileStorage(serviceInfo.getStorageConfig().get(STORAGE_BASE_DIR));
      }
      return null;
    } catch (Exception e) {
      logger.error("Failed to retrieve storage information.", e);
      throw new TESServiceException("Failed to retrieve storage information.", e);
    }
  }
  
  @Override
  public Job stageInputFiles(Job job, final LocalFileStorage localFileStorage, final SharedFileStorage sharedFileStorage) throws BindingException {
    try {
      return FileValueHelper.updateInputFiles(job, new FileTransformer() {
        @Override
        public FileValue transform(FileValue fileValue) throws BindingException {
          if (fileValue instanceof DirectoryValue) {
            return stageDirectory((DirectoryValue) fileValue, sharedFileStorage, localFileStorage, this);
          }
          return stageFile(fileValue, sharedFileStorage, localFileStorage, this);
        }
      });
    } catch (BindingException e) {
      logger.error("Failed to stage input files", e);
      throw new BindingException("Failed to stage input files", e);
    }
  }
  
  private DirectoryValue stageDirectory(final DirectoryValue directoryValue, final SharedFileStorage sharedFileStorage, final LocalFileStorage localFileStorage, final FileTransformer fileTransformer) throws BindingException {
    String location = directoryValue.getPath();
    if (!location.startsWith(sharedFileStorage.getBaseDir())) {
      if (!location.startsWith("/")) {
        location = new File(localFileStorage.getBaseDir(), location).getAbsolutePath();  
      }
      if (!location.startsWith(DOCKER_PATH_PREFIX)) {
        String mappedLocation = replacePrefix(location, localFileStorage.getBaseDir(), sharedFileStorage.getBaseDir());
        
        File destinationFile = new File(mappedLocation);
        try {
          FileUtils.copyDirectory(new File(location), destinationFile);
        } catch (IOException e) {
          throw new RuntimeException("Failed to copy file from " + location + " to " + destinationFile);
        }
        directoryValue.setPath(mappedLocation);
        directoryValue.setName(destinationFile.getName());
        directoryValue.setLocation(mappedLocation);
      }
    }
    
    List<FileValue> directoryListing = directoryValue.getListing();
    if (directoryListing != null) {
      for (FileValue listingFile : directoryListing) {
        fileTransformer.transform(listingFile);
      }
    }
    return directoryValue;
  }
  
  private FileValue stageFile(final FileValue fileValue, final SharedFileStorage sharedFileStorage, final LocalFileStorage localFileStorage, final FileTransformer fileTransformer) throws BindingException {
    String location = fileValue.getPath();
    if (!location.startsWith(sharedFileStorage.getBaseDir())) {
      if (!location.startsWith(DOCKER_PATH_PREFIX)) {
        if (!location.startsWith("/")) {
          location = new File(localFileStorage.getBaseDir(), location).getAbsolutePath();  
        }
        String mappedLocation = replacePrefix(location, localFileStorage.getBaseDir(), sharedFileStorage.getBaseDir());
        
        File destinationFile = new File(mappedLocation);
        if (!destinationFile.exists()) {
          try {
            FileUtils.copyFile(new File(location), destinationFile);
          } catch (IOException e) {
            throw new RuntimeException("Failed to copy file from " + location + " to " + destinationFile);
          }
        }
        fileValue.setName(destinationFile.getName());
        fileValue.setPath(mappedLocation);
        fileValue.setLocation(mappedLocation);
      }
    }
    List<FileValue> secondaryFiles = fileValue.getSecondaryFiles();
    if (secondaryFiles != null) {
      for (FileValue secondaryFile : secondaryFiles) {
        fileTransformer.transform(secondaryFile);
      }
    }
    return fileValue;
  }
  
  private String replacePrefix(String src, String toMatch, String toReplace) {
    return toReplace + src.substring(toMatch.length());
  }
}
