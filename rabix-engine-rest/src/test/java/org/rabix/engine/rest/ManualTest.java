package org.rabix.engine.rest;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.JSONHelper;

public class ManualTest {

  public static void main(String[] args) throws IOException {
    runTask(10);
  }
  
  private static void runTask(int times) throws IOException {
    Client client = ClientBuilder.newClient(new ClientConfig().register(LoggingFilter.class));
    WebTarget webTarget = client.target("http://localhost" + ":" + 8081 + "/v0/engine/jobs");

    Map<String, Object> inputs = JSONHelper.readMap(JSONHelper.readJsonNode(FileUtils.readFileToString(new File("/Users/janko/Desktop/Archive/varscan.inputs.yaml"))));
    Job job = new Job("file:///Users/janko/Desktop/Archive/varscan.wf.yaml", inputs);

    Invocation.Builder invocationBuilder = webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).header("batch", times);
    invocationBuilder.post(Entity.entity(job, javax.ws.rs.core.MediaType.APPLICATION_JSON));
  }
  
}
