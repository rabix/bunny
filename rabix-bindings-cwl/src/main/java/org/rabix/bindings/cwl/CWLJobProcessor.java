package org.rabix.bindings.cwl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.rabix.bindings.cwl.bean.CWLDataLink;
import org.rabix.bindings.cwl.bean.CWLInputPort;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLJobApp;
import org.rabix.bindings.cwl.bean.CWLOutputPort;
import org.rabix.bindings.cwl.bean.CWLStep;
import org.rabix.bindings.cwl.bean.CWLWorkflow;
import org.rabix.bindings.cwl.bean.resource.CWLResource;
import org.rabix.bindings.cwl.helper.CWLBindingHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
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
public class CWLJobProcessor implements BeanProcessor<CWLJob> {

  private final static Logger logger = LoggerFactory.getLogger(CWLJobProcessor.class);
  
  public static final String DOT_SEPARATOR = ".";
  public static final String SLASH_SEPARATOR = "/";
  
  public CWLJob process(CWLJob job) throws BeanProcessorException {
    try {
      return process(null, job);
    } catch (CWLException e) {
      logger.error("Failed to process Job.", e);
      throw new BeanProcessorException(e);
    }
  }
  
  private CWLJob process(CWLJob parentJob, CWLJob job) throws CWLException {
    if (job.getId() == null) {
      String workflowId = parentJob != null ? parentJob.getId() : null;
      String id = workflowId != null? workflowId + DOT_SEPARATOR + InternalSchemaHelper.ROOT_NAME : InternalSchemaHelper.ROOT_NAME;
      job.setId(id);
    }
    processElements(null, job);

    if (job.getApp().isWorkflow()) {
      CWLWorkflow workflow = (CWLWorkflow) job.getApp();
      for (CWLStep step : workflow.getSteps()) {
        step.setId(Draft2ToCWLConverter.convertStepID(step.getId()));
        
        CWLJob stepJob = step.getJob();
        String stepId = job.getId() + DOT_SEPARATOR + CWLSchemaHelper.normalizeId(step.getId());
        stepJob.setId(stepId);
        processResources(job.getApp(), stepJob.getApp());
        processElements(job, stepJob);
        process(job, stepJob);
      }
    }
    return job;
  }
  
