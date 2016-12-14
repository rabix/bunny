package org.rabix.backend.local.tes.client;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.rabix.backend.local.tes.config.TESConfig;
import org.rabix.backend.local.tes.model.TESJob;
import org.rabix.backend.local.tes.model.TESJobId;
import org.rabix.backend.local.tes.model.TESJobListRequest;
import org.rabix.backend.local.tes.model.TESJobListResponse;
import org.rabix.backend.local.tes.model.TESServiceInfo;
import org.rabix.backend.local.tes.model.TESTask;
import org.rabix.common.helper.JSONHelper;

import com.google.inject.Inject;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TESHttpClient {

  private final int port;
  private final String host;
  private final String scheme;
  
  private final OkHttpClient httpClient;
  
  @Inject
  public TESHttpClient(TESConfig tesConfig) {
    this.host = tesConfig.getHost();
    this.port = tesConfig.getPort();
    this.scheme = tesConfig.getScheme();
    
    this.httpClient = new OkHttpClient();
  }
  
  public TESServiceInfo getServiceInfo() throws TESHTTPClientException {
    HttpUrl httpURL = new HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .port(port)
        .addPathSegment("v1/jobs-service")
        .build();
    
    Request request = new Request.Builder().url(httpURL).get().build();
    try (Response response = httpClient.newCall(request).execute()) {
      return JSONHelper.readObject(response.body().string(), TESServiceInfo.class);
    } catch (IOException e) {
      throw new TESHTTPClientException("Failed to get ServiceInfo entity", e);
    }
  }
  
  public TESJobId runTask(TESTask task) throws TESHTTPClientException {
    HttpUrl.Builder httpURLBuilder = new HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .port(port)
        .addPathSegment("v1/jobs");
    
    String serialized = JSONHelper.writeObject(task);
    Request request = new Request.Builder().url(httpURLBuilder.build()).post(RequestBody.create(MediaType.parse("JSON"), serialized)).build();
    try (Response response = httpClient.newCall(request).execute()) {
      if (response.isSuccessful()) {
        return JSONHelper.readObject(response.body().string(), TESJobId.class);
      } else {
        throw new TESHTTPClientException("Failed to run a Task");
      }
    } catch (IOException e) {
      throw new TESHTTPClientException("Failed to run Task", e);
    }
  }
  
  public TESJobListResponse listJobs(TESJobListRequest jobListRequest) throws TESHTTPClientException {
    HttpUrl.Builder httpURLBuilder = new HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .port(port)
        .addPathSegment("v1/jobs");
    
    if (jobListRequest != null) {
      if (StringUtils.isEmpty(jobListRequest.getNamePrefix())) {
        httpURLBuilder.setQueryParameter("namePrefix", jobListRequest.getNamePrefix());
      }
      if (StringUtils.isEmpty(jobListRequest.getProjectId())) {
        httpURLBuilder.setQueryParameter("projectID", jobListRequest.getProjectId());
      }
      if (jobListRequest.getPageSize() != null) {
        httpURLBuilder.setQueryParameter("pageSize", jobListRequest.getPageSize().toString());
      }
      if (StringUtils.isEmpty(jobListRequest.getPageToken())) {
        httpURLBuilder.setQueryParameter("pageToken", jobListRequest.getPageToken());
      }
    }
    
    Request request = new Request.Builder().url(httpURLBuilder.build()).get().build();
    try (Response response = httpClient.newCall(request).execute()) {
      if (response.isSuccessful()) {
        return JSONHelper.readObject(response.body().string(), TESJobListResponse.class);
      } else {
        throw new TESHTTPClientException("Failed to run a Task");
      }
    } catch (IOException e) {
      throw new TESHTTPClientException("Failed to get ServiceInfo entity", e);
    }
  }
  
  public TESJob getJob(TESJobId jobId) throws TESHTTPClientException {
    HttpUrl httpURL = new HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .port(port)
        .addPathSegment("v1/jobs")
        .addPathSegment(jobId.getValue())
        .build();
    
    Request request = new Request.Builder().url(httpURL).build();
    try (Response response = httpClient.newCall(request).execute()) {
      return JSONHelper.readObject(response.body().string(), TESJob.class);
    } catch (IOException e) {
      throw new TESHTTPClientException("Failed to get ServiceInfo entity", e);
    }
  }
  
  public TESJobId cancelJob(TESJobId jobId) throws TESHTTPClientException {
    HttpUrl httpURL = new HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .port(port)
        .addPathSegment("v1/jobs")
        .addPathSegment(jobId.getValue())
        .build();
    
    Request request = new Request.Builder().url(httpURL).method("DELETE", null).build();
    try (Response response = httpClient.newCall(request).execute()) {
      return JSONHelper.readObject(response.body().string(), TESJobId.class);
    } catch (IOException e) {
      throw new TESHTTPClientException("Failed to get ServiceInfo entity", e);
    }
  }
  
  
}
