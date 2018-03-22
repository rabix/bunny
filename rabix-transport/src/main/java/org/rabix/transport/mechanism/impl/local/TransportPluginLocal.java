package org.rabix.transport.mechanism.impl.local;

import org.apache.commons.configuration.Configuration;
import org.rabix.common.VMQueues;
import org.rabix.common.json.BeanSerializer;
import org.rabix.common.json.processor.BeanProcessorException;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.TransportPluginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransportPluginLocal implements TransportPlugin<TransportQueueLocal> {

  private static final Logger logger = LoggerFactory.getLogger(TransportPluginLocal.class);

  private ConcurrentMap<TransportQueueLocal, Receiver<?>> receivers = new ConcurrentHashMap<>();

  private ExecutorService receiverThreadPool = Executors.newCachedThreadPool();

  public TransportPluginLocal(Configuration configuration) throws TransportPluginException {
  }

  @Override
  public <T> ResultPair<T> send(TransportQueueLocal queue, T entity) {
    try {
      VMQueues.getQueue(queue.getQueue()).put(entity);
      return ResultPair.<T> success();
    } catch (InterruptedException e) {
      logger.error("Failed to send a message to " + queue, e);
      return ResultPair.<T>fail("Failed to put to queue " + queue, e);
    }
  }

  @Override
  public TransportPluginType getType() {
    return TransportPluginType.LOCAL;
  }

  @Override
  public <T> void startReceiver(TransportQueueLocal sourceQueue, Class<T> clazz, ReceiveCallback<T> receiveCallback, ErrorCallback errorCallback) {
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
  public void stopReceiver(TransportQueueLocal queue) {
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

    private TransportQueueLocal queue;

    private volatile boolean isStopped = false;

    public Receiver(Class<T> clazz, ReceiveCallback<T> callback, ErrorCallback errorCallback, TransportQueueLocal queue) {
      this.clazz = clazz;
      this.callback = callback;
      this.queue = queue;
    }

    void start() {
      try {
        while (!isStopped) {
          T payload = VMQueues.<T> getQueue(queue.getQueue()).take();
          callback.handleReceive(payload, () -> {});
        }
      } catch (InterruptedException e) {
        logger.error("Failed to receive a message from " + queue, e);
        errorCallback.handleError(e);
      } catch (BeanProcessorException e) {
        logger.error("Failed to deserialize message payload", e);
        errorCallback.handleError(e);
      } catch (TransportPluginException e) {
        logger.error("Failed to handle receive", e);
        errorCallback.handleError(e);
      }
    }

    void stop() {
      isStopped = true;
    }

  }

}
