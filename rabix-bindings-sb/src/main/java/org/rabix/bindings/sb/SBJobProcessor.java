package org.rabix.bindings.sb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.sb.bean.SBDataLink;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.bean.SBJobApp;
import org.rabix.bindings.sb.bean.SBOutputPort;
import org.rabix.bindings.sb.bean.SBStep;
import org.rabix.bindings.sb.bean.SBWorkflow;
import org.rabix.bindings.sb.helper.SBBindingHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.common.json.processor.BeanProcessor;
import org.rabix.common.json.processor.BeanProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BeanProcessor} used for Job processing. It populates some additional fields.
 */
public class SBJobProcessor implements BeanProcessor<SBJob> {

  private final static Logger logger = LoggerFactory.getLogger(SBJobProcessor.class);
  
  public SBJob process(SBJob job) throws BeanProcessorException {
    try {
      return process(null, job);
    } catch (SBException e) {
      logger.error("Failed to process Job.", e);
      throw new BeanProcessorException(e);
    }
  }
  
  private SBJob process(SBJob parentJob, SBJob job) throws SBException {
    if (job.getId() == null) {
      String workflowId = parentJob != null ? parentJob.getId() : null;
      String id = workflowId != null? workflowId + SBSchemaHelper.PORT_ID_SEPARATOR + InternalSchemaHelper.ROOT_NAME : InternalSchemaHelper.ROOT_NAME;
      job.setId(id);
    }
    if(job.getApp().getCwlVersion() == null && parentJob != null) {
      job.getApp().setCwlVersion(parentJob.getApp().getCwlVersion());
    }
    processElements(null, job);

    if (job.getApp().isWorkflow()) {
      SBWorkflow workflow = (SBWorkflow) job.getApp();
      for (SBStep step : workflow.getSteps()) {
        SBJob stepJob = step.getJob();
        String stepId = job.getId() + SBSchemaHelper.PORT_ID_SEPARATOR + SBSchemaHelper.normalizeId(step.getId());
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
  private void processElements(SBJob parentJob, SBJob job) throws SBException {
    SBJobApp app = job.getApp();
    if (app.isWorkflow()) {
      SBWorkflow workflow = (SBWorkflow) app;
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
  private void createDataLinks(SBWorkflow workflow) throws SBException {
    for (SBOutputPort port : workflow.getOutputs()) {
      List<String> sources = transformSource(port.getSource());
      for (int position = 0; position < sources.size(); position++) {
        String destination = port.getId();
        LinkMerge linkMerge = port.getLinkMerge() != null? LinkMerge.valueOf(port.getLinkMerge()) : LinkMerge.merge_nested;
        SBDataLink dataLink = new SBDataLink(sources.get(position), destination, linkMerge, position + 1);
        workflow.addDataLink(dataLink);
      }
    }
    for (SBStep step : workflow.getSteps()) {
      List<SBDataLink> dataLinks = new ArrayList<>();
      for (Map<String, Object> input : step.getInputs()) {
        List<String> sources = transformSource(SBBindingHelper.getSource(input));
        for (int position = 0; position < sources.size(); position++) {
          String destination = SBBindingHelper.getId(input);
          LinkMerge linkMerge = SBBindingHelper.getLinkMerge(input) != null ? LinkMerge.valueOf(SBBindingHelper.getLinkMerge(input)) : LinkMerge.merge_nested;
          SBDataLink dataLink = new SBDataLink(sources.get(position), destination, linkMerge, position + 1);
          dataLinks.add(dataLink);
        }
      }
      workflow.addDataLinks(dataLinks);
    }
  }

  @SuppressWarnings("unchecked")
  private List<String> transformSource(Object source) throws SBException {
    if (source == null) {
      return Collections.<String> emptyList();
    }
    List<String> transformed = new ArrayList<>();
    if (source instanceof String) {
      transformed.add((String) source);
    } else if (source instanceof List<?>) {
      transformed.addAll((List<? extends String>) source);
    } else {
      throw new SBException("Failed to process source properties. Invalid application structure.");
    }
    return transformed;
  }

  /**
   * Process input or output ports
   */
  private void processPorts(SBJob parentJob, SBJob job, List<? extends ApplicationPort> ports) throws SBException {
    for (ApplicationPort port : ports) {
      String prefix = job.getId().substring(job.getId().lastIndexOf(SBSchemaHelper.PORT_ID_SEPARATOR) + 1) + SBSchemaHelper.PORT_ID_SEPARATOR;
      setScatter(job, prefix, port);  // if it's a container
      if (parentJob != null) {
        // it it's an embedded container
        setScatter(parentJob, prefix, port);
      }
      
      if (parentJob != null && parentJob.getApp().isWorkflow()) {
        // if it's a container
        SBWorkflow workflowApp = (SBWorkflow) parentJob.getApp();
        processDataLinks(workflowApp.getDataLinks(), port, job, true);
      }
      if (job != null && job.getApp().isWorkflow()) {
        SBWorkflow workflowApp = (SBWorkflow) job.getApp();
        processDataLinks(workflowApp.getDataLinks(), port, job, false);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private void setScatter(SBJob job, String prefix, ApplicationPort port) throws SBException {
    Object scatterObj = job.getScatter();;
    if (scatterObj != null) {
      List<String> scatterList = new ArrayList<>();
      if (scatterObj instanceof List<?>) {
        for (String scatter : ((List<String>) scatterObj)) {
          scatterList.add(SBSchemaHelper.normalizeId(scatter));
        }
      } else if (scatterObj instanceof String) {
        scatterList.add(SBSchemaHelper.normalizeId((String) scatterObj));
      } else {
        throw new SBException("Failed to process scatter properties. Invalid application structure.");
      }

      // TODO fix
      for (String scatterStr : scatterList) {
        if (scatterStr.startsWith(prefix)) {
          if ((prefix + SBSchemaHelper.normalizeId(port.getId())).equals(scatterStr)) {
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
  private void processDataLinks(List<SBDataLink> dataLinks, ApplicationPort port, SBJob job, boolean strip) {
    for (SBDataLink dataLink : dataLinks) {
      String source = dataLink.getSource();
      String destination = dataLink.getDestination();
      
      String scatter = null;
      if (job.getId().contains(SBSchemaHelper.PORT_ID_SEPARATOR)) {
        String remaining = job.getId().substring(job.getId().lastIndexOf(".") + 1);
        String mod = job.getId().substring(0, job.getId().lastIndexOf("."));
        if (mod.contains(".")) {
          mod = mod.substring(mod.lastIndexOf(".") + 1);
        }
        if (strip) {
          mod = remaining;
        } else {
          mod = mod + remaining;
        }
        scatter = SBSchemaHelper.ID_START + mod + SBSchemaHelper.PORT_ID_SEPARATOR + SBSchemaHelper.normalizeId(port.getId());
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
