package org.rabix.engine.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.rabix.bindings.model.Job;

public class ManualTest {

  public static void main(String[] args) {
    runTask();
  }
  
  public static void runTask() {
    for (int i=0;i<5000;i++) {
      Client client = ClientBuilder.newClient(new ClientConfig().register(LoggingFilter.class));
      WebTarget webTarget = client.target("http://localhost" + ":" + 8081 + "/v0/engine/jobs");

      Map<String, Object> inputs = new HashMap<>();
      Map<String, Object> file = new HashMap<>();
      file.put("class", "File");
      file.put("path", "/Users/janko/Desktop/examples/dna2protein/data/input.txt");
      inputs.put("input_file", file);

      Job job = new Job("file:///Users/janko/Desktop/examples/dna2protein/dna2protein.cwl.json", inputs);
      
      Invocation.Builder invocationBuilder = webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON);
      invocationBuilder.post(Entity.entity(job, javax.ws.rs.core.MediaType.APPLICATION_JSON));
      System.out.println(i);
    }
  }
  
}
