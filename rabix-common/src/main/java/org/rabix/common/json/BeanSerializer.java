package org.rabix.common.json;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.processor.BeanProcessor;
import org.rabix.common.json.processor.BeanProcessorException;
import org.rabix.common.json.processor.BeanProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Preconditions;

public class BeanSerializer {

  private static Logger logger = LoggerFactory.getLogger(BeanSerializer.class);

  /**
   * Load bean and process it if there is a processor
   */
  public static <T> T deserialize(String data, Class<T> clazz) throws BeanProcessorException {
    Preconditions.checkNotNull(data);

    T bean = JSONHelper.readObject(data, clazz);

    BeanProcessor<T> processor = BeanProcessorFactory.create(clazz);
    if (processor != null) {
      return processor.process(bean);
    }
    return bean;
  }

  /**
   * Save bean and use {@link BeanPropertyView.Full} for filtering
   */
  public static String serializeFull(Object data) {
    return serialize(data, BeanPropertyView.Full.class);
  }

  /**
   * Save bean and use {@link BeanPropertyView.Partial} for filtering
   */
  public static String serializePartial(Object data) {
    return serialize(data, BeanPropertyView.Partial.class);
  }

  /**
   * Save bean and use custom {@link BeanPropertyView} for filtering
   */
  public static String serialize(Object data, Class<? extends BeanPropertyView> clazz) {
    Preconditions.checkNotNull(data);

    try {
      if (clazz != null) {
        ObjectWriter writer = JSONHelper.mapper.writerWithView(clazz);
        return writer.withDefaultPrettyPrinter().writeValueAsString(data);
      }
      return JSONHelper.mapper.writeValueAsString(data);
    } catch (IOException e) {
      logger.error("Failed to serialize object " + data, e);
      throw new IllegalStateException(e);
    }
  }

  /**
   * Save bean to file and use {@link BeanPropertyView.Full} for filtering
   */
  public static void serializeFull(File file, Object data) {
    serializeFull(file.toPath(), data);
  }
  
  public static void serializeFull(Path file, Object data) {
    serialize(file, data, BeanPropertyView.Full.class);
  }

  /**
   * Save bean to file and use {@link BeanPropertyView.Partial} for filtering
   */
  public static void serializePartial(File file, Object data) {
    serializePartial(file.toPath(), data);
  }
  
  public static void serializePartial(Path file, Object data) {
    serialize(file, data, BeanPropertyView.Partial.class);
  }

  /**
   * Save bean to file and use custom {@link BeanPropertyView} for filtering
   */
  public static void serialize(File file, Object data, Class<? extends BeanPropertyView> clazz) {
    serialize(file.toPath(), clazz);
  }
  
  public static void serialize(Path file, Object data, Class<? extends BeanPropertyView> clazz) {
    Preconditions.checkNotNull(data);

    try {
      if (clazz != null) {
        ObjectWriter writer = JSONHelper.mapper.writerWithView(clazz);
        writer.withDefaultPrettyPrinter().writeValue(Files.newOutputStream(file), data);
        return;
      }
      JSONHelper.mapper.writerWithDefaultPrettyPrinter().writeValue(Files.newOutputStream(file), data);
    } catch (IOException e) {
      logger.error("Failed to serialize object " + data + " to file " + file.toString(), e);
      throw new IllegalStateException(e);
    }
  }

}
