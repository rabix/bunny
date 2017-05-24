{
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
    "inputs": [
      { "id": "message",
        "type": "string",
        "inputBinding": {
          "position": 1
        }
      },
      {
        "id": "src",
        "type": "File"
      },
      { "id": "dir",
      	"type" : "Directory"
      }
    ],
    "outputs": [],
    "arguments": [
        "one",
        {
            "position": 5,
            "valueFrom": "const"
        },
        {
            "position": 6
        },
        42
    ]
  }
