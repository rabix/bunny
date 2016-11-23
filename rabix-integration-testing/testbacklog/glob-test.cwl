{
  "id": "https://api.sbgenomics.com/v2/apps/sinisa/platform-workflows-debug/globsubdirectory/2/raw/",
  "class": "CommandLineTool",
  "label": "globSubdirectory",
  "description": "",
  "requirements": [
    {
      "fileDef": [
        {
          "fileContent": "import os\n\nif __name__=='__main__':\n    os.mkdir(\"./test\")\n    with open(\"./test/output.txt\", \"w\"):\n        pass\n\n    with open(\"../output_1.txt\", \"w\"):\n        pass\n    os.mkdir(\"./test1\")\n    \n    with open(\"./test1/output.txt\", \"w\"):\n        pass",
          "filename": "createOutput.py"
        }
      ],
      "class": "CreateFileRequirement"
    },
    {
      "id": "#cwl-js-engine",
      "class": "ExpressionEngineRequirement",
      "requirements": [
        {
          "dockerPull": "rabix/js-engine",
          "class": "DockerRequirement"
        }
      ]
    }
  ],
  "inputs": [
    {
      "id": "#input",
      "inputBinding": {
        "separate": true,
        "sbg:cmdInclude": true
      },
      "type": [
        "null",
        "File"
      ]
    }
  ],
  "outputs": [
    {
      "outputBinding": {
        "glob": "test/output.txt"
      },
      "id": "#output",
      "type": [
        "null",
        "File"
      ]
    },
    {
      "outputBinding": {
        "glob": {
          "engine": "#cwl-js-engine",
          "class": "Expression",
          "script": "$job.inputs.input.path"
        }
      },
      "id": "#output_abs",
      "type": [
        "null",
        "File"
      ]
    },
    {
      "outputBinding": {
        "glob": "*/output.txt"
      },
      "id": "#output_list",
      "type": [
        "null",
        {
          "name": "output_list",
          "type": "array",
          "items": "File"
        }
      ]
    },
    {
      "outputBinding": {
        "glob": "../*.txt"
      },
      "id": "#output_relative",
      "type": [
        "null",
        "File"
      ]
    },
    {
      "type": [
        "null",
        {
          "name": "output_curly",
          "type": "array",
          "items": "File"
        }
      ],
      "outputBinding": {
        "glob": {
          "class": "Expression",
          "engine": "#cwl-js-engine",
          "script": "'{' + $job.inputs.input.path + ',*/output.txt}'"
        }
      },
      "id": "#output_curly"
    }
  ],
  "hints": [
    {
      "value": 1,
      "class": "sbg:CPURequirement"
    },
    {
      "value": 1000,
      "class": "sbg:MemRequirement"
    },
    {
      "dockerPull": "images.sbgenomics.com/sinisa/ubuntu:latest",
      "dockerImageId": "",
      "class": "DockerRequirement"
    }
  ],
  "baseCommand": [
    "python3",
    "createOutput.py"
  ],
  "stdin": "",
  "stdout": "",
  "successCodes": [],
  "temporaryFailCodes": [],
  "arguments": [],
  "sbg:createdBy": "sinisa",
  "sbg:validationErrors": [],
  "sbg:latestRevision": 2,
  "sbg:createdOn": 1470131601,
  "sbg:job": {
    "inputs": {
      "input": {
        "path": "/path/to/input.ext",
        "size": 0,
        "secondaryFiles": [],
        "class": "File"
      }
    },
    "allocatedResources": {
      "mem": 1000,
      "cpu": 1
    }
  },
  "sbg:project": "sinisa/platform-workflows-debug",
  "sbg:modifiedOn": 1470150073,
  "sbg:sbgMaintained": false,
  "sbg:revisionsInfo": [
    {
      "sbg:modifiedOn": 1470131601,
      "sbg:modifiedBy": "sinisa",
      "sbg:revision": 0,
      "sbg:revisionNotes": null
    },
    {
      "sbg:modifiedOn": 1470132186,
      "sbg:modifiedBy": "sinisa",
      "sbg:revision": 1,
      "sbg:revisionNotes": null
    },
    {
      "sbg:modifiedOn": 1470150073,
      "sbg:modifiedBy": "sinisa",
      "sbg:revision": 2,
      "sbg:revisionNotes": null
    }
  ],
  "sbg:image_url": null,
  "sbg:id": "sinisa/platform-workflows-debug/globsubdirectory/2",
  "sbg:cmdPreview": "python3 createOutput.py",
  "sbg:revision": 2,
  "sbg:contributors": [
    "sinisa"
  ],
  "sbg:modifiedBy": "sinisa"
}
