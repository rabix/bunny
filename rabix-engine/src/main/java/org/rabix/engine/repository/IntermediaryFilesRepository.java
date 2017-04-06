package org.rabix.engine.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IntermediaryFilesRepository {

  void insert(UUID rootId, String filename, Integer count);
  
  void update(UUID rootId, String filename, Integer count);
  
  void delete(UUID rootId, String filename);
  
  void delete(UUID rootId);
  
  void deleteByRootIds(Set<UUID> rootIds);
  
  List<IntermediaryFileEntity> get(UUID rootId);
  
  public class IntermediaryFileEntity {
    
    UUID rootId;
    String filename;
    Integer count;
    
    public IntermediaryFileEntity(UUID rootId, String filename, Integer count) {
      super();
      this.rootId = rootId;
      this.filename = filename;
      this.count = count;
    }
    public UUID getRootId() {
      return rootId;
    }
    public void setRootId(UUID rootId) {
      this.rootId = rootId;
    }
    public String getFilename() {
      return filename;
    }
    public void setFilename(String filename) {
      this.filename = filename;
    }
    public Integer getCount() {
      return count;
    }
    public void setCount(Integer count) {
      this.count = count;
    }
    
    @Override
    public String toString() {
      return "IntermediaryFileEntity [filename=" + filename + ", count=" + count + "]";
    }
    
  }
  
}
