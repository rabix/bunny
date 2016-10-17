package org.rabix.bindings.helper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.rabix.common.helper.EncodingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URIHelper {

  private final static Logger logger = LoggerFactory.getLogger(URIHelper.class);

  public static final  String FRAGMENT_SEPARATOR = "#";
  private static final String DEFAULT_ENCODING = "UTF-8";
  
  public final static String FTP_URI_SCHEME = "ftp://";
  public final static String HTTP_URI_SCHEME = "http";
  public final static String DATA_URI_SCHEME = "data";
  public final static String FILE_URI_SCHEME = "file://";
  
  private final static String DATA_URI_BASE_64 = "base64";
  private final static String DATA_URI_PAYLOAD_SEPARATOR = ",";
  private final static String DATA_URI_MEDIA_TYPE_DEFAULT = "text/plain";
  
  // TODO move to configuration
  private final static int FTP_PORT = 2221;
  private final static String FTP_HOST = "localhost";
  private final static String FTP_USERNAME = "username";
  private final static String FTP_PASSWORD = "password";
  
  public static String getData(String uri) throws IOException {
    if (isFTP(uri)) {
      return fetchFromFTP(uri);
    }
    if (isHTTP(uri)) {
      return fetchFromHTTP(extractBase(uri));
    }
    if (isFile(uri)) {
      return loadFromFile(extractBase(uri));
    }
    if (isData(uri)) {
      return loadData(uri);
    }
    return uri;
  }
  
  public static String extractBase(String uri) {
    if (uri.contains(FRAGMENT_SEPARATOR)) {
      return uri.substring(0, uri.lastIndexOf(FRAGMENT_SEPARATOR));
    }
    return uri;
  }
  
  public static String extractFragment(String uri) {
    if (uri.contains(FRAGMENT_SEPARATOR)) {
      return uri.substring(uri.lastIndexOf(FRAGMENT_SEPARATOR));
    }
    return uri;
  }
  
  public static String createURI(String scheme, String payload) {
    return scheme + payload;
  }
  
  public static String createDataURI(String payload) {
    return DATA_URI_SCHEME + ":" + DATA_URI_MEDIA_TYPE_DEFAULT + ";" + DATA_URI_BASE_64 + "," + EncodingHelper.encodeBase64(payload);
  }
  
  public static boolean isFTP(String uri) {
    return uri.startsWith(FTP_URI_SCHEME);
  }
  
  public static boolean isFile(String uri) {
    return uri.startsWith(FILE_URI_SCHEME);
  }
  
  public static boolean isData(String uri) {
    return uri.startsWith(DATA_URI_SCHEME);
  }
  
  public static boolean isHTTP(String uri) {
    return uri.startsWith(HTTP_URI_SCHEME);
  }
  
  public static String getURIInfo(String uri) {
    if (uri == null) {
      return null;
    }
    if (isFTP(uri)) {
      return uri.substring(FTP_URI_SCHEME.length());
    }
    if (isFile(uri)) {
      return uri.substring(FILE_URI_SCHEME.length());
    }
    if (isData(uri)) {
      return uri;
    }
    if (isHTTP(uri)) {
      return uri.substring(HTTP_URI_SCHEME.length() + 1);
    }
    return null;
  }

  private static String fetchFromFTP(String uri) {
    String ftpPath = getURIInfo(uri);
    
    File appDirectory;
    try {
      appDirectory = createTempDirectory();
      
      File localFile = downloadFromFTP(appDirectory, ftpPath);
      return FileUtils.readFileToString(localFile);
    } catch (IOException e) {
      // TODO handle
    }
    return null;
  }
  
  public static File createTempDirectory() throws IOException {
    final File temp;

    temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
    if (!(temp.delete())) {
      throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
    }

    if (!(temp.mkdir())) {
      throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
    }

    return (temp);
  }
  
  private static String fetchFromHTTP(String uri) throws IOException {
    URL website = new URL(uri);

    URLConnection connection = website.openConnection();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      StringBuilder response = new StringBuilder();
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      return response.toString();
    } catch (IOException e) {
      logger.error("Failed to load data from URL " + uri, e);
      throw e;
    }
  }

  private static String loadFromFile(String uri) throws IOException {
    try {
      String filePath = getURIInfo(uri);
      String absoluteFilePath = new File(filePath).getCanonicalPath();
      return FileUtils.readFileToString(new File(absoluteFilePath), DEFAULT_ENCODING);
    } catch (IOException e) {
      logger.error("Failed to load data from file " + uri, e);
      throw e;
    }
  }

  private static String loadData(String uri) throws IOException {
    String data = getURIInfo(uri);
    String headers = data.substring(0, data.indexOf(DATA_URI_PAYLOAD_SEPARATOR));
    String payload = data.substring(data.indexOf(DATA_URI_PAYLOAD_SEPARATOR) + 1);

    boolean isBase64Encoded = headers.contains(DATA_URI_BASE_64);
    if (isBase64Encoded) {
      try {
        return new String(Base64.decodeBase64(payload), DEFAULT_ENCODING);
      } catch (UnsupportedEncodingException e) {
        logger.error("Failed to decode payload from URI " + uri);
        throw new IOException(e);
      }
    }
    return payload;
  }
  
  private static File downloadFromFTP(File workingDir, String remotePath) throws IOException {
    FTPClient ftpClient = new FTPClient();
    try {
      ftpClient.connect(FTP_HOST, FTP_PORT);
      ftpClient.login(FTP_USERNAME, FTP_PASSWORD);
      ftpClient.enterLocalPassiveMode();
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

      File localWorkingDir = workingDir;
      String[] parts = remotePath.split(File.separator);
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
      boolean success = ftpClient.retrieveFile(remotePath, os);
      os.close();

      if (success) {
        logger.debug("File {} has been downloaded successfully.", remotePath);
      }
      return file;
    } catch (IOException e) {
      throw e;
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
  
}
