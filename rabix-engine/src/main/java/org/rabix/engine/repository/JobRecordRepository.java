package org.rabix.engine.repository;

import java.util.List;

import org.rabix.engine.model.JobRecord;

public interface JobRecordRepository {

  int insert(JobRecord jobRecord);
  
  int update(JobRecord jobRecord);
  
  List<JobRecord> get(String rootId);
  
  JobRecord getRoot(String rootId);
  
  JobRecord get(String id, String rootId);
  
  List<JobRecord> getByParent(String parentId, String rootId);
  
  List<JobRecord> getReady(String rootId);
  
}
