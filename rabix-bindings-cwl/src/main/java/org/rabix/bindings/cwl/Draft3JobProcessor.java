package org.rabix.bindings.draft3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.rabix.bindings.draft3.bean.Draft3DataLink;
import org.rabix.bindings.draft3.bean.Draft3InputPort;
import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.bean.Draft3JobApp;
import org.rabix.bindings.draft3.bean.Draft3OutputPort;
import org.rabix.bindings.draft3.bean.Draft3Step;
import org.rabix.bindings.draft3.bean.Draft3Workflow;
import org.rabix.bindings.draft3.helper.Draft3BindingHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.common.json.processor.BeanProcessor;
import org.rabix.common.json.processor.BeanProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BeanProcessor} used for Job processing. It populates some additional fields.
 */
public class Draft3JobProcessor implements BeanProcessor<Draft3Job> {

  private final static Logger logger = LoggerFactory.getLogger(Draft3JobProcessor.class);
  
  public static final String DOT_SEPARATOR = ".";
  public static final String SLASH_SEPARATOR = "/";
  
  public Draft3Job process(Draft3Job job) throws BeanProcessorException {
    try {
      return process(null, job);
    } catch (Draft3Exception e) {
      logger.error("Failed to process Job.", e);
      throw new BeanProcessorException(e);
    }
  }
  
  private Draft3Job process(Draft3Job parentJob, Draft3Job job) throws Draft3Exception {
    if (job.getId() == null) {
      String workflowId = parentJob != null ? parentJob.getId() : null;
      String id = workflowId != null? workflowId + DOT_SEPARATOR + InternalSchemaHelper.ROOT_NAME : InternalSchemaHelper.ROOT_NAME;
      job.setId(id);
    }
    processElements(null, job);

    if (job.getApp().isWorkflow()) {
      Draft3Workflow workflow = (Draft3Workflow) job.getApp();
      for (Draft3Step step : workflow.getSteps()) {
        step.setId(Draft2ToDraft3Converter.convertStepID(step.getId()));
        
        Draft3Job stepJob = step.getJob();
        String stepId = job.getId() + DOT_SEPARATOR + Draft3SchemaHelper.normalizeId(step.getId());
        stepJob.setId(stepId);
        processElements(job, stepJob);
        process(job, stepJob);
      }
    }
    return job;
  }
  
  /**
   * Process Job inputs, outputs and data links
   */
  private void processElements(Draft3Job parentJob, Draft3Job job) throws Draft3Exception {
    Draft3JobApp app = job.getApp();
    for (Draft3InputPort port : app.getInputs()) {
      port.setId(Draft2ToDraft3Converter.convertPortID(port.getId()));
    }
    for (Draft3OutputPort port : app.getOutputs()) {
      port.setId(Draft2ToDraft3Converter.convertPortID(port.getId()));
    }
    if (app.isWorkflow()) {
      Draft3Workflow workflow = (Draft3Workflow) app;
      if (CollectionUtils.isEmpty(workflow.getDataLinks())) {
        createDataLinks(workflow);
      }
    }
    processPorts(parentJob, job, app.getInputs());
    processPorts(parentJob, job, app.getOutputs());
  }

  /**
   * Created data links from source properties
   */
  private void createDataLinks(Draft3Workflow workflow) throws Draft3Exception {
    for (Draft3OutputPort port : workflow.getOutputs()) {
      port.setId(Draft2ToDraft3Converter.convertPortID(port.getId()));
      
      List<String> sources = transformSource(port.getSource());
      for (int position = 0; position < sources.size(); position++) {
        String destination = port.getId();
        LinkMerge linkMerge = port.getLinkMerge() != null? LinkMerge.valueOf(port.getLinkMerge()) : LinkMerge.merge_nested;
        
        String source = sources.get(position);
        source = Draft2ToDraft3Converter.convertSource(source);
        source = Draft3SchemaHelper.normalizeId(source);
        Draft3DataLink dataLink = new Draft3DataLink(source, destination, linkMerge, position + 1);
        workflow.addDataLink(dataLink);
      }
    }
    for (Draft3Step step : workflow.getSteps()) {
      step.setId(Draft2ToDraft3Converter.convertStepID(step.getId()));
      
      List<Draft3DataLink> dataLinks = new ArrayList<>();
      for (Map<String, Object> input : step.getInputs()) {
        
        List<String> sources = transformSource(Draft3BindingHelper.getSource(input));
        for (int position = 0; position < sources.size(); position++) {
          String destination = Draft3BindingHelper.getId(input);
          destination = Draft2ToDraft3Converter.convertDestinationId(destination);
          destination = step.getId() + SLASH_SEPARATOR + destination;
          LinkMerge linkMerge = Draft3BindingHelper.getLinkMerge(input) != null ? LinkMerge.valueOf(Draft3BindingHelper.getLinkMerge(input)) : LinkMerge.merge_nested;
          
          String source = sources.get(position);
          source = Draft2ToDraft3Converter.convertSource(source);
          
          source = Draft3SchemaHelper.normalizeId(source);
          Draft3DataLink dataLink = new Draft3DataLink(source, destination, linkMerge, position + 1);
          dataLinks.add(dataLink);
        }
      }
      workflow.addDataLinks(dataLinks);
    }
  }

