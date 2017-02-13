package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.SchemaHelper;
import org.rabix.engine.cache.Cachable;
import org.rabix.engine.cache.Cache;
import org.rabix.engine.cache.CacheItem.Action;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.model.VariableRecord.VariableRecordCacheKey;
import org.rabix.engine.repository.VariableRecordRepository;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class VariableRecordService {

  private CacheService cacheService;
  private VariableRecordRepository variableRecordRepository;

  @Inject
  public VariableRecordService(VariableRecordRepository variableRecordRepository, CacheService cacheService) {
    this.cacheService = cacheService;
    this.variableRecordRepository = variableRecordRepository;
  }
  
  public void create(VariableRecord variableRecord) {
    Cache cache = cacheService.getCache(variableRecord.getRootId(), VariableRecord.CACHE_NAME);
    cache.put(variableRecord, Action.INSERT);
  }
  
  public void delete(String rootId) {
  }

  public void update(VariableRecord variableRecord) {
    Cache cache = cacheService.getCache(variableRecord.getRootId(), VariableRecord.CACHE_NAME);
    cache.put(variableRecord, Action.UPDATE);
  }
  
  public List<VariableRecord> find(String jobId, LinkPortType type, String contextId) {
    Cache cache = cacheService.getCache(contextId, VariableRecord.CACHE_NAME);
    List<Cachable> records = cache.get(new VariableRecordCacheKey(jobId, null, contextId, type));
    if (!records.isEmpty()) {
      return Lists.transform(records, new Function<Cachable, VariableRecord>() {
        @Override
        public VariableRecord apply(Cachable input) {
          return (VariableRecord) input;
        }
      });
    }
    List<VariableRecord> fromDB = variableRecordRepository.getByType(jobId, type, SchemaHelper.toUUID(contextId));
    for (VariableRecord variableRecord : fromDB) {
      cache.put(variableRecord, Action.UPDATE);
    }
    return fromDB;
  }
  
  public List<VariableRecord> find(String jobId, String portId, String contextId) {
    Cache cache = cacheService.getCache(contextId, VariableRecord.CACHE_NAME);
    List<Cachable> records = cache.get(new VariableRecordCacheKey(jobId, portId, contextId, null));
    if (!records.isEmpty()) {
      return Lists.transform(records, new Function<Cachable, VariableRecord>() {
        @Override
        public VariableRecord apply(Cachable input) {
          return (VariableRecord) input;
        }
      });
    }
    List<VariableRecord> fromDB = variableRecordRepository.getByPort(jobId, portId, SchemaHelper.toUUID(contextId));
    for (VariableRecord variableRecord : fromDB) {
      cache.put(variableRecord, Action.UPDATE);
    }
    return fromDB;
  }

  public VariableRecord find(String jobId, String portId, LinkPortType type, String contextId) {
    Cache cache = cacheService.getCache(contextId, VariableRecord.CACHE_NAME);
    List<Cachable> records = cache.get(new VariableRecordCacheKey(jobId, portId, contextId, type));
    if (!records.isEmpty()) {
      return (VariableRecord) records.get(0);
    }
    VariableRecord record = variableRecordRepository.get(jobId, portId, type, SchemaHelper.toUUID(contextId));
    if (record != null) { // TODO why?
      cache.put(record, Action.UPDATE);
    }
    return record;
  }

  @SuppressWarnings("unchecked")
  public void addValue(VariableRecord variableRecord, Object value, Integer position, boolean wrap) {
    variableRecord.setNumberOfTimesUpdated(variableRecord.getNumberOfTimesUpdated());

    if (variableRecord.isDefault()) {
      variableRecord.setValue(null);
      variableRecord.setDefault(false);
    }
    if (variableRecord.getValue() == null) {
      if (position == 1) {
        if (wrap) {
          variableRecord.setWrapped(true);
          variableRecord.setValue(new ArrayList<>());
          ((ArrayList<Object>) variableRecord.getValue()).add(value);
        } else {
          variableRecord.setValue(value);
        }
      } else {
        List<Object> valueList = new ArrayList<>();
        expand(valueList, position);
        valueList.set(position - 1, value);
        variableRecord.setValue(valueList);
        variableRecord.setWrapped(true);
      }
    } else {
      if (variableRecord.isWrapped()) {
        expand((List<Object>) variableRecord.getValue(), position);
        ((List<Object>) variableRecord.getValue()).set(position - 1, value);
      } else {
        List<Object> valueList = new ArrayList<>();
        valueList.add(variableRecord.getValue());
        expand(valueList, position);
        valueList.set(position - 1, value);
        variableRecord.setValue(valueList);
        variableRecord.setWrapped(true);
      }
    }
  }
  
  public Object linkMerge(VariableRecord variableRecord) {
    switch (variableRecord.getLinkMerge()) {
    case merge_nested:
      return variableRecord.getValue();
    case merge_flattened:
      return mergeFlatten(variableRecord.getValue());
    default:
      return variableRecord.getValue();
    }
  }

  private <T> void expand(List<T> list, Integer position) {
    int initialSize = list.size();
    if (initialSize >= position) {
      return;
    }
    for (int i = 0; i < position - initialSize; i++) {
      list.add(null);
    }
    return;
  }

  @SuppressWarnings("unchecked")
  private Object mergeFlatten(Object value) {
    if (value == null) {
      return null;
    }
    if (!(value instanceof List<?>)) {
      return value;
    }
    List<Object> flattenedValues = new ArrayList<>();
    if (value instanceof List<?>) {
      for (Object subvalue : ((List<?>) value)) {
        Object flattenedSubvalue = mergeFlatten(subvalue);
        if (flattenedSubvalue instanceof List<?>) {
          flattenedValues.addAll((Collection<? extends Object>) flattenedSubvalue);
        } else {
          flattenedValues.add(flattenedSubvalue);
        }
      }
    } else {
      flattenedValues.add(value);
    }
    return flattenedValues;
  }
  
  public Object getValue(VariableRecord variableRecord) {
    if (variableRecord.getLinkMerge() == null) {
      return variableRecord.getValue();
    }
    return linkMerge(variableRecord);
  }
  
}
