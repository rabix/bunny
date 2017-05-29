package org.rabix.storage.repository;

public interface AppRepository {

  void insert(String hash, String app);
  
  String get(String hash);
  
}
