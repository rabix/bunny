package org.rabix.engine.processor.handler.impl;

import org.rabix.engine.event.impl.ScatterJobEvent;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.store.model.JobRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ScatterJobEventHandler implements EventHandler<ScatterJobEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ScatterJobEventHandler.class);

    private final ScatterHandler scatterHandler;
    private final JobRecordService jobRecordService;

    @Inject
    public ScatterJobEventHandler(ScatterHandler scatterHandler, JobRecordService jobRecordService) {
        this.scatterHandler = scatterHandler;
        this.jobRecordService = jobRecordService;
    }

    @Override
    public void handle(ScatterJobEvent event, EventHandlingMode mode) throws EventHandlerException {
        JobRecord jobRecord = jobRecordService.find(event.getScatterWrapperId(), event.getContextId());
        if (jobRecord == null) {
            logger.warn("Cannot scatter {} of {}. No job record was found.", event.getScatterWrapperId(), event.getContextId());
            return;
        }

        scatterHandler.scatterPort(jobRecord, event.getEvent(), event.getPortId(), event.getValue(), event.getPosition(), event.getNumberOfScatteredFromEvent(), event.isLookAhead(), event.isFromEvent());
    }
}