  @SuppressWarnings("unchecked")
  private List<String> transformSource(Object source) throws Draft3Exception {
    if (source == null) {
      return Collections.<String> emptyList();
    }
    List<String> transformed = new ArrayList<>();
    if (source instanceof String) {
      transformed.add((String) source);
    } else if (source instanceof List<?>) {
      transformed.addAll((List<? extends String>) source);
    } else {
      throw new Draft3Exception("Failed to process source properties. Invalid application structure.");
    }
    return transformed;
  }

  /**
   * Process input or output ports
   */
  private void processPorts(Draft3Job parentJob, Draft3Job job, List<? extends ApplicationPort> ports) throws Draft3Exception {
    for (ApplicationPort port : ports) {
      String prefix = job.getId().substring(job.getId().lastIndexOf(DOT_SEPARATOR) + 1) + SLASH_SEPARATOR;
      setScatter(job, prefix, port);  // if it's a container
      if (parentJob != null) {
        // it it's an embedded container
        setScatter(parentJob, prefix, port);
      }
      
      if (parentJob != null && parentJob.getApp().isWorkflow()) {
        // if it's a container
        Draft3Workflow workflowApp = (Draft3Workflow) parentJob.getApp();
        processDataLinks(workflowApp.getDataLinks(), port, job, true);
      }
      if (job != null && job.getApp().isWorkflow()) {
        Draft3Workflow workflowApp = (Draft3Workflow) job.getApp();
        processDataLinks(workflowApp.getDataLinks(), port, job, false);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private void setScatter(Draft3Job job, String prefix, ApplicationPort port) throws Draft3Exception {
    Object scatterObj = job.getScatter();;
    if (scatterObj != null) {
      List<String> scatterList = new ArrayList<>();
      if (scatterObj instanceof List<?>) {
        for (String scatter : ((List<String>) scatterObj)) {
          scatterList.add(Draft3SchemaHelper.normalizeId(scatter));
        }
      } else if (scatterObj instanceof String) {
        scatterList.add(Draft3SchemaHelper.normalizeId((String) scatterObj));
      } else {
        throw new Draft3Exception("Failed to process scatter properties. Invalid application structure.");
      }

      // TODO fix
      for (String scatterStr : scatterList) {
        if (scatterStr.startsWith(prefix)) {
          if ((prefix + Draft3SchemaHelper.normalizeId(port.getId())).equals(scatterStr)) {
            if (!(port.getScatter() != null && port.getScatter())) {
              port.setScatter(true);              
            }
            break;
          }
        }
      }
    }
  }

  /**
   * Process data links
   */
  private void processDataLinks(List<Draft3DataLink> dataLinks, ApplicationPort port, Draft3Job job, boolean strip) {
    for (Draft3DataLink dataLink : dataLinks) {
      String source = dataLink.getSource();
      String destination = dataLink.getDestination();
      
      String scatter = null;
      if (job.getId().contains(DOT_SEPARATOR)) {
        String mod = job.getId().substring(job.getId().indexOf(DOT_SEPARATOR) + 1);
        if (strip) {
          mod = mod.substring(mod.indexOf(DOT_SEPARATOR) + 1);
        }
        scatter = mod + SLASH_SEPARATOR + Draft3SchemaHelper.normalizeId(port.getId());
      } else {
        scatter = port.getId();
      }
      
      // TODO fix
      if ((source.equals(scatter) || destination.equals(scatter)) && (dataLink.getScattered() == null || !dataLink.getScattered())) {
        dataLink.setScattered(port.getScatter());
      }
    }
  }
}
