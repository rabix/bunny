package org.rabix.engine.store.repository;

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

  void decrement(UUID rootId, String filename);

  class IntermediaryFileEntity {

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

    public void decrement() {
      count = count == null ? -1 : count - 1;
    }

    public void increment() {
      count = count == null ? 1 : count + 1;
    }

    @Override
    public String toString() {
      return "IntermediaryFileEntity [rootId=" + rootId + ", filename=" + filename + ", count=" + count + "]";
    }
  }

  void increment(UUID rootId, String filename);

}
