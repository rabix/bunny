{
  "inputs": {
    "message": "test",
    "src": {
      "class": "File",
      "path": "Hello.java"
    }
  },
  "app": {
    "cwlVersion": "v1.0",
    "class": "CommandLineTool",
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
      }
    },
    "outputs": []
  }
}