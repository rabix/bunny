package org.rabix.bindings.cwl.bean.resource.requirement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.resource.CWLResource;
import org.rabix.bindings.cwl.bean.resource.CWLResourceType;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.expression.CWLExpressionResolver;
import org.rabix.bindings.cwl.helper.CWLDirectoryValueHelper;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CWLInitialWorkDirRequirement extends CWLResource {

  public final static String KEY_LISTING = "listing";
  public static final String KEY_DIRENT_ENTRY = "entry";
  public static final String KEY_DIRENT_WRITABLE = "writable";
  public static final String KEY_DIRENT_ENTRYNAME = "entryname";
  
  @JsonIgnore
  @SuppressWarnings("unchecked")
  public List<Object> getListing(CWLJob job) throws CWLExpressionException {
    Object listingObj = getValue(KEY_LISTING);
    if (listingObj == null ) {
      return Collections.emptyList();
    }
    
    List<Object> files = new ArrayList<>();
    if (listingObj instanceof String || CWLExpressionResolver.isExpressionObject(listingObj)) {
      files = CWLExpressionResolver.resolve(listingObj, job, null); // TODO validate
    }
    
    if (listingObj instanceof List<?>) {
      // iterate and cast
      List<Object> listingArray = (List<Object>) listingObj;
      
      for (Object listingArrayObj : listingArray) {
        if (listingArrayObj instanceof String || CWLExpressionResolver.isExpressionObject(listingArrayObj)) {
          files.add(CWLExpressionResolver.resolve(listingArrayObj, job, null)); // TODO validate
          continue;
        }
        if (CWLSchemaHelper.isFileFromValue(listingArrayObj)) {
          files.add(CWLFileValueHelper.createFileValue(listingArrayObj));
          continue;
        }
        if (CWLSchemaHelper.isDirectoryFromValue(listingArrayObj)) {
          files.add(CWLDirectoryValueHelper.createDirectoryValue(listingArrayObj));
          continue;
        }
        if (isDirent(listingArrayObj)) {
          files.add(createDirent(listingArrayObj, job));
          continue;
        }
      }
    }
    return files;
  }
  
  @SuppressWarnings("unchecked")
  public static boolean isDirent(Object value) {
    if (value instanceof Map<?,?>) {
      Map<String, Object> valueMap = (Map<String, Object>) value;
      return valueMap.containsKey(KEY_DIRENT_ENTRY); 
    }
    return false;
  }
  
  @SuppressWarnings("unchecked")
  public static CWLDirent createDirent(Object value, CWLJob job) throws CWLExpressionException {
    if (isDirent(value)) {
      Map<String, Object> valueMap = (Map<String, Object>) value;
      Object entryObj = valueMap.get(KEY_DIRENT_ENTRY);
      Object entrynameObj = valueMap.get(KEY_DIRENT_ENTRYNAME);
      boolean writable = valueMap.containsKey(KEY_DIRENT_WRITABLE)? (Boolean) valueMap.get(KEY_DIRENT_WRITABLE) : false; // it's false by default
      
      String entryname = null;
      if (entrynameObj != null) {
        if (CWLExpressionResolver.isExpressionObject(entrynameObj) || entrynameObj instanceof String) {
          entryname = CWLExpressionResolver.resolve(entrynameObj, job, null);
        } else {
          entryname = (String) entrynameObj;
        }
      }
      
      Object entry = null;
      if (entryObj != null) {
        if (CWLExpressionResolver.isExpressionObject(entryObj) || entryObj instanceof String) {
          entry = CWLExpressionResolver.resolve(entryObj, job, null);
        } else {
          entry = (String) entrynameObj;
        }
      }
      
      return new CWLDirent(entry, entryname, writable);
    }
    return null;
  }
  
  public static class CWLDirent {
    private final Object entry;
    private final Object entryname;
    private final boolean writable;
    
    public CWLDirent(Object entry, Object entryname, boolean writable) {
      this.entry = entry;
      this.entryname = entryname;
      this.writable = writable;
    }

    public Object getEntry() {
      return entry;
    }

    public Object getEntryname() {
      return entryname;
    }

    public boolean isWritable() {
      return writable;
    }

    @Override
    public String toString() {
      return "Dirent [entry=" + entry + ", entryname=" + entryname + ", writable=" + writable + "]";
    }
    
  }
  
  @Override
  @JsonIgnore
  public CWLResourceType getTypeEnum() {
    return CWLResourceType.INITIAL_WORK_DIR_REQUIREMENT;
  }
  
}
