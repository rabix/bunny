package org.rabix.transport.backend;

import java.util.UUID;

import org.rabix.common.json.BeanPropertyView;
import org.rabix.common.json.BeanSerializer;
import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendSlurm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
    @Type(value = BackendActiveMQ.class, name = "ACTIVE_MQ"),
    @Type(value = BackendRabbitMQ.class, name = "RABBIT_MQ"),
    @Type(value = BackendSlurm.class, name = "SLURM"),
    @Type(value = BackendLocal.class, name = "LOCAL") })
@JsonInclude(Include.NON_NULL)
public abstract class Backend {

  public static enum BackendStatus {
    ACTIVE,
    INACTIVE
  }
  
  @JsonProperty("id")
  @JsonView(BeanPropertyView.Full.class)
  protected UUID id;
  @JsonProperty("name")
  @JsonView(BeanPropertyView.Full.class)
  protected String name;
  @JsonProperty("status")
  @JsonView(BeanPropertyView.Full.class)
  protected BackendStatus status;
  
  public static enum BackendType {
    LOCAL,
    ACTIVE_MQ,
    RABBIT_MQ,
    SLURM
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public abstract BackendType getType();

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Backend other = (Backend) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
  
  @JsonView(BeanPropertyView.Full.class)
  @JsonIgnore
  public String getConfiguration() {
    return BeanSerializer.serializePartial(this);
  }

  public BackendStatus getStatus() {
    return status;
  }

  public void setStatus(BackendStatus status) {
    this.status = status;
  }
}