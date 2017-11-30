package org.rabix.engine.service;

import java.util.UUID;

public interface GarbageCollectionService {

  void gc(UUID rootId);

  void forceGc(UUID rootId);
}
