package org.rabix.backend.slurm.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SlurmJobService {
    private final static Logger logger = LoggerFactory.getLogger(SlurmJobService.class);

    private final Map<UUID, String> jobDataMap = new HashMap<>();


    public void save(UUID id, String slurmJobid){
        jobDataMap.put(id, slurmJobid);
    }

    public String getSlurmJob(UUID id){
        return jobDataMap.get(id);
    }
}



