package org.rabix.backend.tes.model;

public enum TESState {
  UNKNOWN,
  QUEUED,
  RUNNING,
  PAUSED,
  COMPLETE,
  EXECUTOR_ERROR,
  SYSTEM_ERROR,
  CANCELED,
  INITIALIZING
}
