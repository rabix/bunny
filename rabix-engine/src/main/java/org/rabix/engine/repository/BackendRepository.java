package org.rabix.engine.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.Backend.BackendStatus;

public interface BackendRepository {

  void insert(UUID id, Backend backend, Timestamp heartbeatInfo);

  void update(UUID id, Backend configuration);
  
  Backend get(UUID id);
  
  List<Backend> getByStatus(BackendStatus status);
  
  void updateHeartbeatInfo(UUID id, Timestamp heartbeatInfo);
  
  void updateStatus(UUID id, BackendStatus status);
  
  Timestamp getHeartbeatInfo(UUID id);
  
  public class BackendEntity {
    
    Backend backend;
    Timestamp heartbeatInfo;

    public BackendEntity(Backend backend, Timestamp heartbeatInfo) {
      super();
      this.backend = backend;
      this.heartbeatInfo = heartbeatInfo;
    }

    public Backend getBackend() {
      return backend;
    }
    
    public void setBackend(Backend backend) {
      this.backend = backend;
    }
    
    public Timestamp getHeartbeatInfo() {
      return heartbeatInfo;
    }
    
    public void setHeartbeatInfo(Timestamp heartbeatInfo) {
      this.heartbeatInfo = heartbeatInfo;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((backend == null) ? 0 : backend.hashCode());
      result = prime * result + ((heartbeatInfo == null) ? 0 : heartbeatInfo.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      BackendEntity other = (BackendEntity) obj;
      if (backend == null) {
        if (other.backend != null)
          return false;
      } else if (!backend.equals(other.backend))
        return false;
      if (heartbeatInfo == null) {
        if (other.heartbeatInfo != null)
          return false;
      } else if (!heartbeatInfo.equals(other.heartbeatInfo))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "BackendEntity [backend=" + backend + ", timestamp=" + heartbeatInfo + "]";
    }
  }

}