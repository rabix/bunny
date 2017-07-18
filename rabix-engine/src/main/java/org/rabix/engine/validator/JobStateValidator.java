package org.rabix.engine.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.engine.store.model.JobRecord;

public class JobStateValidator {
  
  private static Map<JobRecord.JobState, List<JobRecord.JobState>> transitions = new HashMap<JobRecord.JobState, List<JobRecord.JobState>>();

  static {
    List<JobRecord.JobState> transitionFromPending = new ArrayList<JobRecord.JobState>();
    transitionFromPending.add(JobRecord.JobState.READY);
    transitionFromPending.add(JobRecord.JobState.COMPLETED);
    transitions.put(JobRecord.JobState.PENDING, transitionFromPending);
    List<JobRecord.JobState> transitionFromReady = new ArrayList<JobRecord.JobState>();
    transitionFromReady.add(JobRecord.JobState.RUNNING);
    transitionFromReady.add(JobRecord.JobState.FAILED);
    transitionFromReady.add(JobRecord.JobState.COMPLETED);
    transitions.put(JobRecord.JobState.READY, transitionFromReady);
    List<JobRecord.JobState> transitionFromRunning = new ArrayList<JobRecord.JobState>();
    transitionFromRunning.add(JobRecord.JobState.COMPLETED);
    transitionFromRunning.add(JobRecord.JobState.ABORTED);
    transitionFromRunning.add(JobRecord.JobState.FAILED);
    transitions.put(JobRecord.JobState.RUNNING, transitionFromRunning);
    List<JobRecord.JobState> transitionFromCompleted = new ArrayList<JobRecord.JobState>();
    transitionFromCompleted.add(JobRecord.JobState.READY);
    transitions.put(JobRecord.JobState.COMPLETED, transitionFromCompleted);
    List<JobRecord.JobState> transitionFromFailed = new ArrayList<JobRecord.JobState>();
    transitions.put(JobRecord.JobState.FAILED, transitionFromFailed);
    List<JobRecord.JobState> transitionFromAborted = new ArrayList<JobRecord.JobState>();
    transitionFromAborted.add(JobRecord.JobState.COMPLETED);
    transitions.put(JobRecord.JobState.ABORTED, transitionFromAborted);
    
    transitions = Collections.unmodifiableMap(transitions);
  }
  
  public static JobRecord.JobState checkState(JobRecord jobRecord, JobRecord.JobState jobState) throws JobStateValidationException {
    return checkState(jobRecord.getState(), jobState);
  }
  
  public static JobRecord.JobState checkState(JobRecord.JobState currentState, JobRecord.JobState jobState) throws JobStateValidationException {
    if (transitions.get(currentState).contains(jobState)) {
      return jobState;
    } else {
      throw new JobStateValidationException("Job state cannot transition from " + currentState + " to " + jobState);
    }
  }

}
