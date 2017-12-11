package org.rabix.engine.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.rabix.engine.event.Event;

import java.util.UUID;

public class ScatterJobEvent implements Event {

    @JsonProperty("contextId")
    private final UUID contextId;

    @JsonProperty("eventGroupId")
    private final UUID eventGroupId;

    @JsonProperty("scatterWrapperId")
    private final String scatterWrapperId;

    @JsonProperty("originEvent")
    private final Event event;

    @JsonProperty("portId")
    private final String portId;

    @JsonProperty("value")
    private final Object value;

    @JsonProperty("position")
    private final Integer position;

    @JsonProperty("numberOfScatteredFromEvent")
    private final Integer numberOfScatteredFromEvent;

    @JsonProperty("isLookAhead")
    private final boolean isLookAhead;

    @JsonProperty("isFromEvent")
    private final boolean isFromEvent;

    public ScatterJobEvent(@JsonProperty("contextId") UUID contextId,
                           @JsonProperty("eventGroupId") UUID eventGroupId,
                           @JsonProperty("scatterWrapperId") String scatterWrapperId,
                           @JsonProperty("originEvent") Event event,
                           @JsonProperty("portId") String portId,
                           @JsonProperty("value") Object value,
                           @JsonProperty("position") Integer position,
                           @JsonProperty("numberOfScatteredFromEvent") Integer numberOfScatteredFromEvent,
                           @JsonProperty("isLookAhead") boolean isLookAhead,
                           @JsonProperty("isFromEvent") boolean isFromEvent) {
        this.contextId = contextId;
        this.eventGroupId = eventGroupId;
        this.scatterWrapperId = scatterWrapperId;
        this.event = event;
        this.portId = portId;
        this.value = value;
        this.position = position;
        this.numberOfScatteredFromEvent = numberOfScatteredFromEvent;
        this.isLookAhead = isLookAhead;
        this.isFromEvent = isFromEvent;
    }

    @Override
    public EventType getType() {
        return EventType.CREATE_JOB_RECORDS;
    }

    @Override
    public UUID getContextId() {
        return contextId;
    }

    @Override
    public UUID getEventGroupId() {
        return eventGroupId;
    }

    @Override
    public String getProducedByNode() {
        return null;
    }

    public String getScatterWrapperId() {
        return scatterWrapperId;
    }

    public Event getEvent() {
        return event;
    }

    public String getPortId() {
        return portId;
    }

    public Object getValue() {
        return value;
    }

    public Integer getPosition() {
        return position;
    }

    public Integer getNumberOfScatteredFromEvent() {
        return numberOfScatteredFromEvent;
    }

    public boolean isLookAhead() {
        return isLookAhead;
    }

    public boolean isFromEvent() {
        return isFromEvent;
    }
}
