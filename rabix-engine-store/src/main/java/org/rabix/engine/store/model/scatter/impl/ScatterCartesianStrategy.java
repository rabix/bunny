package org.rabix.engine.store.model.scatter.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.ScatterMethod;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.store.model.scatter.PortMapping;
import org.rabix.engine.store.model.scatter.RowMapping;
import org.rabix.engine.store.model.scatter.ScatterStrategy;
import org.rabix.engine.store.model.scatter.ScatterStrategyException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ScatterCartesianStrategy implements ScatterStrategy {

  @JsonProperty("combinations")
  private LinkedList<Combination> combinations;

  @JsonProperty("values")
  private Map<String, LinkedList<Object>> values;
  @JsonProperty("positions")
  private Map<String, LinkedList<Integer>> positions;
  @JsonProperty("sizePerPort")
  private Map<String, Integer> sizePerPort;

  @JsonProperty("scatterMethod")
  private ScatterMethod scatterMethod;
  
  @JsonProperty("emptyListDetected")
  private Boolean emptyListDetected;
  
  @JsonProperty("skipScatter")
  private Boolean skipScatter;
  
  @JsonCreator
  public ScatterCartesianStrategy(@JsonProperty("combinations") LinkedList<Combination> combinations,
      @JsonProperty("values") Map<String, LinkedList<Object>> values,
      @JsonProperty("positions") Map<String, LinkedList<Integer>> positions,
      @JsonProperty("scatterMethod") ScatterMethod scatterMethod,
      @JsonProperty("sizePerPort") Map<String, Integer> sizePerPort,
      @JsonProperty("emptyListDetected") Boolean emptyListDetected,
      @JsonProperty("skipScatter") Boolean skipScatter) {
    super();
    this.combinations = combinations;
    this.values = values;
    this.positions = positions;
    this.sizePerPort = sizePerPort;
    this.scatterMethod = scatterMethod;
    this.emptyListDetected = emptyListDetected;
    this.skipScatter = skipScatter;
  }

  public ScatterCartesianStrategy(DAGNode dagNode) {
    this.values = new HashMap<>();
    this.positions = new HashMap<>();
    this.combinations = new LinkedList<>();
    this.scatterMethod = dagNode.getScatterMethod();
    this.sizePerPort = new HashMap<>();
    this.emptyListDetected = false;
    this.skipScatter = false;
    initialize(dagNode);
  }

  public ScatterMethod getScatterMethod() {
    return scatterMethod;
  }
  
  public void initialize(DAGNode dagNode) {
    for (DAGLinkPort port : dagNode.getInputPorts()) {
      if (port.isScatter()) {
        values.put(port.getId(), new LinkedList<Object>());
        positions.put(port.getId(), new LinkedList<Integer>());
      }
    }
  }

  @Override
  public synchronized void enable(String port, Object value, Integer position, Integer sizePerPort) throws ScatterStrategyException {
    LinkedList<Integer> positionList = positions.get(port);
    positionList = expand(positionList, position);
    positionList.set(position - 1, position);
    positions.put(port, positionList);

    LinkedList<Object> valueList = values.get(port);
    valueList = expand(valueList, position);
    valueList.set(position - 1, value);
    values.put(port, valueList);
    this.sizePerPort.put(port, sizePerPort);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Object> valueStructure(String jobId, String portId, UUID rootId) {
    if (emptyListDetected) {
      return (List<Object>) generateOutputsForEmptyList();
    }
    
    Collections.sort(combinations, new Comparator<Combination>() {
      @Override
      public int compare(Combination o1, Combination o2) {
        return o1.indexes.toString().compareTo(o2.indexes.toString());
      }
    });

    if (scatterMethod.equals(ScatterMethod.flat_crossproduct)) {
      LinkedList<Object> result = new LinkedList<>();
      for (Combination combination : combinations) {
        String scatteredJobId = InternalSchemaHelper.scatterId(jobId, combination.position);
        result.addLast(new JobPortPair(scatteredJobId, portId));
      }
      return result;
    }
    if (scatterMethod.equals(ScatterMethod.nested_crossproduct)) {
      LinkedList<Object> result = new LinkedList<>();

      int position = 1;
      LinkedList<Object> subresult = new LinkedList<>();
      for (Combination combination : combinations) {
        if (combination.indexes.get(0) != position) {
          result.addLast(subresult);
          subresult = new LinkedList<>();
          position++;
        }
        String scatteredJobId = InternalSchemaHelper.scatterId(jobId, combination.position);
        subresult.addLast(new JobPortPair(scatteredJobId, portId));
      }
      result.addLast(subresult);
      return result;
    }
    return null;
  }

  private <T> LinkedList<T> expand(LinkedList<T> list, Integer position) {
    if (list == null) {
      list = new LinkedList<>();
    }
    int initialSize = list.size();
    if (initialSize >= position) {
      return list;
    }
    for (int i = 0; i < position - initialSize; i++) {
      list.add(null);
    }
    return list;
  }

  private LinkedList<LinkedList<Integer>> cartesianProduct(LinkedList<LinkedList<Integer>> lists) throws BindingException {
    if (lists.size() < 2) {
      throw new BindingException("Can't have a product of fewer than two lists (got " + lists.size() + ")");
    }
    return cartesianProduct(0, lists);
  }

  private LinkedList<LinkedList<Integer>> cartesianProduct(int index, List<LinkedList<Integer>> lists) {
    LinkedList<LinkedList<Integer>> result = new LinkedList<LinkedList<Integer>>();
    if (index == lists.size()) {
      result.add(new LinkedList<Integer>());
    } else {
      for (Integer obj : lists.get(index)) {
        for (LinkedList<Integer> list : cartesianProduct(index + 1, lists)) {
          list.addFirst(obj);
          result.add(list);
        }
      }
    }
    return result;
  }

  @Override
  public synchronized void commit(List<RowMapping> mappings) {
    for (RowMapping mapping : mappings) {
      for (Combination combination : combinations) {
        if (combination.position == mapping.getIndex()) {
          combination.enabled = true;
        }
      }
    }
  }

  @Override
  public synchronized int enabledCount() {
    int size = 1;
    for (Entry<String, Integer> sizePerPort : this.sizePerPort.entrySet()) {
      size = size * sizePerPort.getValue();
    }
    return size;
  }

  @Override
  public synchronized List<RowMapping> enabled() throws ScatterStrategyException {
    List<RowMapping> result = new LinkedList<>();
    LinkedList<LinkedList<Integer>> mapping = new LinkedList<>();
    for (Entry<String, LinkedList<Integer>> positionEntry : positions.entrySet()) {
      mapping.add(positionEntry.getValue());
    }
    LinkedList<LinkedList<Integer>> newMapping;
    try {
      newMapping = cartesianProduct(mapping);
      
      for (int i = 0; i < newMapping.size(); i++) {
        LinkedList<Integer> indexes = newMapping.get(i);
        if (!hasNull(indexes)) {
          Combination combination = getCombination(indexes);
          if (combination == null) {
            combination = new Combination(combinations.size() + 1, false, indexes);
            combinations.add(combination);
          }
          if (!combination.enabled) {
            List<PortMapping> portMappings = new LinkedList<>();

            int positionIndex = 1;
            for (Entry<String, LinkedList<Object>> valueEntry : values.entrySet()) {
              String port = valueEntry.getKey();
              int position = combination.indexes.get(positionIndex - 1);
              Object value = valueEntry.getValue().get(position - 1);
              portMappings.add(new PortMapping(port, value));
              positionIndex++;
            }
            result.add(new RowMapping(combination.position, portMappings));
          }
        }
      }
      return result;
    } catch (BindingException e) {
      throw new ScatterStrategyException(e);
    }
  }

  private boolean hasNull(LinkedList<Integer> list) {
    for (Integer value : list) {
      if (value == null) {
        return true;
      }
    }
    return false;
  }

  private Combination getCombination(LinkedList<Integer> indexes) {
    for (Combination combination : combinations) {
      if (combination.indexes.toString().equals(indexes.toString())) {
        return combination;
      }
    }
    return null;
  }

  public static class Combination {
    @JsonProperty("position")
    int position;
    @JsonProperty("enabled")
    boolean enabled;
    @JsonProperty("indexes")
    List<Integer> indexes;

    @JsonCreator
    public Combination(@JsonProperty("position") int position, @JsonProperty("enabled") boolean enabled,
        @JsonProperty("indexes") List<Integer> indexes) {
      this.position = position;
      this.enabled = enabled;
      this.indexes = indexes;
    }

    @Override
    public String toString() {
      return "Combination [position=" + position + ", enabled=" + enabled + ", indexes=" + indexes + "]";
    }
    
  }

  @Override
  public boolean isBlocking() {
    return ScatterMethod.isBlocking(scatterMethod);
  }

  @Override
  public boolean isHanging() {
    for (String port : values.keySet()) {
      if (values.get(port) == null || (values.get(port) instanceof List<?> && ((List<?>)values.get(port)).isEmpty())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Object generateOutputsForEmptyList() {
    if (scatterMethod.equals(ScatterMethod.flat_crossproduct)) {
      return new ArrayList<>();  
    }
    
    Integer numberOfEmptyLists = values.values().stream().map(l -> l.size() != 0? l.size() : 1).reduce((x,y) -> x*y).get();
    List<List<?>> result = new ArrayList<>();
    for (int i = 0; i < numberOfEmptyLists; i++) {
      result.add(new ArrayList<>());
    }
    return result;
  }

  @Override
  public void setEmptyListDetected() {
    this.emptyListDetected = true;
  }

  @Override
  public boolean isEmptyListDetected() {
    return emptyListDetected;
  }

  @Override
  public void skipScatter(boolean skipScatter) {
    this.skipScatter = skipScatter;
  }

  @Override
  public boolean skipScatter() {
    return skipScatter;
  }

}
