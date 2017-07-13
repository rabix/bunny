# Overview

rabix-backend-slurm allows to use rabix with [SLURM workload manager](https://slurm.schedmd.com/). Workflows
that must be handled by rabix will be processed and executed alongside with other jobs on the SLURM-managed cluster with regards
to allocated resources and granted rights. Jobs from these worklows can be monitored the same way as other jobs submitted to SLURM.

 
# Configuration

rabix-backend-slurm requires several rabix-cli instances to be set up. The rabix-cli on the controller machine 
needs to have the following configuration:

* Backend type needs to be set to SLURM:
```
backend.embedded.types=SLURM
```
* Path to rabix-cli on worker machines
```
rabix.slurm.rabix-worker-cli=/path/to/rabix
```
Configuration file for rabix-clis on worker machines (if necessary) 
```
rabix.slurm.rabix-worker-cli-config-dir=--configuration-dir /path/to/rabix-cli/config
```
All the machines in the partition must have a shared file directory set up. 

Rabix-clis on worker machines are ordinary rabix-clis and they are installed as specified 
[here](https://github.com/rabix/bunny/blob/master/README.md).



# Run
The jobs are submitted to rabix-cli on the controller machine in the same way as to a regular rabix-cli:
```
./rabix /shared/dir/cwl.json /shared/dir/inputs.json
```
But internally rabix-backend-slurm runs submits all the job steps to the SLURM scheduler and they are executed 
by separate rabix-clis on worker nodes within the partition.

The workflow must be located inside a shared file directory so all worker instances can access it with read-write-execute
permissions.


# Development

rabix-backend-slurm can be run on a local machine without SLURM for development purposes. You need to set
```
rabix.slurm.dev=true
```
for the controller rabix-cli and the path to a worker rabix-cli for running separate job steps.
```
rabix.slurm.rabix-worker-cli=/path/to/rabix
```