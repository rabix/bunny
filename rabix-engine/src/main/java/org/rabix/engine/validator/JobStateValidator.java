package org.rabix.engine.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.JobState;

public class JobStateValidator {
  
  private static Map<JobState, List<JobState>> transitions = new HashMap<JobState, List<JobState>>();

  static {
    List<JobState> transitionFromPending = new ArrayList<JobState>();
    transitionFromPending.add(JobRecord.JobState.READY);
    transitions.put(JobRecord.JobState.PENDING, transitionFromPending);
    List<JobState> transitionFromReady = new ArrayList<JobState>();
    transitionFromReady.add(JobRecord.JobState.RUNNING);
    transitionFromReady.add(JobRecord.JobState.FAILED);
    transitionFromReady.add(JobRecord.JobState.COMPLETED);
    transitions.put(JobRecord.JobState.READY, transitionFromReady);
    List<JobState> transitionFromRunning = new ArrayList<JobState>();
    transitionFromRunning.add(JobRecord.JobState.COMPLETED);
    transitionFromRunning.add(JobRecord.JobState.FAILED);
    transitions.put(JobRecord.JobState.RUNNING, transitionFromRunning);
    List<JobState> transitionFromCompleted = new ArrayList<JobState>();
    transitionFromCompleted.add(JobRecord.JobState.READY);
    transitions.put(JobRecord.JobState.COMPLETED, transitionFromCompleted);
    List<JobState> transitionFromFailed = new ArrayList<JobState>();
    transitions.put(JobRecord.JobState.FAILED, transitionFromFailed);
    
    transitions = Collections.unmodifiableMap(transitions);
  }
  
  public static JobState checkState(JobRecord jobRecord, JobState jobState) throws JobStateValidationException {
    JobState currentState = jobRecord.getState();
    if (transitions.get(currentState).contains(jobState)) {
      return jobState;
    } else {
      throw new JobStateValidationException("Job state cannot transition from " + jobRecord.getState() + " to " + jobState);
    }
  }

}
