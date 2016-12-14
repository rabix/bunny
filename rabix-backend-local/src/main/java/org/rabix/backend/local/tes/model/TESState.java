package org.rabix.backend.local.tes.model;

public enum TESState {
  Unknown,
  Queued,
  Running,
  Paused,
  Complete,
  Error,
  SystemError,
  Canceled;
}
