package org.rabix.engine.model.scatter.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.model.ScatterMethod;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.model.scatter.PortMapping;
import org.rabix.engine.model.scatter.RowMapping;
import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.service.VariableRecordService;

import com.google.common.base.Preconditions;

public class ScatterZipStrategy implements ScatterStrategy {

  private LinkedList<Combination> combinations;
  
  private Map<String, LinkedList<Object>> values;
  private Map<String, LinkedList<Boolean>> indexes;

  private final ScatterMethod scatterMethod;
  private final VariableRecordService variableRecordService;
  
  public ScatterZipStrategy(DAGNode dagNode, VariableRecordService variableRecordService) {
    values = new HashMap<>();
    indexes = new HashMap<>();
    combinations = new LinkedList<>();
    
    this.scatterMethod = dagNode.getScatterMethod();
    this.variableRecordService = variableRecordService;
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
  
  public void enable(String port, Object value, Integer position) {
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

  private class Combination {
    int position;
    boolean enabled;

    public Combination(int position, boolean enabled) {
      this.position = position;
      this.enabled = enabled;
    }
  }

  @Override
  public boolean isBlocking() {
    return ScatterMethod.isBlocking(scatterMethod);
  }

  @Override
  public LinkedList<Object> values(String jobId, String portId, String contextId) {
    Collections.sort(combinations, new Comparator<Combination>() {
      @Override
      public int compare(Combination o1, Combination o2) {
        return o1.position - o2.position;
      }
    });
    
    LinkedList<Object> result = new LinkedList<>();
    for (Combination combination : combinations) {
      String scatteredJobId = InternalSchemaHelper.scatterId(jobId, combination.position);
      VariableRecord variableRecord = variableRecordService.find(scatteredJobId, portId, LinkPortType.OUTPUT, contextId);
      result.addLast(variableRecordService.getValue(variableRecord));
    }
    return result;
  }

}
