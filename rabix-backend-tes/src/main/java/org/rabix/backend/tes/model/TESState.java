package org.rabix.backend.tes.model;

public enum TESState {
  UNKNOWN,
  QUEUED,
  RUNNING,
  PAUSED,
  COMPLETE,
  ERROR,
  SYSTEMERROR,
  CANCELED,
  INITIALIZING
}
