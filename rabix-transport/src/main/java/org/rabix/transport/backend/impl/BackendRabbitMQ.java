package org.rabix.transport.backend.impl;

import org.rabix.transport.backend.Backend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BackendRabbitMQ extends Backend {

  @JsonProperty("host")
  private String host;
  @JsonProperty("engine_configuration")
  private EngineConfiguration engineConfiguration;
  @JsonProperty("backend_configuration")
  private BackendConfiguration backendConfiguration;
  
  @JsonCreator
  public BackendRabbitMQ(@JsonProperty("id") String id, @JsonProperty("host") String host, @JsonProperty("engine_configuration") EngineConfiguration engineConfiguration, @JsonProperty("backend_configuration") BackendConfiguration backendConfiguration) {
    this.id = id;
    this.host = host;
    this.engineConfiguration = engineConfiguration;
    this.backendConfiguration = backendConfiguration;
  }
  
  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public EngineConfiguration getEngineConfiguration() {
    return engineConfiguration;
  }

  public void setEngineConfiguration(EngineConfiguration engineConfiguration) {
    this.engineConfiguration = engineConfiguration;
  }

  public BackendConfiguration getBackendConfiguration() {
    return backendConfiguration;
  }

  public void setBackendConfiguration(BackendConfiguration backendConfiguration) {
    this.backendConfiguration = backendConfiguration;
  }

  public static class EngineConfiguration {
    @JsonProperty("exchange")
    private String exchange;
    @JsonProperty("exchange_type")
    private String exchangeType;
    @JsonProperty("receive_routing_key")
    private String receiveRoutingKey;
    @JsonProperty("heartbeat_routing_key")
    private String heartbeatRoutingKey;
    
    @JsonCreator
    public EngineConfiguration(@JsonProperty("exchange") String exchange, @JsonProperty("exchange_type") String exchangeType, @JsonProperty("receive_routing_key") String receiveRoutingKey, @JsonProperty("heartbeat_routing_key") String heartbeatRoutingKey) {
      this.exchange = exchange;
      this.exchangeType = exchangeType;
      this.receiveRoutingKey = receiveRoutingKey;
      this.heartbeatRoutingKey = heartbeatRoutingKey;
    }

    public String getExchange() {
      return exchange;
    }

    public String getExchangeType() {
      return exchangeType;
    }

    public String getReceiveRoutingKey() {
      return receiveRoutingKey;
    }

    public String getHeartbeatRoutingKey() {
      return heartbeatRoutingKey;
    }
    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
      result = prime * result + ((exchangeType == null) ? 0 : exchangeType.hashCode());
      result = prime * result + ((heartbeatRoutingKey == null) ? 0 : heartbeatRoutingKey.hashCode());
      result = prime * result + ((receiveRoutingKey == null) ? 0 : receiveRoutingKey.hashCode());
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
      EngineConfiguration other = (EngineConfiguration) obj;
      if (exchange == null) {
        if (other.exchange != null)
          return false;
      } else if (!exchange.equals(other.exchange))
        return false;
      if (exchangeType == null) {
        if (other.exchangeType != null)
          return false;
      } else if (!exchangeType.equals(other.exchangeType))
        return false;
      if (heartbeatRoutingKey == null) {
        if (other.heartbeatRoutingKey != null)
          return false;
      } else if (!heartbeatRoutingKey.equals(other.heartbeatRoutingKey))
        return false;
      if (receiveRoutingKey == null) {
        if (other.receiveRoutingKey != null)
          return false;
      } else if (!receiveRoutingKey.equals(other.receiveRoutingKey))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "EngineConfiguration [exchange=" + exchange + ", exchangeType=" + exchangeType + ", receiveRoutingKey=" + receiveRoutingKey + ", heartbeatRoutingKey=" + heartbeatRoutingKey + "]";
    }
    
  }
  
  public static class BackendConfiguration {
    @JsonProperty("exchange")
    private String exchange;
    @JsonProperty("exchange_type")
    private String exchangeType;
    @JsonProperty("receive_routing_key")
    private String receiveRoutingKey;
    @JsonProperty("receive_control_routing_key")
    private String receiveControlRoutingKey;
    @JsonProperty("heartbeat_period_mills")
    private Long heartbeatPeriodMills;
    
    @JsonCreator
    public BackendConfiguration(@JsonProperty("exchange") String exchange,
        @JsonProperty("exchange_type") String exchangeType,
        @JsonProperty("receive_routing_key") String receiveRoutingKey,
        @JsonProperty("receive_control_routing_key") String receiveControlRoutingKey,
        @JsonProperty("heartbeat_period_mills") Long heartbeatPeriodMills) {
      this.exchange = exchange;
      this.exchangeType = exchangeType;
      this.receiveRoutingKey = receiveRoutingKey;
      this.receiveControlRoutingKey = receiveControlRoutingKey;
      this.heartbeatPeriodMills = heartbeatPeriodMills;
    }

    public String getExchange() {
      return exchange;
    }

    public String getExchangeType() {
      return exchangeType;
    }

    public String getReceiveRoutingKey() {
      return receiveRoutingKey;
    }

    public String getReceiveControlRoutingKey() {
      return receiveControlRoutingKey;
    }
    
    public Long getHeartbeatPeriodMills() {
      return heartbeatPeriodMills;
    }
    
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
      result = prime * result + ((exchangeType == null) ? 0 : exchangeType.hashCode());
      result = prime * result + ((receiveRoutingKey == null) ? 0 : receiveRoutingKey.hashCode());
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
      BackendConfiguration other = (BackendConfiguration) obj;
      if (exchange == null) {
        if (other.exchange != null)
          return false;
      } else if (!exchange.equals(other.exchange))
        return false;
      if (exchangeType == null) {
        if (other.exchangeType != null)
          return false;
      } else if (!exchangeType.equals(other.exchangeType))
        return false;
      if (receiveRoutingKey == null) {
        if (other.receiveRoutingKey != null)
          return false;
      } else if (!receiveRoutingKey.equals(other.receiveRoutingKey))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "BackendConfiguration [exchange=" + exchange + ", exchangeType=" + exchangeType + ", receiveRoutingKey=" + receiveRoutingKey + ", receiveControlRoutingKey=" + receiveControlRoutingKey + ", heartbeatPeriodMills=" + heartbeatPeriodMills + "]";
    }

  }
  
  @Override
  @JsonIgnore
  public BackendType getType() {
    return BackendType.RABBIT_MQ;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((backendConfiguration == null) ? 0 : backendConfiguration.hashCode());
    result = prime * result + ((engineConfiguration == null) ? 0 : engineConfiguration.hashCode());
    result = prime * result + ((host == null) ? 0 : host.hashCode());
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
    BackendRabbitMQ other = (BackendRabbitMQ) obj;
    if (backendConfiguration == null) {
      if (other.backendConfiguration != null)
        return false;
    } else if (!backendConfiguration.equals(other.backendConfiguration))
      return false;
    if (engineConfiguration == null) {
      if (other.engineConfiguration != null)
        return false;
    } else if (!engineConfiguration.equals(other.engineConfiguration))
      return false;
    if (host == null) {
      if (other.host != null)
        return false;
    } else if (!host.equals(other.host))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "BackendRabbitMQ [host=" + host + ", engineConfiguration=" + engineConfiguration + ", backendConfiguration=" + backendConfiguration + ", id=" + id + "]";
  }

}