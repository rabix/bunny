package org.rabix.transport.mechanism.impl.rabbitmq;

import org.apache.commons.configuration.Configuration;

public class TransportConfigRabbitMQ {

  public static boolean isDev(Configuration configuration) {
    return configuration.getBoolean("rabbitmq.dev", true);
  }

  public static String getHost(Configuration configuration) {
    return configuration.getString("rabbitmq.host");
  }

  public static int getPort(Configuration configuration) {
    return configuration.getInt("rabbitmq.port");
  }

  public static boolean isSSL(Configuration configuration) {
    return configuration.getBoolean("rabbitmq.ssl");
  }

  public static String getUsername(Configuration configuration) {
    return configuration.getString("rabbitmq.username");
  }

  public static String getPassword(Configuration configuration) {
    return configuration.getString("rabbitmq.password");
  }

  public static String getVirtualhost(Configuration configuration) {
    return configuration.getString("rabbitmq.virtualhost");
  }

  public static String getEngineExchange(Configuration configuration) {
    return configuration.getString("rabbitmq.engine.exchange");
  }

  public static String getEngineExchangeType(Configuration configuration) {
    return configuration.getString("rabbitmq.engine.exchangeType");
  }

  public static String getEngineHeartbeatRoutingKey(Configuration configuration) {
    return configuration.getString("rabbitmq.engine.heartbeatRoutingKey");
  }

  public static String getEngineReceiveRoutingKey(Configuration configuration) {
    return configuration.getString("rabbitmq.engine.receiveRoutingKey");
  }

  public static String getBackendExchange(Configuration configuration) {
    return configuration.getString("rabbitmq.backend.exchange");
  }

  public static String getBackendExchangeType(Configuration configuration) {
    return configuration.getString("rabbitmq.backend.exchangeType");
  }

  public static String getBackendReceiveRoutingKey(Configuration configuration) {
    return configuration.getString("rabbitmq.backend.receiveRoutingKey");
  }

  public static String getBackendReceiveControlRoutingKey(Configuration configuration) {
    return configuration.getString("rabbitmq.backend.receiveControlRoutingKey");
  }

  public static Long getBackendHeartbeatTimeMills(Configuration configuration) {
    return configuration.getLong("rabbitmq.backend.heartbeatTimeMills");
  }

  public static boolean durableQueues(Configuration configuration) {
    return configuration.getBoolean("rabbitmq.backend.durableQueues", false);
  }

  public static int consumersThreadPoolSize(Configuration configuration) {
    return configuration.getInt("rabbitmq.consumers.thread.pool.size", Runtime.getRuntime().availableProcessors() * 2);
  }
}
