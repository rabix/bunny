package org.rabix.engine.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.engine.model.JobRecord;
import org.rabix.engine.service.JobRecordService.JobState;

public class JobStateValidator {
  
  private static Map<JobState, List<JobState>> transitions = new HashMap<JobState, List<JobState>>();

  static {
    List<JobState> transitionFromPending = new ArrayList<JobState>();
    transitionFromPending.add(JobState.READY);
    transitions.put(JobState.PENDING, transitionFromPending);
    List<JobState> transitionFromReady = new ArrayList<JobState>();
    transitionFromReady.add(JobState.RUNNING);
    transitionFromReady.add(JobState.FAILED);
    transitionFromReady.add(JobState.COMPLETED);
    transitions.put(JobState.READY, transitionFromReady);
    List<JobState> transitionFromRunning = new ArrayList<JobState>();
    transitionFromRunning.add(JobState.COMPLETED);
    transitionFromRunning.add(JobState.FAILED);
    transitions.put(JobState.RUNNING, transitionFromRunning);
    List<JobState> transitionFromCompleted = new ArrayList<JobState>();
    transitionFromCompleted.add(JobState.READY);
    transitions.put(JobState.COMPLETED, transitionFromCompleted);
    List<JobState> transitionFromFailed = new ArrayList<JobState>();
    transitions.put(JobState.FAILED, transitionFromFailed);
    
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
