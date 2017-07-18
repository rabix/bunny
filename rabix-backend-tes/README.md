# Overview

rabix-backend-tes is an integration module for the [GA4GH Task Execution Schema](https://github.com/ga4gh/task-execution-schemas) project. It allows the use of a TES server such as [Funnel](https://github.com/ohsu-comp-bio/funnel) for execution of CWL tasks.

# Configuration
Backend type first needs to be set to TES:
```
backend.embedded.types=TES
```

Then the connection with the server:
```
rabix.tes.client-scheme=http
rabix.tes.client-host=localhost
rabix.tes.client-port=8000
```

If your local execution directory isn't accessible to the TES server then a different storage path needs to be set:
```
rabix.tes.storage.base=/tmp
```
Currently we are supporting local directories if you are running TES server locally and Google Cloud Storage otherwise (gs://). GS authentification is taken from the environment's default (`GOOGLE_APPLICATION_CREDENTIALS` environment variable pointing to the GS auth key file or through gs-cli client auth).

If using rabix-cli, tes url and storage path can be set with commandline parameters instead:
```
./rabix ./app.cwl.json ./inputs.json -tes-url=http://localhost:8000 -tes-storage=gs://bucket/path
```

If using rabix-engine-rest, then the project needs to first be compiled using Maven profile `tes` and if the above mentioned configs are present in the config folder then the engine can be started normally: 
```
./rabix-engine
```
