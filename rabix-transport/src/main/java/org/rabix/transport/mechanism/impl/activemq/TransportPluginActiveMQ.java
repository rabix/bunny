package org.rabix.transport.mechanism.impl.activemq;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.commons.configuration.Configuration;
import org.rabix.common.json.BeanSerializer;
import org.rabix.common.json.processor.BeanProcessorException;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.TransportPluginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransportPluginActiveMQ implements TransportPlugin<TransportQueueActiveMQ> {

  private static final Logger logger = LoggerFactory.getLogger(TransportConfigActiveMQ.class);
  
  private PooledConnectionFactory connectionFactory;
  
  private ConcurrentMap<TransportQueueActiveMQ, Receiver<?>> receivers = new ConcurrentHashMap<>();
  
  private ExecutorService receiverThreadPool = Executors.newCachedThreadPool();

  public TransportPluginActiveMQ(Configuration configuration) throws TransportPluginException {
    connectionFactory = new PooledConnectionFactory(TransportConfigActiveMQ.getBroker(configuration));
    connectionFactory.setIdleTimeout(5000);
    connectionFactory.setMaxConnections(10);
    connectionFactory.setBlockIfSessionPoolIsFull(false);
    connectionFactory.setMaximumActiveSessionPerConnection(5000);
    connectionFactory.start();
  }
  
  @Override
  public <T> ResultPair<T> send(TransportQueueActiveMQ queue, T entity) {
    Session session = null;
    Connection connection = null;
    try {
      connection = connectionFactory.createConnection();
      connection.start();

      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Destination destination = session.createQueue(queue.getQueue());

      MessageProducer producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

      String payload = BeanSerializer.serializeFull(entity);
      TextMessage message = session.createTextMessage(payload);
      producer.send(message);
      return ResultPair.<T> success();
    } catch (JMSException e) {
      logger.error("Failed to send a message to " + queue, e);
      return ResultPair.<T> fail("Failed to send a message to " + queue, e);
    } finally {
      try {
        session.close();
        connection.close();
      } catch (JMSException ignore) {
      }
    }
  }

  @Override
  public TransportPluginType getType() {
    return TransportPluginType.ACTIVE_MQ;
  }

  @Override
  public <T> void startReceiver(TransportQueueActiveMQ sourceQueue, Class<T> clazz, ReceiveCallback<T> receiveCallback, ErrorCallback errorCallback) {
    final Receiver<T> receiver = new Receiver<>(clazz, receiveCallback, errorCallback, sourceQueue);
    receivers.put(sourceQueue, receiver);
    receiverThreadPool.submit(new Runnable() {
      @Override
      public void run() {
        receiver.start();
      }
    });    
  }

  @Override
  public void stopReceiver(TransportQueueActiveMQ queue) {
    Receiver<?> receiver = receivers.get(queue);
    if (receiver != null) {
      receiver.stop();
      receivers.remove(queue);
    }
  }

  private class Receiver<T> {

    private Class<T> clazz;
    private ReceiveCallback<T> callback;
    private ErrorCallback errorCallback;

    private TransportQueueActiveMQ queue;

    private volatile boolean isStopped = false;

    public Receiver(Class<T> clazz, ReceiveCallback<T> callback, ErrorCallback errorCallback, TransportQueueActiveMQ queue) {
      this.clazz = clazz;
      this.callback = callback;
      this.queue = queue;
    }

    void start() {
      Session session = null;
      Connection connection = null;
      MessageConsumer consumer = null;

      try {
        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(queue.getQueue());
        consumer = session.createConsumer(destination);

        while (!isStopped) {
          Message message = consumer.receive();
          TextMessage textMessage = (TextMessage) message;
          String text = textMessage.getText();
          callback.handleReceive(BeanSerializer.deserialize(text, clazz));
        }
      } catch (JMSException e) {
        logger.error("Failed to receive a message from " + queue, e);
        errorCallback.handleError(e);
      } catch (BeanProcessorException e) {
        logger.error("Failed to deserialize message payload", e);
        errorCallback.handleError(e);
      } catch (TransportPluginException e) {
        logger.error("Failed to handle receive", e);
        errorCallback.handleError(e);
      } finally {
        try {
          consumer.close();
          session.close();
          connection.close();
        } catch (JMSException ignore) {
        }
      }
    }

    void stop() {
      isStopped = true;
    }

  }
}
