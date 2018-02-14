package org.rabix.backend.tes.client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.rabix.backend.tes.config.TESConfig;
import org.rabix.backend.tes.model.TESCancelTaskRequest;
import org.rabix.backend.tes.model.TESCancelTaskResponse;
import org.rabix.backend.tes.model.TESCreateTaskResponse;
import org.rabix.backend.tes.model.TESGetTaskRequest;
import org.rabix.backend.tes.model.TESTask;
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
    this.httpClient = new OkHttpClient.Builder()
      .connectTimeout(0, TimeUnit.SECONDS)
      .writeTimeout(0, TimeUnit.SECONDS)
      .readTimeout(0, TimeUnit.SECONDS)
      .retryOnConnectionFailure(true)
      .build();
  }

  public TESCreateTaskResponse runTask(TESTask task) throws TESHTTPClientException {
    HttpUrl.Builder httpURLBuilder = new HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .port(port)
        .addPathSegment("v1")
        .addPathSegment("tasks");
    
    String serialized = JSONHelper.writeObject(task);
    Request request = new Request.Builder().url(httpURLBuilder.build()).post(RequestBody.create(MediaType.parse("JSON"), serialized)).build();
    try (Response response = httpClient.newCall(request).execute()) {
      if (response.isSuccessful()) {
        return JSONHelper.readObject(response.body().string(), TESCreateTaskResponse.class);
      } else {
        throw new TESHTTPClientException("Failed to run a Task: " + response.body().string());
      }
    } catch (IOException e) {
      throw new TESHTTPClientException("Failed to run Task", e);
    }
  }

  public TESTask getTask(TESGetTaskRequest req) throws TESHTTPClientException {
    HttpUrl httpURL = new HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .port(port)
        .addPathSegment("v1")
        .addPathSegment("tasks")
        .addPathSegment(req.getId())
        .addQueryParameter("view", req.getView().name())
        .build();
    
    Request request = new Request.Builder().url(httpURL).build();
    try (Response response = httpClient.newCall(request).execute()) {
      return JSONHelper.readObject(response.body().string(), TESTask.class);
    } catch (IOException e) {
      throw new TESHTTPClientException("Failed to get Task entity", e);
    }
  }
  
  public TESCancelTaskResponse cancelTask(TESCancelTaskRequest req) throws TESHTTPClientException {
    HttpUrl httpURL = new HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .port(port)
        .addPathSegment("v1")
        .addPathSegment("tasks")
        .addPathSegment(req.getId() + ":cancel")
        .build();
    
    Request request = new Request.Builder().url(httpURL).method("POST", null).build();
    try (Response response = httpClient.newCall(request).execute()) {
      return JSONHelper.readObject(response.body().string(), TESCancelTaskResponse.class);
    } catch (IOException e) {
      throw new TESHTTPClientException("Failed to get CancelTaskResponse entity", e);
    }
  }

}