  private void processResources(CWLJobApp parentJob, CWLJobApp stepJob) {
    for(CWLResource resource: parentJob.getRequirements()) {
      stepJob.setHint(resource);
      stepJob.setRequirement(resource);
    }
  }
  
  
  /**
   * Process Job inputs, outputs and data links
   */
  private void processElements(CWLJob parentJob, CWLJob job) throws CWLException {
    CWLJobApp app = job.getApp();
    for (CWLInputPort port : app.getInputs()) {
      port.setId(Draft2ToCWLConverter.convertPortID(port.getId()));
    }
    for (CWLOutputPort port : app.getOutputs()) {
      port.setId(Draft2ToCWLConverter.convertPortID(port.getId()));
    }
    if (app.isWorkflow()) {
      CWLWorkflow workflow = (CWLWorkflow) app;
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
  private void createDataLinks(CWLWorkflow workflow) throws CWLException {
    for (CWLOutputPort port : workflow.getOutputs()) {
      port.setId(Draft2ToCWLConverter.convertPortID(port.getId()));
      
      List<String> sources = transformSource(port.getSource());
      for (int position = 0; position < sources.size(); position++) {
        String destination = port.getId();
        LinkMerge linkMerge = port.getLinkMerge() != null? LinkMerge.valueOf(port.getLinkMerge()) : LinkMerge.merge_nested;
        
        String source = sources.get(position);
        source = Draft2ToCWLConverter.convertSource(source);
        source = CWLSchemaHelper.normalizeId(source);
        CWLDataLink dataLink = new CWLDataLink(source, destination, linkMerge, position + 1);
        workflow.addDataLink(dataLink);
      }
    }
    for (CWLStep step : workflow.getSteps()) {
      step.setId(Draft2ToCWLConverter.convertStepID(step.getId()));
      
      List<CWLDataLink> dataLinks = new ArrayList<>();
      for (Map<String, Object> input : step.getInputs()) {
        
        List<String> sources = transformSource(CWLBindingHelper.getSource(input));
        for (int position = 0; position < sources.size(); position++) {
          String destination = CWLBindingHelper.getId(input);
          destination = Draft2ToCWLConverter.convertDestinationId(destination);
          destination = step.getId() + SLASH_SEPARATOR + destination;
          LinkMerge linkMerge = CWLBindingHelper.getLinkMerge(input) != null ? LinkMerge.valueOf(CWLBindingHelper.getLinkMerge(input)) : LinkMerge.merge_nested;
          
          String source = sources.get(position);
          source = Draft2ToCWLConverter.convertSource(source);
          
          source = CWLSchemaHelper.normalizeId(source);
          CWLDataLink dataLink = new CWLDataLink(source, destination, linkMerge, position + 1);
          dataLinks.add(dataLink);
        }
      }
      workflow.addDataLinks(dataLinks);
    }
  }

  @SuppressWarnings("unchecked")
  private List<String> transformSource(Object source) throws CWLException {
    if (source == null) {
      return Collections.<String> emptyList();
    }
    List<String> transformed = new ArrayList<>();
    if (source instanceof String) {
      transformed.add((String) source);
    } else if (source instanceof List<?>) {
      transformed.addAll((List<? extends String>) source);
    } else {
      throw new CWLException("Failed to process source properties. Invalid application structure.");
    }
    return transformed;
  }

  /**
   * Process input or output ports
   */
  private void processPorts(CWLJob parentJob, CWLJob job, List<? extends ApplicationPort> ports) throws CWLException {
    for (ApplicationPort port : ports) {
      String prefix = job.getId().substring(job.getId().lastIndexOf(DOT_SEPARATOR) + 1) + SLASH_SEPARATOR;
      setScatter(job, prefix, port);  // if it's a container
      if (parentJob != null) {
        // it it's an embedded container
        setScatter(parentJob, prefix, port);
      }
      
      if (parentJob != null && parentJob.getApp().isWorkflow()) {
        // if it's a container
        CWLWorkflow workflowApp = (CWLWorkflow) parentJob.getApp();
        processDataLinks(workflowApp.getDataLinks(), port, job, true);
      }
      if (job != null && job.getApp().isWorkflow()) {
        CWLWorkflow workflowApp = (CWLWorkflow) job.getApp();
        processDataLinks(workflowApp.getDataLinks(), port, job, false);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private void setScatter(CWLJob job, String prefix, ApplicationPort port) throws CWLException {
    Object scatterObj = job.getScatter();;
    if (scatterObj != null) {
      List<String> scatterList = new ArrayList<>();
      if (scatterObj instanceof List<?>) {
        for (String scatter : ((List<String>) scatterObj)) {
          scatterList.add(CWLSchemaHelper.normalizeId(scatter));
        }
      } else if (scatterObj instanceof String) {
        scatterList.add(CWLSchemaHelper.normalizeId((String) scatterObj));
      } else {
        throw new CWLException("Failed to process scatter properties. Invalid application structure.");
      }

      // TODO fix
      for (String scatterStr : scatterList) {
        if (scatterStr.startsWith(prefix)) {
          if ((prefix + CWLSchemaHelper.normalizeId(port.getId())).equals(scatterStr)) {
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
  private void processDataLinks(List<CWLDataLink> dataLinks, ApplicationPort port, CWLJob job, boolean strip) {
    for (CWLDataLink dataLink : dataLinks) {
      String source = dataLink.getSource();
      String destination = dataLink.getDestination();
      
      String scatter = null;
      if (job.getId().contains(DOT_SEPARATOR)) {
        String mod = job.getId().substring(job.getId().indexOf(DOT_SEPARATOR) + 1);
        if (strip) {
          mod = mod.substring(mod.indexOf(DOT_SEPARATOR) + 1);
        }
        scatter = mod + SLASH_SEPARATOR + CWLSchemaHelper.normalizeId(port.getId());
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
