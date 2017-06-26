# Overview

**rabix-engine-rest** is a standalone REST server for execution of CWL tasks

# Setup

Running the **rabix-engine** binary sets up just an engine without any executor connected, leaving the registration of executors possible through the REST api. Building the project with `embedded` maven profile and adding the 
`backend.embedded.types=LOCAL` to the configuration (properties files in the config folder) starts a local rabix executor when the engine is started. Similarily building it with `tes` profile and setting the proper [configuration for TES](https://github.com/rabix/bunny/blob/develop/rabix-backend-tes/README.md) starts an embeded TES client that converts CWL tasks to TES format and then uses the configured TES server's REST api to submit and execute them.

# API

Submitting a job is done by sending a [Job object](https://github.com/rabix/bunny/blob/master/rabix-bindings/src/main/java/org/rabix/bindings/model/Job.java) to the following url:

`POST /v0/engine/jobs`

Example body of the request:

```
{
  "app":"file:///git/bunny/examples/dna2protein/dna2protein.cwl.json",
    "inputs": {
      "input_file": {
        "class":"File",
        "path":"/git/bunny/examples/dna2protein/data/input.txt"
      }
    }
}
```

`app` parameter can be a url to the CWL workflow or tool, or it can be a base64 encoded string with the actual content of the CWL file.

Additional integer parameter `batch` can be placed in the request headers that makes the engine create and run that many copies of the submitted job instead of just one.
