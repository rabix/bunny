package org.rabix.ftp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.rabix.common.service.download.DownloadService;
import org.rabix.common.service.download.DownloadServiceException;
import org.rabix.common.service.upload.UploadService;
import org.rabix.common.service.upload.UploadServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class SimpleFTPClient implements DownloadService, UploadService {

  private final static Logger logger = LoggerFactory.getLogger(SimpleFTPClient.class);
  
  private int port;
  private String host;
  private String username;
  private String password;

  @Inject
  public SimpleFTPClient(Configuration configuration) {
    this.port = FTPConfig.getPort(configuration);
    this.host = FTPConfig.getHost(configuration);
    this.username = FTPConfig.getUsername(configuration);
    this.password = FTPConfig.getPassword(configuration);
  }

  public void download(File workingDir, DownloadResource remotePath, Map<String, Object> config) throws DownloadServiceException {
    FTPClient ftpClient = new FTPClient();
    try {
      ftpClient.connect(host, port);
      ftpClient.login(username, password);
      ftpClient.enterLocalPassiveMode();
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

      File localWorkingDir = workingDir;
      String[] parts = remotePath.getPath().split(File.separator);
      for (int i = 0; i < parts.length - 1; i++) {
        if (parts[i].isEmpty()) {
          continue;
        }
        String remoteWorkingDir = parts[i];
        localWorkingDir = new File(localWorkingDir, remoteWorkingDir);
        if (!localWorkingDir.exists()) {
          localWorkingDir.mkdirs();
        }
      }
      File file = new File(localWorkingDir, parts[parts.length - 1]);
      OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
      boolean success = ftpClient.retrieveFile(remotePath.getPath(), os);
      os.close();

      if (success) {
        logger.debug("File {} has been downloaded successfully.", remotePath);
      }
    } catch (IOException e) {
      throw new DownloadServiceException(e);
    } finally {
      try {
        if (ftpClient.isConnected()) {
          ftpClient.logout();
          ftpClient.disconnect();
        }
      } catch (IOException ex) {
        // do nothing
      }
    }
  }

  @Override
  public void download(File workingDir, Set<DownloadResource> remotePaths, Map<String, Object> config) throws DownloadServiceException {
    for (DownloadResource path : remotePaths) {
      download(workingDir, path, config);
    }
  }
  
  public void upload(File file, File baseExecutionDirectory, boolean wait, boolean create, Map<String, Object> config) throws UploadServiceException {
    FTPClient ftp = new FTPClient();
    int reply;
    try {
      ftp.connect(host, port);

      reply = ftp.getReplyCode();
      if (!FTPReply.isPositiveCompletion(reply)) {
        ftp.disconnect();
        throw new IOException("Exception in connecting to FTP Server");
      }
      ftp.login(username, password);
      ftp.setFileType(FTP.BINARY_FILE_TYPE);
      ftp.enterLocalPassiveMode();

      String remotePath = file.getAbsolutePath().substring(baseExecutionDirectory.getAbsolutePath().length()); // TODO check
      String[] paths = remotePath.split(File.separator);
      
      for(int i = 0; i < paths.length - 1; i++) {
        if (paths[i].isEmpty()) {
          continue;
        }
        boolean exists = changeWorkingDirectory(ftp, paths[i]);
        if (!exists) {
          ftp.makeDirectory(paths[i]);
          changeWorkingDirectory(ftp, paths[i]);
        }
      }
      try (InputStream input = new FileInputStream(new File(file.getAbsolutePath()))) {
        ftp.storeFile(paths[paths.length - 1], input);
      }
      ftp.disconnect();
    } catch (IOException e) {
      throw new UploadServiceException(e);
    }
  }
  
  @Override
  public void upload(Set<File> files, File baseExecutionDirectory,boolean wait,  boolean create, Map<String, Object> config) throws UploadServiceException {
    for (File file : files) {
      upload(file, baseExecutionDirectory, wait, create, config);
    }
  }
  
  private static boolean changeWorkingDirectory(FTPClient ftpClient, String dirPath) throws IOException {
    ftpClient.changeWorkingDirectory(dirPath);
    int returnCode = ftpClient.getReplyCode();
    if (returnCode == 550) {
      return false;
    }
    return true;
  }

}
