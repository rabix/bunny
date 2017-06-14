package org.rabix.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class VMQueues {

  private static Map<String, BlockingQueue<Object>> queues = new HashMap<>();
  
  @SuppressWarnings("unchecked")
  public synchronized static <T> BlockingQueue<T> getQueue(String name) {
    BlockingQueue<Object> queue = queues.get(name);
    if (queue == null) {
      queue = new LinkedBlockingQueue<>();
      queues.put(name, queue);
    }
    return (BlockingQueue<T>) queue;
  }
  
}
