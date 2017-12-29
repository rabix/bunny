package org.rabix.cli.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.logging.VerboseLogger;
import org.rabix.engine.service.BackendService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.status.EngineStatusCallbackException;
import org.rabix.engine.status.impl.DefaultEngineStatusCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

public class LocalBackendEngineStatusCallback extends DefaultEngineStatusCallback {

    private final Logger logger = LoggerFactory.getLogger(LocalBackendEngineStatusCallback.class);

    private final JobService jobService;

    @Inject
    public LocalBackendEngineStatusCallback(BackendService backendService, JobService jobService) {
        super(backendService);
        this.jobService = jobService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onJobRootCompleted(UUID rootId) throws EngineStatusCallbackException {
        Job rootJob = jobService.get(rootId);
        if (rootJob.getStatus().equals(Job.JobStatus.COMPLETED)) {
            try {
                try {
                    Bindings bindings = BindingsFactory.create(rootJob);
                    Map<String, Object> outputs = (Map<String, Object>) bindings.translateToSpecific(rootJob.getOutputs());
                    System.out.println(JSONHelper.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outputs));
                    System.exit(0);
                } catch (BindingException e) {
                    logger.error("Failed to translate common outputs to native", e);
                    throw new RuntimeException(e);
                }
            } catch (JsonProcessingException e) {
                logger.error("Failed to write outputs to standard out", e);
                System.exit(10);
            }
        } else {
            VerboseLogger.log("Failed to execute a Job");
            System.exit(10);
        }
    }
    
    @Override
    public void onJobRootFailed(UUID rootId, String message) throws EngineStatusCallbackException {
      System.out.println(message);
      System.exit(1);
    }
}
