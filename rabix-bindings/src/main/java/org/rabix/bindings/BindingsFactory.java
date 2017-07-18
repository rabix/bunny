package org.rabix.bindings;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.NotImplementedException;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingsFactory {

  private final static Logger logger = LoggerFactory.getLogger(BindingsFactory.class);
  
  public static final String MULTIPROTOCOL_KEY = "rabix.multiprotocol";
  
  private static SortedSet<Bindings> bindings = new TreeSet<>(new Comparator<Bindings>() {
    @Override
    public int compare(Bindings b1, Bindings b2) {
      return b1.getProtocolType().order - b2.getProtocolType().order;
    }
  });

  static {
    for (ProtocolType type : ProtocolType.values()) {
      try {
        Class<?> clazz = Class.forName(type.bindingsClass);
        if (clazz == null) {
          continue;
        }
        try {
          bindings.add((Bindings) clazz.newInstance());
        } catch (Exception e) {
          logger.warn("Failed to instantiate class " + clazz, e);
        }
      } catch (Exception e) {
        // ignore
      }
    }
  }

  public static synchronized Bindings create(String appURL) throws BindingException {
    int wrongVersions = 0;
    Exception finalException = null;
    for (Bindings binding : bindings) {
      try {
        Application app = binding.loadAppObject(appURL);
        if (app == null) {
          continue;
        }
        if (app.getVersion() != null && binding.getProtocolType().appVersion.equalsIgnoreCase(app.getVersion())) {
          return binding;
        }
        else if(app.getVersion() == null && binding.getProtocolType().appVersion == null) {
          return binding;
        }
      } catch (NotImplementedException e) {
        throw e; // fail if we do not support this kind of deserialization (Schema salad)
      } catch (BindingWrongVersionException ignore) {
        wrongVersions++;
      } catch (Exception e) {
        finalException = e;
      }
    }

    if (wrongVersions == bindings.size()) {
      StringBuilder validVersions = new StringBuilder();
      boolean first = true;
      for (Bindings b : bindings) {
        if (b.getProtocolType().appVersion == null)
          continue;
        if (!first)
          validVersions.append(", ");
        validVersions.append(b.getProtocolType().appVersion);
        first = false;
      }
      throw new BindingException("Invalid cwlVersion. Allowed values are: " + validVersions);
    }

    if (finalException != null && finalException.getMessage() != null) {
      throw new BindingException(finalException.getMessage(), finalException);
    }

    throw new BindingException("Unknown error when parsing the app!");
  }

  public static synchronized Bindings create(Job job) throws BindingException {
    return create(job.getApp());
  }
  
  public static synchronized Bindings create(ProtocolType protocol) throws BindingException {
    for(Bindings binding: bindings) {
      if(binding.getProtocolType().equals(protocol)) {
        return binding;
      }
    }
    throw new BindingException("Cannot find binding for the protocol.");
  }

}
