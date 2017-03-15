package org.rabix.transport.mechanism.impl.rabbitmq;

import org.rabix.transport.mechanism.TransportQueue;

public class TransportQueueRabbitMQ implements TransportQueue {

  private final String exchange;
  private final String exchangeType;
  private final String routingKey;
  
  public TransportQueueRabbitMQ(String exchange, String exchangeType, String routingKey) {
    this.exchange = exchange;
    this.exchangeType = exchangeType;
    this.routingKey = routingKey;
  }
  
  public String getExchange() {
    return exchange;
  }
  
  public String getExchangeType() {
    return exchangeType;
  }
  
  public String getRoutingKey() {
    return routingKey;
  }

  public String getQueueName() {
    return exchange + "_" + routingKey;
  }

  @Override
  public String toString() {
    return "TransportQueueRabbitMQ [exchange=" + exchange + ", exchangeType=" + exchangeType + ", routingKey=" + routingKey + "]";
  }
  
}
