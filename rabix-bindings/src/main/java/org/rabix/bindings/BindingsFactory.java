package org.rabix.bindings;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.rabix.bindings.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingsFactory {

  private final static Logger logger = LoggerFactory.getLogger(BindingsFactory.class);
  public static ProtocolType protocol = null;
  
  private static SortedSet<Bindings> bindings = new TreeSet<>(new Comparator<Bindings>() {
    @Override
    public int compare(Bindings b1, Bindings b2) {
      return b1.getProtocolType().order - b2.getProtocolType().order;
    }
  });

  static {
    try {
      for (ProtocolType type : ProtocolType.values()) {
        Class<?> clazz = Class.forName(type.bindingsClass);
        if (clazz == null) {
          continue;
        }
        bindings.add((Bindings) clazz.newInstance());
      }
    } catch (Exception e) {
      logger.error("Failed to initialize bindings", e);
      throw new RuntimeException("Failed to initialize bindings", e);
    }
  }

  public static void setProtocol(String prot) {
    switch (prot) {
    case "draft-2":
      protocol = ProtocolType.DRAFT2;
      break;
    case "draft-3":
      protocol = ProtocolType.DRAFT3;
      break;
    }
  }
 
  public static Bindings create(String appURL) throws BindingException {
    if(protocol != null) {
      for (Bindings binding : bindings) {
        if(binding.getProtocolType() == protocol) {
          return binding;
        }
      }
    }
    else {
      for (Bindings binding : bindings) {
        try {
          Object app = binding.loadAppObject(appURL);
          if (app == null) {
            continue;
          }
          return binding;
        } catch (Exception ignore) {
        }
      }
    }
    throw new BindingException("Cannot find binding for the payload.");
  }

  public static Bindings create(Job job) throws BindingException {
    return create(job.getApp());
  }

}
