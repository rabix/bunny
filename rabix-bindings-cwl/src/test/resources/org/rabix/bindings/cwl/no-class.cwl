{
    "cwlVersion": "v1.0",
    "baseCommand": "echo",
    "requirements": {
      "DockerRequirement": {
        "dockerPull": "ubuntu:latest"
      },
      "InitialWorkDirRequirement": {
        "listing": [
          "$(inputs.src)"
        ]
      }
    },
    "inputs": {
      "message": {
        "type": "string",
        "inputBinding": {
          "position": 1
        }
      },
      "src": "File",
      "dir": {
      	"type" : "Directory"
      }
    },
    "outputs": []
  }
