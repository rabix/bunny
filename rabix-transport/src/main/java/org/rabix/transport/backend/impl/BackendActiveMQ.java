package org.rabix.transport.backend.impl;

import org.rabix.transport.backend.Backend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BackendActiveMQ extends Backend {

  @JsonProperty("broker")
  private String broker;
  @JsonProperty("toBackendQueue")
  private String toBackendQueue;
  @JsonProperty("toBackendControlQueue")
  private String toBackendControlQueue;
  @JsonProperty("fromBackendQueue")
  private String fromBackendQueue;
  @JsonProperty("fromBackendHeartbeatQueue")
  private String fromBackendHeartbeatQueue;
  
  public BackendActiveMQ(@JsonProperty("id") String id, @JsonProperty("broker") String broker, @JsonProperty("toBackendQueue") String toBackendQueue, @JsonProperty("toBackendControlQueue") String toBackendControlQueue, @JsonProperty("fromBackendQueue") String fromBackendQueue, @JsonProperty("fromBackendHeartbeatQueue") String fromBackendHeartbeatQueue) {
    this.id = id;
    this.broker = broker;
    this.toBackendQueue = toBackendQueue;
    this.toBackendControlQueue = toBackendControlQueue;
    this.fromBackendQueue = fromBackendQueue;
    this.fromBackendHeartbeatQueue = fromBackendHeartbeatQueue;
  }

  public String getBroker() {
    return broker;
  }

  public void setBroker(String broker) {
    this.broker = broker;
  }

  public String getToBackendQueue() {
    return toBackendQueue;
  }

  public void setToBackendQueue(String toBackendQueue) {
    this.toBackendQueue = toBackendQueue;
  }

  public String getFromBackendQueue() {
    return fromBackendQueue;
  }

  public void setFromBackendQueue(String fromBackendQueue) {
    this.fromBackendQueue = fromBackendQueue;
  }

  public String getFromBackendHeartbeatQueue() {
    return fromBackendHeartbeatQueue;
  }

  public void setFromBackendHeartbeatQueue(String fromBackendHeartbeatQueue) {
    this.fromBackendHeartbeatQueue = fromBackendHeartbeatQueue;
  }

  public String getToBackendControlQueue() {
    return toBackendControlQueue;
  }

  public void setToBackendControlQueue(String toBackendControlQueue) {
    this.toBackendControlQueue = toBackendControlQueue;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((broker == null) ? 0 : broker.hashCode());
    result = prime * result + ((fromBackendHeartbeatQueue == null) ? 0 : fromBackendHeartbeatQueue.hashCode());
    result = prime * result + ((fromBackendQueue == null) ? 0 : fromBackendQueue.hashCode());
    result = prime * result + ((toBackendQueue == null) ? 0 : toBackendQueue.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    BackendActiveMQ other = (BackendActiveMQ) obj;
    if (broker == null) {
      if (other.broker != null)
        return false;
    } else if (!broker.equals(other.broker))
      return false;
    if (fromBackendHeartbeatQueue == null) {
      if (other.fromBackendHeartbeatQueue != null)
        return false;
    } else if (!fromBackendHeartbeatQueue.equals(other.fromBackendHeartbeatQueue))
      return false;
    if (fromBackendQueue == null) {
      if (other.fromBackendQueue != null)
        return false;
    } else if (!fromBackendQueue.equals(other.fromBackendQueue))
      return false;
    if (toBackendQueue == null) {
      if (other.toBackendQueue != null)
        return false;
    } else if (!toBackendQueue.equals(other.toBackendQueue))
      return false;
    return true;
  }

  @Override
  @JsonIgnore
  public BackendType getType() {
    return BackendType.ACTIVE_MQ;
  }

  @Override
  public String toString() {
    return "BackendActiveMQ [broker=" + broker + ", toBackendQueue=" + toBackendQueue + ", toBackendControlQueue=" + toBackendControlQueue + ", fromBackendQueue=" + fromBackendQueue + ", fromBackendHeartbeatQueue=" + fromBackendHeartbeatQueue + "]";
  }

}
