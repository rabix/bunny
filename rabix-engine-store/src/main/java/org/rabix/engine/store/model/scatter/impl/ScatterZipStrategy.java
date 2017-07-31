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

import org.rabix.bindings.model.ScatterMethod;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.store.model.scatter.RowMapping;
import org.rabix.engine.store.model.scatter.ScatterStrategyException;
import org.rabix.engine.store.model.scatter.PortMapping;
import org.rabix.engine.store.model.scatter.ScatterStrategy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

public class ScatterZipStrategy implements ScatterStrategy {

  @JsonProperty("combinations")
  private LinkedList<Combination> combinations;
  
  @JsonProperty("values")
  private Map<String, LinkedList<Object>> values;
  @JsonProperty("indexes")
  private Map<String, LinkedList<Boolean>> indexes;

  @JsonProperty("scatterMethod")
  private ScatterMethod scatterMethod;
  
  @JsonProperty("emptyListDetected")
  private Boolean emptyListDetected;
  
  @JsonProperty("skipScatter")
  private Boolean skipScatter;
  
  @JsonCreator
  public ScatterZipStrategy(@JsonProperty("combinations") LinkedList<Combination> combinations,
      @JsonProperty("values") Map<String, LinkedList<Object>> values,
      @JsonProperty("indexes") Map<String, LinkedList<Boolean>> indexes,
      @JsonProperty("scatterMethod") ScatterMethod scatterMethod,
      @JsonProperty("emptyListDetected") Boolean emptyListDetected, @JsonProperty("skipScatter") Boolean skipScatter) {
    super();
    this.combinations = combinations;
    this.values = values;
    this.indexes = indexes;
    this.scatterMethod = scatterMethod;
    this.emptyListDetected = emptyListDetected;
    this.skipScatter = skipScatter;
  }

  public ScatterZipStrategy(DAGNode dagNode) {
    values = new HashMap<>();
    indexes = new HashMap<>();
    combinations = new LinkedList<>();
    
    this.scatterMethod = dagNode.getScatterMethod();
    this.emptyListDetected = false;
    this.skipScatter = false;
    initialize(dagNode);
  }
  
  public void initialize(DAGNode dagNode) {
    for(DAGLinkPort port : dagNode.getInputPorts()) {
      if (port.isScatter()) {
        values.put(port.getId(), new LinkedList<Object>());
        indexes.put(port.getId(), new LinkedList<Boolean>());
      }
    }
  }
  
  public void enable(String port, Object value, Integer position, Integer sizePerPort) throws ScatterStrategyException {
    Preconditions.checkNotNull(port);
    Preconditions.checkNotNull(position);
    
    List<Object> valueList = values.get(port);
    List<Boolean> indexList = indexes.get(port);

    if (indexList.size() < position) {
      expand(indexList, position);
      expand(valueList, position);
    }

    indexList.set(position - 1, true);
    valueList.set(position - 1, value);
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

  @Override
  public List<RowMapping> enabled() {
    List<RowMapping> result = new LinkedList<>();

    List<String> ports = new LinkedList<>();
    LinkedList<LinkedList<Boolean>> indexLists = new LinkedList<>();

    for (Entry<String, LinkedList<Boolean>> entry : indexes.entrySet()) {
      ports.add(entry.getKey());
      indexLists.add(entry.getValue());
    }

    List<Boolean> first = indexLists.get(0);
    for (int i = 0; i < first.size(); i++) {
      if (first.get(i) == null) {
        continue;
      }
      boolean exists = true;
      for (LinkedList<Boolean> indexList : indexLists) {
        if (indexList.size() <= i || indexList.get(i) == null) {
          exists = false;
          break;
        }
      }
      if (!exists) {
        break;
      } else {
        Combination combination = null;
        for (int index = 0; index < combinations.size(); index++) {
          Combination c = combinations.get(index);

          if (c.position == i + 1) {
            combination = c;
            break;
          }
        }
        if (combination == null) {
          combination = new Combination(i + 1, false);
          combinations.add(combination);
        }

        if (!combination.enabled) {
          List<PortMapping> portMappings = new LinkedList<>();

          for (String portId : ports) {
            Object value = values.get(portId).get(i);
            portMappings.add(new PortMapping(portId, value));
          }
          result.add(new RowMapping(combination.position, portMappings));
        }
      }
    }
    return result;
  }

  @Override
  public void commit(List<RowMapping> mappings) {
    for (RowMapping mapping : mappings) {
      for (Combination combination : combinations) {
        if (combination.position == mapping.getIndex()) {
          combination.enabled = true;
          break;
        }
      }
    }
  }
  
  @Override
  public int enabledCount() {
    return combinations.size();
  }

  public static class Combination {
    @JsonProperty("position")
    int position;
    @JsonProperty("enabled")
    boolean enabled;

    @JsonCreator
    public Combination(@JsonProperty("position") int position, @JsonProperty("enabled") boolean enabled) {
      this.position = position;
      this.enabled = enabled;
    }
  }

  @Override
  public boolean isBlocking() {
    return ScatterMethod.isBlocking(scatterMethod);
  }

  @Override
  public List<Object> valueStructure(String jobId, String portId, UUID rootId) {
    if (combinations.isEmpty()) {
      return new LinkedList<>();
    }
    Collections.sort(combinations, new Comparator<Combination>() {
      @Override
      public int compare(Combination o1, Combination o2) {
        return o1.position - o2.position;
      }
    });
    
    LinkedList<Object> result = new LinkedList<>();
    for (Combination combination : combinations) {
      String scatteredJobId = InternalSchemaHelper.scatterId(jobId, combination.position);
      result.addLast(new JobPortPair(scatteredJobId, portId));
    }
    return result;
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
    return new ArrayList<>();
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
  public boolean skipScatter() {
    return skipScatter;
  }

  @Override
  public void skipScatter(boolean skip) {
    this.skipScatter = skip;
  }

}
