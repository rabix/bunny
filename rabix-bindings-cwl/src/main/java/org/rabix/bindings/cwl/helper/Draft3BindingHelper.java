package org.rabix.bindings.draft3.helper;

import java.util.Map;

import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.draft3.expression.Draft3ExpressionResolver;

public class Draft3BindingHelper extends Draft3BeanHelper {

  public static final String DEFAULT_SEPARATOR = "\u0020";
  public static final boolean IS_SEPARATED_BY_DEFAULT = true;
  public static final boolean IS_SHELL_QUOTE_BY_DEFAULT = true;
  public static final boolean LOAD_CONTENTS_BY_DEFAULT = false;
  
  public static final String KEY_ID = "id";
  public static final String KEY_SOURCE = "source";
  public static final String KEY_DEFAULT = "default";
  public static final String KEY_PREFIX = "prefix";
  public static final String KEY_POSITION = "position";
  public static final String KEY_SHELL_QUOTE = "shellQuote";
  public static final String KEY_GLOB = "glob";
  public static final String KEY_SEPARATE = "separate";
  public static final String KEY_ITEM_SEPARATOR = "itemSeparator";
  public static final String KEY_LOAD_CONTENTS = "loadContents";
  public static final String KEY_SBG_METADATA = "sbg:metadata";
  // this is only to maintain temporary backward compatibility with old properties (without prefix)
  public static final String KEY_METADATA = "metadata";
  public static final String KEY_INHERIT_METADATA_FROM = "sbg:inheritMetadataFrom";
  public static final String KEY_SECONDARY_FILES = "secondaryFiles";
  public static final String KEY_VALUE_FROM = "valueFrom";
  public static final String KEY_OUTPUT_EVAL = "outputEval";
  public static final String KEY_LINK_MERGE = "linkMerge";

  public static Object getGlob(Object binding) {
    return getValue(KEY_GLOB, binding);
  }
  
  public static String getId(Object binding) {
    return getValue(KEY_ID, binding);
  }
  
  public static String getPrefix(Object binding) {
    return getValue(KEY_PREFIX, binding);
  }

  public static boolean isSeparated(Object binding) {
    return getValue(KEY_SEPARATE, binding, IS_SEPARATED_BY_DEFAULT);
  }
  
  public static boolean shellQuote(Object binding) {
    return getValue(KEY_SHELL_QUOTE, binding, IS_SHELL_QUOTE_BY_DEFAULT);
  }
  
  public static boolean loadContents(Object binding) {
    return getValue(KEY_LOAD_CONTENTS, binding, LOAD_CONTENTS_BY_DEFAULT);
  }
  
  public static String getSeparator(Object binding) {
    return isSeparated(binding)? DEFAULT_SEPARATOR : "";
  }

  public static String getItemSeparator(Object binding) {
    return getValue(KEY_ITEM_SEPARATOR, binding);
  }

  public static int getPosition(Object binding) {
    return getValue(KEY_POSITION, binding, 0);
  }
  
  public static Map<String, Object> getMetadata(Object binding) {
    Map<String, Object> metadata = getValue(KEY_SBG_METADATA, binding);
    if (metadata == null) {
      metadata = getValue(KEY_METADATA, binding);
    }
    return metadata;
  }
  
  public static String getInheritMetadataFrom(Object binding) {
    return getValue(KEY_INHERIT_METADATA_FROM, binding);
  }
  
  public static Object getSecondaryFiles(Object binding) {
    return getValue(KEY_SECONDARY_FILES, binding);
  }
  
  public static Object getValueFrom(Object binding) {
    return getValue(KEY_VALUE_FROM, binding);
  }
  
  public static Object getOutputEval(Object binding) {
    return getValue(KEY_OUTPUT_EVAL, binding);
  }
  
  public static Object getDefault(Map<String, Object> binding) {
    return getValue(KEY_DEFAULT, binding);
  }

  public static Object getSource(Map<String, Object> binding) {
    return getValue(KEY_SOURCE, binding);
  }
  
  public static String getLinkMerge(Map<String, Object> binding) {
    return getValue(KEY_LINK_MERGE, binding);
  }
  
  /**
   * Evaluate outputEval binding and return transformed value
   */
  public static Object evaluateOutputEval(Draft3Job job, Object output, Object binding) throws Draft3ExpressionException {
    Object outputEval = getOutputEval(binding);
    return Draft3ExpressionResolver.resolve(outputEval, job, output);
  }
  
}
