package org.rabix.backend.tes;

import org.rabix.backend.api.BackendAPI;
import org.rabix.backend.api.BackendAPIException;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendLocal;

public class TESBackend implements BackendAPI {

  @Override
  public Backend start() throws BackendAPIException {
    return new BackendLocal();
  }

  @Override
  public void initialize(Backend backend) throws BackendAPIException {
    // TODO Auto-generated method stub
    
  }
  
}
