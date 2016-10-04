{
  "inputs": {
    "message": "test",
    "src": {
      "class": "File",
      "path": "Hello.java"
    },
    "dir": {
      "class": "Directory",
      "path": "hello_directory"
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
      },
      "src": {
      	"type" : "File"
      },
      "dir": {
      	"type" : "Directory"
      }
    },
    "outputs": []
  }
}