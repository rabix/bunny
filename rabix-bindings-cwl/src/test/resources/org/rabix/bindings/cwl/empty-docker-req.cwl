{
    "cwlVersion": "v1.0",
    "class": "CommandLineTool",
    "baseCommand": "echo",
    "requirements": {
      "DockerRequirement": {
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
