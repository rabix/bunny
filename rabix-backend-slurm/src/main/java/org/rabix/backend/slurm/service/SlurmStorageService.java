package org.rabix.backend.slurm.service;

import java.nio.file.Path;

public interface SlurmStorageService {

    enum StorageType {
        Local
    }

    Path stagingPath(String... args);
}
