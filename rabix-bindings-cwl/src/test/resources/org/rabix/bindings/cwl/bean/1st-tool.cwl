{
  "inputs": {
    "message": "test"
  },
  "app": {
    "cwlVersion": "v1.0",
    "class": "CommandLineTool",
    "baseCommand": "echo",
    "requirements" : {
    	"DockerRequirement" : {
    		"dockerPull" : "ubuntu:latest"
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