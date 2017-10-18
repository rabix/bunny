{
    "sbg:sbgMaintained": false,
    "id": "https://api.sbgenomics.com/v2/apps/bixqa/qa-load-2017-07-31-18/chip-seq-bwa-alignment-and-peak-calling-macs2/3/raw/",
    "sbg:categories": [
        "chip-seq"
    ],
    "sbg:batchInput": "#sample_files",
    "sbg:revision": 3,
    "label": "ChIP-seq BWA Alignment and Peak Calling MACS2 2.1.1",
    "steps": [
        {
            "inputs": [
                {
                    "source": [
                        "#sample_files"
                    ],
                    "id": "#SBG_Set_Metadata.input_files"
                },
                {
                    "default": "sample",
                    "id": "#SBG_Set_Metadata.chip_seq"
                }
            ],
            "sbg:x": 247.24568128237476,
            "id": "#SBG_Set_Metadata",
            "sbg:y": 250.5264076601027,
            "run": {
                "sbg:modifiedOn": 1499419709,
                "id": "bix-demo/sbgtools-demo/sbg-set-metadata/1",
                "sbg:categories": [
                    "ChIP-seq",
                    "Other"
                ],
                "sbg:project": "bix-demo/sbgtools-demo",
                "sbg:toolkit": "SBGTools",
                "label": "SBG ChIP-seq Set Metadata",
                "sbg:sbgMaintained": false,
                "x": 247.24568128237476,
                "successCodes": [],
                "sbg:id": "admin/sbg-public-data/sbg-set-metadata/1",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "SBGTools - Demo New",
                "description": "Tool for setting \"chip-seq\" field in metadata, used only for ChIP-seq pipelines.",
                "sbg:image_url": null,
                "sbg:latestRevision": 1,
                "requirements": [
                    {
                        "class": "CreateFileRequirement",
                        "fileDef": [
                            {
                                "filename": "set_metadata.py",
                                "fileContent": "\"\"\"\nUsage:\n    group_by_metadata.py\n\nDescription:\n    Prepare Input Files manipulates / creates an output list as group of files with the same metadata.\n\nOptions:\n\n    -h, --help            Show this message.\n\n    -v, -V, --version     Tool version.\n\"\"\"\n\nimport json\n\n\njob = \"\"\nwith open('job.json') as data_file:\n    job = json.load(data_file)\n\nif \"inputs\" in job and \"input_files\" in job[\"inputs\"]:\n    files = job[\"inputs\"][\"input_files\"]\nelse: files = []\n    \nif \"inputs\" in job and \"chip_seq\" in job[\"inputs\"]:\n    metadata = job[\"inputs\"][\"chip_seq\"]\nelse: metadata = \"\"\n    \nprint(metadata)\n\n\noutput_files = []\nfor f in files:\n    d = {'class': 'File'}\n    d['path'] = f[\"path\"]\n            \n    if \"size\" in f:\n        d['size'] = f[\"size\"]\n                \n    if \"contents\" in f:\n        d['contents'] = f[\"contents\"]\n                \n    if \"name\" in f:\n        d['name'] = f[\"name\"]\n            \n    if \"checksum\" in f:\n        d['checksum'] = f[\"checksum\"]\n            \n    if \"location\" in f:\n        d['location'] = f[\"location\"]\n            \n    if \"metadata\" in f:\n        d['metadata'] = f[\"metadata\"]\n                \n    if \"secondaryFiles\" in f:\n        d['secondaryFiles'] = f[\"secondaryFiles\"]\n\n    if \"metadata\" in f: \n        d['metadata']['chip-seq'] = metadata\n    else:\n        d['metadata'] = {'chip-seq' : metadata}\n        \n    output_files.append(d)\n            \ndata = {}\nif output_files:\n    data['output_files'] = output_files\n    \nwith open('cwl.output.json', 'w') as w:\n    json.dump(data, w)"
                            }
                        ]
                    }
                ],
                "arguments": [],
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499419693,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499419709,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    }
                ],
                "baseCommand": [
                    "python",
                    "set_metadata.py"
                ],
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 1000,
                        "cpu": 1
                    },
                    "inputs": {
                        "input_files": [
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_file-1.ext",
                                "class": "File"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_file-2.ext",
                                "class": "File"
                            }
                        ],
                        "chip_seq": "chip_seq-string-value"
                    }
                },
                "sbg:revision": 1,
                "y": 250.5264076601027,
                "sbg:toolAuthor": "Seven Bridges Genomics",
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "sbg:category": "Input",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "sbg:stageInput": "copy",
                        "id": "#input_files",
                        "required": false,
                        "label": "Input Files",
                        "description": "Input Files.",
                        "sbg:fileTypes": "FASTQ, FASTQ.GZ"
                    },
                    {
                        "sbg:category": "Option",
                        "type": [
                            "null",
                            "string"
                        ],
                        "label": "Chip-seq metadata",
                        "description": "Value of chip-seq metadata.",
                        "id": "#chip_seq"
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:cmdPreview": "python set_metadata.py",
                "stdout": "",
                "outputs": [
                    {
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "label": "Output files",
                        "id": "#output_files",
                        "outputBinding": {
                            "glob": "*.*"
                        },
                        "description": "Output files with metadata set."
                    }
                ],
                "stdin": "",
                "cwlVersion": "sbg:draft-2",
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
                        "dockerPull": "images.sbgenomics.com/milos_jordanski/python:2.7",
                        "dockerImageId": "",
                        "class": "DockerRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499419693
            },
            "outputs": [
                {
                    "id": "#SBG_Set_Metadata.output_files"
                }
            ]
        },
        {
            "inputs": [
                {
                    "source": [
                        "#SBG_Select_Control.output_control_files"
                    ],
                    "id": "#SBG_Set_Metadata_1.input_files"
                },
                {
                    "default": "control",
                    "id": "#SBG_Set_Metadata_1.chip_seq"
                }
            ],
            "sbg:x": 276.9650010982483,
            "id": "#SBG_Set_Metadata_1",
            "sbg:y": 375.71942571346267,
            "run": {
                "sbg:modifiedOn": 1499419709,
                "id": "bix-demo/sbgtools-demo/sbg-set-metadata/1",
                "sbg:categories": [
                    "ChIP-seq",
                    "Other"
                ],
                "sbg:project": "bix-demo/sbgtools-demo",
                "sbg:toolkit": "SBGTools",
                "label": "SBG ChIP-seq Set Metadata",
                "sbg:sbgMaintained": false,
                "x": 276.9650010982483,
                "successCodes": [],
                "sbg:id": "admin/sbg-public-data/sbg-set-metadata/1",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "SBGTools - Demo New",
                "description": "Tool for setting \"chip-seq\" field in metadata, used only for ChIP-seq pipelines.",
                "sbg:image_url": null,
                "sbg:latestRevision": 1,
                "requirements": [
                    {
                        "class": "CreateFileRequirement",
                        "fileDef": [
                            {
                                "filename": "set_metadata.py",
                                "fileContent": "\"\"\"\nUsage:\n    group_by_metadata.py\n\nDescription:\n    Prepare Input Files manipulates / creates an output list as group of files with the same metadata.\n\nOptions:\n\n    -h, --help            Show this message.\n\n    -v, -V, --version     Tool version.\n\"\"\"\n\nimport json\n\n\njob = \"\"\nwith open('job.json') as data_file:\n    job = json.load(data_file)\n\nif \"inputs\" in job and \"input_files\" in job[\"inputs\"]:\n    files = job[\"inputs\"][\"input_files\"]\nelse: files = []\n    \nif \"inputs\" in job and \"chip_seq\" in job[\"inputs\"]:\n    metadata = job[\"inputs\"][\"chip_seq\"]\nelse: metadata = \"\"\n    \nprint(metadata)\n\n\noutput_files = []\nfor f in files:\n    d = {'class': 'File'}\n    d['path'] = f[\"path\"]\n            \n    if \"size\" in f:\n        d['size'] = f[\"size\"]\n                \n    if \"contents\" in f:\n        d['contents'] = f[\"contents\"]\n                \n    if \"name\" in f:\n        d['name'] = f[\"name\"]\n            \n    if \"checksum\" in f:\n        d['checksum'] = f[\"checksum\"]\n            \n    if \"location\" in f:\n        d['location'] = f[\"location\"]\n            \n    if \"metadata\" in f:\n        d['metadata'] = f[\"metadata\"]\n                \n    if \"secondaryFiles\" in f:\n        d['secondaryFiles'] = f[\"secondaryFiles\"]\n\n    if \"metadata\" in f: \n        d['metadata']['chip-seq'] = metadata\n    else:\n        d['metadata'] = {'chip-seq' : metadata}\n        \n    output_files.append(d)\n            \ndata = {}\nif output_files:\n    data['output_files'] = output_files\n    \nwith open('cwl.output.json', 'w') as w:\n    json.dump(data, w)"
                            }
                        ]
                    }
                ],
                "arguments": [],
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499419693,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499419709,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    }
                ],
                "baseCommand": [
                    "python",
                    "set_metadata.py"
                ],
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 1000,
                        "cpu": 1
                    },
                    "inputs": {
                        "input_files": [
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_file-1.ext",
                                "class": "File"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_file-2.ext",
                                "class": "File"
                            }
                        ],
                        "chip_seq": "chip_seq-string-value"
                    }
                },
                "sbg:revision": 1,
                "y": 375.71942571346267,
                "sbg:toolAuthor": "Seven Bridges Genomics",
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "sbg:category": "Input",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "sbg:stageInput": "copy",
                        "id": "#input_files",
                        "required": false,
                        "label": "Input Files",
                        "description": "Input Files.",
                        "sbg:fileTypes": "FASTQ, FASTQ.GZ"
                    },
                    {
                        "sbg:category": "Option",
                        "type": [
                            "null",
                            "string"
                        ],
                        "id": "#chip_seq",
                        "required": false,
                        "label": "Chip-seq metadata",
                        "description": "Value of chip-seq metadata."
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:cmdPreview": "python set_metadata.py",
                "stdout": "",
                "appUrl": "/u/bix-demo/sbgtools-demo/apps/#bix-demo/sbgtools-demo/sbg-set-metadata/1",
                "outputs": [
                    {
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "label": "Output files",
                        "id": "#output_files",
                        "outputBinding": {
                            "glob": "*.*"
                        },
                        "description": "Output files with metadata set."
                    }
                ],
                "stdin": "",
                "cwlVersion": "sbg:draft-2",
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
                        "dockerPull": "images.sbgenomics.com/milos_jordanski/python:2.7",
                        "dockerImageId": "",
                        "class": "DockerRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499419693
            },
            "outputs": [
                {
                    "id": "#SBG_Set_Metadata_1.output_files"
                }
            ]
        },
        {
            "inputs": [
                {
                    "source": [
                        "#SBG_Set_Metadata.output_files",
                        "#SBG_Set_Metadata_1.output_files"
                    ],
                    "id": "#SBG_Scatter_Prepare.files"
                }
            ],
            "sbg:x": 429.14920708798786,
            "id": "#SBG_Scatter_Prepare",
            "sbg:y": 250.73688235164502,
            "run": {
                "sbg:sbgMaintained": false,
                "id": "bix-demo/sbgtools-demo/sbg-scatter-prepare/1",
                "sbg:project": "bix-demo/sbgtools-demo",
                "sbg:revision": 1,
                "label": "SBG Scatter Prepare",
                "arguments": [],
                "x": 429.14920708798786,
                "requirements": [
                    {
                        "class": "CreateFileRequirement",
                        "fileDef": [
                            {
                                "filename": "group_by_metadata.py",
                                "fileContent": "\"\"\"\nUsage:\n    group_by_metadata.py\n\nDescription:\n    Prepare Input Files manipulates / creates an output list as group of files with the same metadata.\n\nOptions:\n\n    -h, --help            Show this message.\n\n    -v, -V, --version     Tool version.\n\"\"\"\n\nimport json\n\n\njob = \"\"\nwith open('job.json') as data_file:\n    job = json.load(data_file)\nfiles = job[\"inputs\"][\"files\"]\n\ncontrol_files = []\nreal_samples = []\n\n\nfor file_list in files:\n    if file_list:\n        for f in file_list:\n            print(f)\n            d = {'class': 'File'}\n            d['path'] = f[\"path\"]\n\n            if \"size\" in f:\n                d['size'] = f[\"size\"]\n\n            if \"contents\" in f:\n                d['contents'] = f[\"contents\"]\n\n            if \"name\" in f:\n                d['name'] = f[\"name\"]\n\n            if \"checksum\" in f:\n                d['checksum'] = f[\"checksum\"]\n\n            if \"location\" in f:\n                d['location'] = f[\"location\"]\n\n            if \"metadata\" in f:\n                d['metadata'] = f[\"metadata\"]\n\n            if \"secondaryFiles\" in f:\n                d['secondaryFiles'] = f[\"secondaryFiles\"]\n\n            if \"metadata\" in f and \"chip-seq\" in f[\"metadata\"] and f[\"metadata\"][\"chip-seq\"]==\"sample\":\n                real_samples.append(d)\n            else:\n                control_files.append(d)\n\nif not control_files:\n    groups=[real_samples]\nelif not real_samples:\n    groups = [control_files]\nelse:\n    groups = [control_files, real_samples]\n            \ndata = {}\ndata['grouped_files'] = groups\nwith open('cwl.output.json', 'w') as w:\n    json.dump(data, w)"
                            }
                        ]
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:id": "admin/sbg-public-data/sbg-scatter-prepare/1",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "SBGTools - Demo New",
                "description": "SBG Scatter Prepare prepares inputs for scattering.",
                "stdout": "",
                "sbg:latestRevision": 1,
                "sbg:modifiedOn": 1499419866,
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499419831,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499419866,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    }
                ],
                "baseCommand": [
                    "python",
                    "group_by_metadata.py"
                ],
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 1000,
                        "cpu": 1
                    },
                    "inputs": {
                        "files": [
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_bam-1.ext",
                                "class": "File"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_bam-2.ext",
                                "class": "File"
                            }
                        ]
                    }
                },
                "outputs": [
                    {
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "label": "Grouped files",
                        "id": "#grouped_files",
                        "outputBinding": {
                            "glob": "*.*"
                        },
                        "description": "Grouped files."
                    }
                ],
                "y": 250.73688235164502,
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "sbg:category": "Input",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "id": "#files",
                        "required": false,
                        "label": "Input files",
                        "description": "Input files."
                    }
                ],
                "successCodes": [],
                "sbg:cmdPreview": "python group_by_metadata.py",
                "sbg:image_url": null,
                "appUrl": "/u/bix-demo/sbgtools-demo/apps/#bix-demo/sbgtools-demo/sbg-scatter-prepare/1",
                "stdin": "",
                "cwlVersion": "sbg:draft-2",
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
                        "dockerPull": "images.sbgenomics.com/milos_jordanski/python:2.7",
                        "dockerImageId": "",
                        "class": "DockerRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499419831
            },
            "outputs": [
                {
                    "id": "#SBG_Scatter_Prepare.grouped_files"
                }
            ]
        },
        {
            "inputs": [
                {
                    "source": [
                        "#SBG_Merge_ChIP_seq_QC_metrics_1.fragLen"
                    ],
                    "id": "#SBG_ChIP_seq_Select_by_Metadata.input_fraglen"
                }
            ],
            "sbg:x": 1771.667303151577,
            "id": "#SBG_ChIP_seq_Select_by_Metadata",
            "sbg:y": 215.83344214492774,
            "run": {
                "sbg:sbgMaintained": false,
                "id": "bix-demo/sbgtools-demo/sbg-chip-seq-select-by-metadata/1",
                "sbg:project": "bix-demo/sbgtools-demo",
                "sbg:revision": 1,
                "label": "SBG ChIP-seq Select by Metadata",
                "arguments": [],
                "x": 1771.667303151577,
                "requirements": [
                    {
                        "requirements": [
                            {
                                "dockerPull": "rabix/js-engine",
                                "class": "DockerRequirement"
                            }
                        ],
                        "class": "ExpressionEngineRequirement",
                        "id": "#cwl-js-engine"
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:id": "admin/sbg-public-data/sbg-chip-seq-select-by-metadata/1",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "SBGTools - Demo New",
                "description": "SBG ChIP-seq Select by Metadata inputs a list of integers and outputs a first non-null element.",
                "stdout": "",
                "sbg:latestRevision": 1,
                "sbg:modifiedOn": 1499419925,
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499419910,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499419925,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    }
                ],
                "baseCommand": [
                    {
                        "script": "{\n  inputs = [].concat($job.inputs.input_fraglen)\n  for (i = 0; i < inputs.length; i++)\n    if(inputs[i])\n      return \"echo \" + inputs[i] + \" > tmp.txt\"\n  return \"echo \\\"All elements are None\\\"\"\n}",
                        "class": "Expression",
                        "engine": "#cwl-js-engine"
                    }
                ],
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 1000,
                        "cpu": 1
                    },
                    "inputs": {
                        "input_fraglen": [
                            4,
                            1
                        ]
                    }
                },
                "outputs": [
                    {
                        "type": [
                            "null",
                            "int"
                        ],
                        "label": "Output fraglen",
                        "id": "#output_fraglen",
                        "outputBinding": {
                            "glob": "tmp.txt",
                            "loadContents": true,
                            "outputEval": {
                                "script": "{\n\treturn parseInt($self[0].contents)\n}",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            }
                        },
                        "description": "Output fraglen."
                    }
                ],
                "y": 215.83344214492774,
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "sbg:includeInPorts": true,
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "int"
                            }
                        ],
                        "id": "#input_fraglen",
                        "required": false,
                        "label": "Input lengths",
                        "description": "Input lengths."
                    }
                ],
                "successCodes": [],
                "sbg:cmdPreview": "echo 4 > tmp.txt",
                "sbg:image_url": null,
                "appUrl": "/u/bix-demo/sbgtools-demo/apps/#bix-demo/sbgtools-demo/sbg-chip-seq-select-by-metadata/1",
                "stdin": "",
                "cwlVersion": "sbg:draft-2",
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
                        "dockerPull": "ubuntu:14.04",
                        "dockerImageId": "",
                        "class": "DockerRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499419910
            },
            "outputs": [
                {
                    "id": "#SBG_ChIP_seq_Select_by_Metadata.output_fraglen"
                }
            ]
        },
        {
            "inputs": [
                {
                    "id": "#Picard_MarkDuplicates_1.verbosity"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.validation_stringency"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.sorting_collections_size_ratio"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.remove_duplicates"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.read_name_regex"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.quiet"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.program_record"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.program_group_ver"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.program_group_name"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.program_group_command_line"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.output_type"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.optical_duplicate_pixel_distance"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.memory_per_job"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.max_sequences_for_disk_read_ends_map"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.max_records_in_ram"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.max_file_handles_for_read_ends_map"
                },
                {
                    "source": [
                        "#BWA_Alignment_and_Filtering_1.aligned_reads"
                    ],
                    "id": "#Picard_MarkDuplicates_1.input_bam"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.duplicate_scoring_strategy"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.create_index"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.compression_level"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.comment"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.assume_sorted"
                }
            ],
            "run": {
                "sbg:modifiedOn": 1470819180,
                "id": "bix-demo/picard-1-140-demo/picard-markduplicates-1-140/3",
                "sbg:links": [
                    {
                        "label": "Homepage",
                        "id": "http://broadinstitute.github.io/picard/"
                    },
                    {
                        "label": "Source Code",
                        "id": "https://github.com/broadinstitute/picard/releases/tag/1.140"
                    },
                    {
                        "label": "Wiki",
                        "id": "http://broadinstitute.github.io/picard/"
                    },
                    {
                        "label": "Download",
                        "id": "https://github.com/broadinstitute/picard/zipball/master"
                    },
                    {
                        "label": "Publication",
                        "id": "http://broadinstitute.github.io/picard/"
                    }
                ],
                "sbg:project": "bix-demo/picard-1-140-demo",
                "sbg:toolkit": "Picard",
                "label": "Picard MarkDuplicates",
                "x": 760.8334245681787,
                "sbg:toolkitVersion": "1.140",
                "sbg:revisionNotes": "Input categories added.",
                "stdin": "",
                "successCodes": [],
                "sbg:id": "admin/sbg-public-data/picard-markduplicates-1-140/3",
                "sbg:contributors": [
                    "mladenlSBG",
                    "bix-demo"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "Picard 1.140 - Demo New",
                "description": "Picard MarkDuplicates examines aligned records in the supplied SAM or BAM file to locate duplicate molecules. All records are then written to the output file with the duplicate records flagged.",
                "sbg:image_url": null,
                "sbg:latestRevision": 3,
                "requirements": [
                    {
                        "engineCommand": "cwl-engine.js",
                        "requirements": [
                            {
                                "dockerPull": "rabix/js-engine",
                                "class": "DockerRequirement"
                            }
                        ],
                        "class": "ExpressionEngineRequirement",
                        "id": "#cwl-js-engine"
                    }
                ],
                "arguments": [
                    {
                        "valueFrom": {
                            "script": "{\n  input_bam = [].concat($job.inputs.input_bam)\n  filename =input_bam[0].path.split('/').slice(-1)[0];\n  return filename.split('.').slice(0, -1).concat(\"metrics\").join(\".\");\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "separate": false,
                        "prefix": "METRICS_FILE="
                    },
                    {
                        "valueFrom": {
                            "script": "{\n  input_bam = [].concat($job.inputs.input_bam)\n  filename = input_bam[0].path.split('/').slice(-1)[0];\n  ext = $job.inputs.output_type;\n  filebase = filename.split('.').slice(0, -1).join('.') + '.deduped.';\n  \nif (ext === \"BAM\")\n{\n    return filebase + 'bam';\n    }\n\nelse if (ext === \"SAM\")\n{\n    return filebase + 'sam';\n}\n\nelse \n{\n\treturn filebase + filename.split('.').slice(-1)[0];\n}\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "separate": false,
                        "prefix": "OUTPUT="
                    },
                    {
                        "valueFrom": {
                            "script": "{\n  input_bam = [].concat($job.inputs.input_bam)\n  filename = input_bam[0].path\n  \n  /* figuring out output file type */\n  ext = $job.inputs.output_type\n  if (ext === \"BAM\")\n  {\n    out_extension = \"BAM\"\n  }\n  else if (ext === \"SAM\")\n  {\n    out_extension = \"SAM\"\n  }\n  else \n  {\n\tout_extension = filename.split('.').slice(-1)[0].toUpperCase()\n  }  \n  \n  /* if exist moving .bai in bam.bai */\n  if ($job.inputs.create_index === 'True' && out_extension == \"BAM\")\n  {\n    \n    old_name = filename.split('.').slice(0, -1).concat('deduped.bai').join('.').replace(/^.*[\\\\\\/]/, '')\n    new_name = filename.split('.').slice(0, -1).concat('deduped.bam.bai').join('.').replace(/^.*[\\\\\\/]/, '')\n    return \"; mv \" + \" \" + old_name + \" \" + new_name\n  }\n\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 2001,
                        "separate": true
                    }
                ],
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "SAM/BAM-Processing"
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1450911260,
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1450911261,
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1450911262,
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revision": 2,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1470819180,
                        "sbg:modifiedBy": "mladenlSBG",
                        "sbg:revision": 3,
                        "sbg:revisionNotes": "Input categories added."
                    }
                ],
                "sbg:license": "MIT License, Apache 2.0 Licence",
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 2048,
                        "cpu": 1
                    },
                    "inputs": {
                        "max_file_handles_for_read_ends_map": 0,
                        "duplicate_scoring_strategy": "SUM_OF_BASE_QUALITIES",
                        "input_bam": [
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "input_bam.ext",
                                "class": "File"
                            }
                        ],
                        "memory_per_job": 0,
                        "output_type": "BAM",
                        "comment": [
                            "comment-string-value-1",
                            "comment-string-value-2"
                        ],
                        "sorting_collections_size_ratio": 2.163350957715713
                    }
                },
                "sbg:revision": 3,
                "y": 250.16671231058046,
                "sbg:toolAuthor": "Broad Institute",
                "sbg:createdBy": "bix-demo",
                "inputs": [
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            {
                                "name": "verbosity",
                                "type": "enum",
                                "symbols": [
                                    "ERROR",
                                    "WARNING",
                                    "INFO",
                                    "DEBUG"
                                ]
                            }
                        ],
                        "id": "#verbosity",
                        "sbg:toolDefaultValue": "INFO",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 6,
                            "prefix": "VERBOSITY="
                        },
                        "label": "Verbosity",
                        "description": "Control verbosity of logging. Default value: INFO. This option can be set to 'null' to clear the default value. Possible values: {ERROR, WARNING, INFO, DEBUG}.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            {
                                "name": "validation_stringency",
                                "type": "enum",
                                "symbols": [
                                    "STRICT",
                                    "LENIENT",
                                    "SILENT"
                                ]
                            }
                        ],
                        "id": "#validation_stringency",
                        "sbg:toolDefaultValue": "SILENT",
                        "inputBinding": {
                            "valueFrom": {
                                "script": "{\n  if ($job.inputs.validation_stringency)\n  {\n    return $job.inputs.validation_stringency\n  }\n  else\n  {\n    return \"SILENT\"\n  }\n}",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 4,
                            "prefix": "VALIDATION_STRINGENCY="
                        },
                        "label": "Validation stringency",
                        "description": "Validation stringency for all SAM files read by this program. Setting stringency to SILENT can improve performance when processing a BAM file in which variable-length data (read, qualities, tags) do not otherwise need to be decoded. Default value: STRICT. This option can be set to 'null' to clear the default value. Possible values: {STRICT, LENIENT, SILENT}.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "float"
                        ],
                        "sbg:stageInput": null,
                        "sbg:toolDefaultValue": "0.25",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 9,
                            "prefix": "SORTING_COLLECTION_SIZE_RATIO="
                        },
                        "label": "Sorting collections size ratio",
                        "required": false,
                        "description": "This number, plus the maximum RAM available to the JVM, determine the memory footprint used by some of the sorting collections. If you are running out of memory, try reducing this number.",
                        "id": "#sorting_collections_size_ratio"
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            {
                                "name": "remove_duplicates",
                                "type": "enum",
                                "symbols": [
                                    "true",
                                    "false"
                                ]
                            }
                        ],
                        "id": "#remove_duplicates",
                        "sbg:toolDefaultValue": "false",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 3,
                            "prefix": "REMOVE_DUPLICATES="
                        },
                        "label": "Remove duplicates",
                        "description": "If this parameter is set to true, duplicates will not be written to the output file. If set to false, duplicates will be written with the appropriate flags set. Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false}.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "string"
                        ],
                        "id": "#read_name_regex",
                        "sbg:toolDefaultValue": "[a-zA-Z0-9]+:[0-9]:([0-9]+):([0-9]+):([0-9]+).*",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 9,
                            "prefix": "READ_NAME_REGEX="
                        },
                        "label": "Read name regex",
                        "description": "Regular expression that can be used to parse read names in the incoming SAM file. Read names are parsed to extract three variables: tile/region, x coordinate and y coordinate. These values are used to estimate the rate of optical duplication in order to give a more accurate estimated library size. Set this option to null to disable optical duplicate detection. The regular expression should contain three capture groups for the three variables, in order. It must match the entire read name. Note that if the default regex is specified, a regex match is not actually done, but instead the read name is split on colon character. For 5 element names, the 3rd, 4th and 5th elements are assumed to be tile, x and y values, respectively. For 7 element names (CASAVA 1.8), the 5th, 6th, and 7th elements are assumed to be tile, x and y values, respectively. Default value: [a-zA-Z0-9]+:[0-9]:([0-9]+):([0-9]+):([0-9]+).*. This option can be set to 'null' to clear the default value.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            {
                                "name": "quiet",
                                "type": "enum",
                                "symbols": [
                                    "True",
                                    "False"
                                ]
                            }
                        ],
                        "id": "#quiet",
                        "sbg:toolDefaultValue": "False",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 4,
                            "prefix": "QUIET="
                        },
                        "label": "Quiet",
                        "description": "This parameter indicates whether to suppress job-summary info on System.err. Possible values: {True, False}.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "string"
                        ],
                        "id": "#program_record",
                        "sbg:toolDefaultValue": "MarkDuplicates",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 10,
                            "prefix": "PROGRAM_RECORD_ID="
                        },
                        "label": "Program record ID",
                        "sbg:altPrefix": "PG",
                        "description": "The program record ID for the @PG record(s) created by this program. Set to null to disable PG record creation. This string may have a suffix appended to avoid collision with other program record IDs. Default value: MarkDuplicates. This option can be set to 'null' to clear the default value.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "string"
                        ],
                        "id": "#program_group_ver",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 9,
                            "prefix": "PROGRAM_GROUP_VERSION="
                        },
                        "label": "Program group version",
                        "sbg:altPrefix": "PG_VERSION=",
                        "description": "Value of version number (VN) tag of program group (PG) record to be created. If not specified, the version will be detected automatically. Default value: null.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "string"
                        ],
                        "id": "#program_group_name",
                        "sbg:toolDefaultValue": "MarkDuplicates",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 9,
                            "prefix": "PROGRAM_GROUP_NAME="
                        },
                        "label": "Program group name",
                        "sbg:altPrefix": "PG_NAME=",
                        "description": "Value of program name (PN) tag of program group (PG) record to be created. Default value: MarkDuplicates. This option can be set to 'null' to clear the default value.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "string"
                        ],
                        "id": "#program_group_command_line",
                        "sbg:toolDefaultValue": null,
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 9,
                            "prefix": "PROGRAM_GROUP_COMMAND_LINE="
                        },
                        "label": "Program group command line",
                        "sbg:altPrefix": "PG_COMMAND=",
                        "description": "Value of command line (CL) tag of program group (PG) record to be created. If not supplied the command line will be detected automatically. Default value: null.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            {
                                "name": "output_type",
                                "type": "enum",
                                "symbols": [
                                    "BAM",
                                    "SAM",
                                    "SAME AS INPUT"
                                ]
                            }
                        ],
                        "id": "#output_type",
                        "sbg:toolDefaultValue": "SAME AS INPUT",
                        "label": "Output format",
                        "description": "Since Picard tools can output both SAM and BAM files, user can choose the format of the output file.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "int"
                        ],
                        "id": "#optical_duplicate_pixel_distance",
                        "sbg:toolDefaultValue": "100",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 9,
                            "prefix": "OPTICAL_DUPLICATE_PIXEL_DISTANCE="
                        },
                        "label": "Optical duplicate pixel distance",
                        "description": "The maximum offset between two duplicate clusters in order to consider them optical duplicates. This should usually be set to some fairly small number (e.g. 5-10 pixels) unless using later versions of the Illumina pipeline that multiply pixel values by 10, in which case 50-100 is more normal. Default value: 100. This option can be set to 'null' to clear the default value.",
                        "required": false
                    },
                    {
                        "sbg:category": "Execution options",
                        "type": [
                            "null",
                            "int"
                        ],
                        "id": "#memory_per_job",
                        "sbg:toolDefaultValue": "2048",
                        "label": "Memory per job",
                        "description": "Amount of RAM memory to be used per job. Defaults to 2048MB for single threaded jobs.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "int"
                        ],
                        "id": "#max_sequences_for_disk_read_ends_map",
                        "sbg:toolDefaultValue": "50000",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 9,
                            "prefix": "MAX_SEQUENCES_FOR_DISK_READ_ENDS_MAP="
                        },
                        "label": "Max sequences for disk read ends map",
                        "sbg:altPrefix": "MAX_SEQS=",
                        "description": "This option is obsolete. ReadEnds will always be spilled to disk. Default value: 50000. This option can be set to 'null' to clear the default value.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "int"
                        ],
                        "id": "#max_records_in_ram",
                        "sbg:toolDefaultValue": "500000",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 4,
                            "prefix": "MAX_RECORDS_IN_RAM="
                        },
                        "label": "Max records in RAM",
                        "description": "When writing SAM files that need to be sorted, this parameter will specify the number of records stored in RAM before spilling to disk. Increasing this number reduces the number of file handles needed to sort a SAM file, and increases the amount of RAM needed. Default value: 500000. This option can be set to 'null' to clear the default value.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "int"
                        ],
                        "id": "#max_file_handles_for_read_ends_map",
                        "sbg:toolDefaultValue": "8000",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 9,
                            "prefix": "MAX_FILE_HANDLES_FOR_READ_ENDS_MAP="
                        },
                        "label": "Max file handles for read ends map",
                        "sbg:altPrefix": "MAX_FILE_HANDLES=",
                        "description": "Maximum number of file handles to keep open when spilling read ends to disk. Set this number a little lower than the per-process maximum number of file that may be open. This number can be found by executing the 'ulimit -n' command on a Unix system. Default value: 8000. This option can be set to 'null' to clear the default value.",
                        "required": false
                    },
                    {
                        "sbg:category": "File inputs",
                        "type": [
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "id": "#input_bam",
                        "inputBinding": {
                            "itemSeparator": null,
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "INPUT="
                        },
                        "label": "Input bam",
                        "sbg:altPrefix": "I",
                        "description": "This parameter indicates one or more input SAM or BAM files to analyze. Must be coordinate sorted.  Default value: null. This option may be specified 0 or more times.",
                        "sbg:fileTypes": "SAM, BAM",
                        "required": true
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            {
                                "name": "duplicate_scoring_strategy",
                                "type": "enum",
                                "symbols": [
                                    "SUM_OF_BASE_QUALITIES",
                                    "TOTAL_MAPPED_REFERENCE_LENGTH"
                                ]
                            }
                        ],
                        "id": "#duplicate_scoring_strategy",
                        "sbg:toolDefaultValue": "SUM_OF_BASE_QUALITIES",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "DUPLICATE_SCORING_STRATEGY="
                        },
                        "label": "Duplicate scoring strategy",
                        "sbg:altPrefix": "DS",
                        "description": "This parameter indicates the scoring strategy for choosing the non-duplicate among candidates.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            {
                                "name": "create_index",
                                "type": "enum",
                                "symbols": [
                                    "True",
                                    "False"
                                ]
                            }
                        ],
                        "id": "#create_index",
                        "sbg:toolDefaultValue": "False",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 5,
                            "prefix": "CREATE_INDEX="
                        },
                        "label": "Create index",
                        "description": "This parameter indicates whether to create a BAM index when writing a coordinate-sorted BAM file. Default value: False. This option can be set to 'null' to clear the default value. Possible values: {True, False}.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "int"
                        ],
                        "id": "#compression_level",
                        "sbg:toolDefaultValue": "5",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 4,
                            "prefix": "COMPRESSION_LEVEL="
                        },
                        "label": "Compression level",
                        "description": "Compression level for all compressed files created (e.g. BAM and GELI). Default value: 5. This option can be set to 'null' to clear the default value.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            {
                                "name": "comment",
                                "type": "array",
                                "items": "string"
                            }
                        ],
                        "sbg:stageInput": null,
                        "id": "#comment",
                        "inputBinding": {
                            "itemSeparator": null,
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "position": 9,
                            "prefix": "COMMENT="
                        },
                        "label": "Comment",
                        "sbg:altPrefix": "CO",
                        "description": "Comment(s) to include in the output file's header. Default value: null. This option may be specified 0 or more times.",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            {
                                "name": "assume_sorted",
                                "type": "enum",
                                "symbols": [
                                    "true",
                                    "false"
                                ]
                            }
                        ],
                        "id": "#assume_sorted",
                        "sbg:toolDefaultValue": "false",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "ASSUME_SORTED="
                        },
                        "label": "Assume sorted",
                        "sbg:altPrefix": "AS",
                        "description": "If this parameter is set to true, it is assumed that the input file is coordinate sorted even if the header says otherwise.  Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false}.",
                        "required": false
                    }
                ],
                "sbg:modifiedBy": "mladenlSBG",
                "sbg:cmdPreview": "java -Xmx2048M -jar /opt/picard-tools-1.140/picard.jar MarkDuplicates INPUT=input_bam.ext METRICS_FILE=input_bam.metrics OUTPUT=input_bam.deduped.bam",
                "stdout": "",
                "appUrl": "/u/bix-demo/picard-1-140-demo/apps/#bix-demo/picard-1-140-demo/picard-markduplicates-1-140/3",
                "outputs": [
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#metrics_file",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_bam",
                            "sbg:metadata": {
                                "__inherit__": "input_bam"
                            },
                            "glob": "*.metrics"
                        },
                        "label": "Metrics file",
                        "sbg:fileTypes": "METRICS",
                        "description": "File to which the duplication metrics will be written."
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#deduped_bam",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_bam",
                            "sbg:metadata": {
                                "__inherit__": "input_bam"
                            },
                            "secondaryFiles": [
                                "^.bai",
                                ".bai"
                            ],
                            "glob": "*.deduped.?am"
                        },
                        "label": "Deduped BAM",
                        "sbg:fileTypes": "BAM, SAM",
                        "description": "The output file to which marked records will be written."
                    }
                ],
                "sbg:sbgMaintained": false,
                "baseCommand": [
                    "java",
                    {
                        "script": "{   \n  if($job.inputs.memory_per_job){\n    return '-Xmx'.concat($job.inputs.memory_per_job, 'M')\n  }   \n  \treturn '-Xmx2048M'\n}",
                        "class": "Expression",
                        "engine": "#cwl-js-engine"
                    },
                    "-jar",
                    "/opt/picard-tools-1.140/picard.jar",
                    "MarkDuplicates"
                ],
                "cwlVersion": "sbg:draft-2",
                "hints": [
                    {
                        "dockerPull": "images.sbgenomics.com/mladenlsbg/picard:1.140",
                        "dockerImageId": "eab0e70b6629",
                        "class": "DockerRequirement"
                    },
                    {
                        "value": 1,
                        "class": "sbg:CPURequirement"
                    },
                    {
                        "value": {
                            "script": "{\n  if($job.inputs.memory_per_job){\n  \treturn $job.inputs.memory_per_job\n  }\n  \treturn 2048\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "class": "sbg:MemRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1450911260
            },
            "id": "#Picard_MarkDuplicates_1",
            "sbg:y": 250.16671231058046,
            "sbg:x": 760.8334245681787,
            "outputs": [
                {
                    "id": "#Picard_MarkDuplicates_1.metrics_file"
                },
                {
                    "id": "#Picard_MarkDuplicates_1.deduped_bam"
                }
            ],
            "scatter": "#Picard_MarkDuplicates_1.input_bam"
        },
        {
            "inputs": [
                {
                    "id": "#SAMtools_Index_BAM.output_indexed_data"
                },
                {
                    "id": "#SAMtools_Index_BAM.input_index_file"
                },
                {
                    "source": [
                        "#SBG_Filter_ChIP_seq_BAM_1.aligned_reads"
                    ],
                    "id": "#SAMtools_Index_BAM.input_bam_file"
                }
            ],
            "run": {
                "sbg:modifiedOn": 1467298447,
                "id": "bix-demo/samtools-0-1-19-demo/samtools-index-0-1-19/6",
                "sbg:links": [
                    {
                        "label": "Homepage",
                        "id": "http://samtools.sourceforge.net/"
                    },
                    {
                        "label": "Source Code",
                        "id": "https://github.com/samtools/samtools"
                    },
                    {
                        "label": "Wiki",
                        "id": "http://sourceforge.net/p/samtools/wiki/Home/"
                    },
                    {
                        "label": "Download",
                        "id": "http://sourceforge.net/projects/samtools/files/"
                    },
                    {
                        "label": "Publication",
                        "id": "http://www.ncbi.nlm.nih.gov/pubmed/19505943"
                    },
                    {
                        "label": "Documentation",
                        "id": "http://www.htslib.org/doc/samtools.html"
                    }
                ],
                "sbg:project": "bix-demo/samtools-0-1-19-demo",
                "sbg:toolkit": "SAMtools",
                "label": "SAMtools Index BAM",
                "sbg:sbgMaintained": false,
                "sbg:toolkitVersion": "v0.1.19",
                "x": 1092.6669541597462,
                "successCodes": [],
                "sbg:id": "admin/sbg-public-data/samtools-index-0-1-19/6",
                "sbg:contributors": [
                    "markop",
                    "marouf",
                    "bix-demo"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "SAMtools 0.1.19 - Demo New",
                "description": "SAMtools Index BAM indexes sorted alignments for fast random access. Index file <aln.bam>.bai is created.",
                "sbg:image_url": null,
                "sbg:latestRevision": 6,
                "requirements": [
                    {
                        "requirements": [
                            {
                                "dockerPull": "rabix/js-engine",
                                "class": "DockerRequirement"
                            }
                        ],
                        "class": "ExpressionEngineRequirement",
                        "id": "#cwl-js-engine"
                    }
                ],
                "arguments": [],
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "SAM/BAM-Processing",
                    "Indexing"
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1450911268,
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1450911269,
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1450911269,
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revision": 2,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1455727039,
                        "sbg:modifiedBy": "markop",
                        "sbg:revision": 3,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1456328520,
                        "sbg:modifiedBy": "markop",
                        "sbg:revision": 4,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1459178013,
                        "sbg:modifiedBy": "markop",
                        "sbg:revision": 5,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1467298447,
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 6,
                        "sbg:revisionNotes": null
                    }
                ],
                "sbg:license": "The MIT License",
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 1000,
                        "cpu": 1
                    },
                    "inputs": {
                        "output_indexed_data": false,
                        "input_bam_file": {
                            "size": 0,
                            "secondaryFiles": [],
                            "path": "input.bam",
                            "class": "File"
                        }
                    }
                },
                "sbg:revision": 6,
                "y": 552.5000830888772,
                "sbg:toolAuthor": "Heng Li, Sanger Institute",
                "sbg:createdBy": "bix-demo",
                "inputs": [
                    {
                        "sbg:category": "Configuration",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:stageInput": null,
                        "id": "#output_indexed_data",
                        "required": false,
                        "label": "output indexed data file",
                        "description": "Don't output indexed data file. The default value is [FALSE]."
                    },
                    {
                        "sbg:category": "File input",
                        "type": [
                            "null",
                            "File"
                        ],
                        "sbg:stageInput": "link",
                        "id": "#input_index_file",
                        "required": false,
                        "label": "Input indexed file",
                        "description": "Input index file (BAI).",
                        "sbg:fileTypes": "BAI"
                    },
                    {
                        "type": [
                            "File"
                        ],
                        "sbg:stageInput": "link",
                        "id": "#input_bam_file",
                        "required": true,
                        "label": "BAM input file",
                        "description": "BAM input file.",
                        "sbg:fileTypes": "BAM"
                    }
                ],
                "sbg:modifiedBy": "marouf",
                "sbg:cmdPreview": "/opt/samtools-0.1.19/samtools index input.bam",
                "stdout": "",
                "appUrl": "/u/bix-demo/samtools-0-1-19-demo/apps/#bix-demo/samtools-0-1-19-demo/samtools-index-0-1-19/6",
                "outputs": [
                    {
                        "type": [
                            "File"
                        ],
                        "id": "#output_bam_file",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_bam_file",
                            "glob": {
                                "script": "{\n  if ($job.inputs.output_indexed_data === true)\n  {\n    return $job.inputs.input_bam_file.path.split(\"/\").pop()\n  } \n  else \n    \n  {\n    return ''\n\n  }\n}\n\n\n\n\n\n\n",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            },
                            "secondaryFiles": [
                                ".bai"
                            ]
                        },
                        "label": "Output BAM file",
                        "sbg:fileTypes": "BAM",
                        "description": "Output BAM file with index (BAI) file."
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#generated_index",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_bam_file",
                            "glob": "*.bai"
                        },
                        "label": "Generated index file",
                        "sbg:fileTypes": "BAI",
                        "description": "Generated index file (without the indexed data)."
                    }
                ],
                "stdin": "",
                "baseCommand": [
                    {
                        "script": "{\n  if ($job.inputs.input_index_file)\n  {\n \treturn\"echo Skipping index step because BAI file is provided on the input.\"\n  }\n  else\n  {\n    return \"/opt/samtools-0.1.19/samtools index \" + $job.inputs.input_bam_file.path\n  }\n}\n",
                        "class": "Expression",
                        "engine": "#cwl-js-engine"
                    }
                ],
                "cwlVersion": "sbg:draft-2",
                "hints": [
                    {
                        "dockerPull": "images.sbgenomics.com/markop/samtools:0.1.19",
                        "dockerImageId": "2fb927277493",
                        "class": "DockerRequirement"
                    },
                    {
                        "value": 1,
                        "class": "sbg:CPURequirement"
                    },
                    {
                        "value": 1000,
                        "class": "sbg:MemRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1450911268
            },
            "id": "#SAMtools_Index_BAM",
            "sbg:y": 552.5000830888772,
            "sbg:x": 1092.6669541597462,
            "outputs": [
                {
                    "id": "#SAMtools_Index_BAM.output_bam_file"
                },
                {
                    "id": "#SAMtools_Index_BAM.generated_index"
                }
            ],
            "scatter": "#SAMtools_Index_BAM.input_bam_file"
        },
        {
            "inputs": [
                {
                    "source": [
                        "#sample_files"
                    ],
                    "id": "#SBG_Select_Control.sample_files"
                },
                {
                    "source": [
                        "#control_files"
                    ],
                    "id": "#SBG_Select_Control.control_files"
                }
            ],
            "sbg:x": 117.50002498096968,
            "id": "#SBG_Select_Control",
            "sbg:y": 375.33340885242063,
            "run": {
                "sbg:sbgMaintained": false,
                "id": "bix-demo/sbgtools-demo/sbg-select-control/3",
                "sbg:project": "bix-demo/sbgtools-demo",
                "outputs": [
                    {
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "id": "#output_control_files",
                        "outputBinding": {
                            "glob": "*.*"
                        },
                        "label": "Appropriate control files",
                        "sbg:fileTypes": "FQ, FQ.GZ, FASTQ, FASTQ.GZ",
                        "description": "Appropriate control files defined by case_id metadata"
                    }
                ],
                "label": "SBG Select Control",
                "arguments": [],
                "x": 117.50002498096968,
                "sbg:revisionNotes": "call rm if there is something to remove",
                "successCodes": [],
                "sbg:id": "admin/sbg-public-data/sbg-select-control/3",
                "sbg:contributors": [
                    "nemanja.vucic",
                    "milos_jordanski"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "SBGTools - Demo New",
                "description": "",
                "stdout": "",
                "sbg:latestRevision": 3,
                "requirements": [
                    {
                        "class": "CreateFileRequirement",
                        "fileDef": [
                            {
                                "filename": "select_control.py",
                                "fileContent": "import json\nimport subprocess\nimport sys\n\njob = \"\"\nwith open('job.json') as data_file:\n    job = json.load(data_file)\n\nif \"inputs\" in job and \"sample_files\" in job[\"inputs\"]:\n    sample_files = job[\"inputs\"][\"sample_files\"]\nelse:\n    sample_files = []\n\nif (not isinstance(sample_files, list)):\n    sample_files = [sample_files]\n\ncase_id = \"\"\nfor f in sample_files:\n    if \"metadata\" in f and \"case_id\" in f[\"metadata\"]:\n        case_id = f[\"metadata\"][\"case_id\"]\n        break\n\nprint case_id\n\nif \"inputs\" in job and \"control_files\" in job[\"inputs\"]:\n    control_files = job[\"inputs\"][\"control_files\"]\nelse:\n    control_files = []\n\noutput_control = []\nto_delete_control = []\nfor f in control_files:\n    if \"metadata\" in f and \"case_id\" in f[\"metadata\"] and f[\"metadata\"][\"case_id\"] == case_id:\n        output_control.append(f)\n    else:\n        to_delete_control.append(f)\n\ncommand = \"rm\"\nfor f in to_delete_control:\n    command = command + \" \" + f[\"path\"]\n\nif to_delete_control:\n    sys.stderr.write(command)\n    subprocess.check_call(command, shell=True)\n\noutput_files = []\nfor f in output_control:\n    d = {'class': 'File'}\n    d['path'] = f[\"path\"]\n\n    if \"size\" in f:\n        d['size'] = f[\"size\"]\n\n    if \"contents\" in f:\n        d['contents'] = f[\"contents\"]\n\n    if \"name\" in f:\n        d['name'] = f[\"name\"]\n\n    if \"checksum\" in f:\n        d['checksum'] = f[\"checksum\"]\n\n    if \"location\" in f:\n        d['location'] = f[\"location\"]\n\n    if \"metadata\" in f:\n        d['metadata'] = f[\"metadata\"]\n\n    if \"secondaryFiles\" in f:\n        d['secondaryFiles'] = f[\"secondaryFiles\"]\n\n    output_files.append(d)\n\ndata = {}\nif output_files:\n    data['output_control_files'] = output_files\n\nwith open('cwl.output.json', 'w') as w:\n    json.dump(data, w)"
                            }
                        ]
                    }
                ],
                "sbg:modifiedOn": 1499852353,
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499419615,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499419635,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499809588,
                        "sbg:modifiedBy": "milos_jordanski",
                        "sbg:revision": 2,
                        "sbg:revisionNotes": "remove unnecessary control files"
                    },
                    {
                        "sbg:modifiedOn": 1499852353,
                        "sbg:modifiedBy": "milos_jordanski",
                        "sbg:revision": 3,
                        "sbg:revisionNotes": "call rm if there is something to remove"
                    }
                ],
                "baseCommand": [
                    "python",
                    "select_control.py"
                ],
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 1000,
                        "cpu": 1
                    },
                    "inputs": {
                        "control_files": [
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/control_files-1.ext",
                                "class": "File"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/control_files-2.ext",
                                "class": "File"
                            }
                        ],
                        "sample_files": [
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/sample_files-1.ext",
                                "class": "File"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/sample_files-2.ext",
                                "class": "File"
                            }
                        ]
                    }
                },
                "sbg:revision": 3,
                "y": 375.33340885242063,
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "sbg:category": "Input",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "sbg:stageInput": "copy",
                        "id": "#sample_files",
                        "required": false,
                        "label": "Input sample files",
                        "description": "Input sample files.",
                        "sbg:fileTypes": "FQ, FQ.GZ, FASTQ, FASTQ.GZ"
                    },
                    {
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "sbg:stageInput": "copy",
                        "id": "#control_files",
                        "required": false,
                        "label": "Input control files",
                        "description": "Input control files."
                    }
                ],
                "sbg:modifiedBy": "milos_jordanski",
                "sbg:cmdPreview": "python select_control.py",
                "sbg:image_url": null,
                "appUrl": "/u/bix-demo/sbgtools-demo/apps/#bix-demo/sbgtools-demo/sbg-select-control/3",
                "stdin": "",
                "cwlVersion": "sbg:draft-2",
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
                        "dockerPull": "images.sbgenomics.com/milos_jordanski/python:2.7",
                        "dockerImageId": "",
                        "class": "DockerRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499419615
            },
            "outputs": [
                {
                    "id": "#SBG_Select_Control.output_control_files"
                }
            ]
        },
        {
            "inputs": [
                {
                    "id": "#ChIP_seq_FastQC_1.threads"
                },
                {
                    "id": "#ChIP_seq_FastQC_1.quiet"
                },
                {
                    "id": "#ChIP_seq_FastQC_1.nogroup"
                },
                {
                    "id": "#ChIP_seq_FastQC_1.nano"
                },
                {
                    "id": "#ChIP_seq_FastQC_1.limits_file"
                },
                {
                    "id": "#ChIP_seq_FastQC_1.kmers"
                },
                {
                    "source": [
                        "#SBG_Scatter_Prepare.grouped_files"
                    ],
                    "id": "#ChIP_seq_FastQC_1.input_fastq"
                },
                {
                    "id": "#ChIP_seq_FastQC_1.format"
                },
                {
                    "id": "#ChIP_seq_FastQC_1.contaminants_file"
                },
                {
                    "id": "#ChIP_seq_FastQC_1.casava"
                },
                {
                    "id": "#ChIP_seq_FastQC_1.adapters_file"
                }
            ],
            "run": {
                "sbg:modifiedOn": 1499850960,
                "id": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo/chip-seq-fastqc/1",
                "sbg:links": [
                    {
                        "label": "Homepage",
                        "id": "http://www.bioinformatics.babraham.ac.uk/projects/fastqc/"
                    },
                    {
                        "label": "Source Code",
                        "id": "http://www.bioinformatics.babraham.ac.uk/projects/fastqc/fastqc_v0.11.4_source.zip"
                    },
                    {
                        "label": "Wiki",
                        "id": "https://wiki.hpcc.msu.edu/display/Bioinfo/FastQC+Tutorial"
                    },
                    {
                        "label": "Download",
                        "id": "http://www.bioinformatics.babraham.ac.uk/projects/fastqc/fastqc_v0.11.4.zip"
                    },
                    {
                        "label": "Publication",
                        "id": "http://www.bioinformatics.babraham.ac.uk/projects/fastqc"
                    }
                ],
                "sbg:project": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo",
                "sbg:toolkit": "FastQC",
                "label": "ChIP-seq FastQC",
                "sbg:sbgMaintained": false,
                "sbg:toolkitVersion": "0.11.4",
                "x": 607.5000848770165,
                "successCodes": [],
                "sbg:id": "admin/sbg-public-data/chip-seq-fastqc/1",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "ChIP-seq BWA Alignment and Peak Calling Demo",
                "description": "FastQC reads a set of sequence files and produces a quality control (QC) report from each one. These reports consist of a number of different modules, each of which will help identify a different type of potential problem in your data. \n\nSince it's necessary to convert the tool report in order to show them on Seven Bridges platform, it's recommended to use [FastQC Analysis workflow instead](https://igor.sbgenomics.com/public/apps#admin/sbg-public-data/fastqc-analysis/). \n\nFastQC is a tool which takes a FASTQ file and runs a series of tests on it to generate a comprehensive QC report.  This report will tell you if there is anything unusual about your sequence.  Each test is flagged as a pass, warning, or fail depending on how far it departs from what you would expect from a normal large dataset with no significant biases.  It is important to stress that warnings or even failures do not necessarily mean that there is a problem with your data, only that it is unusual.  It is possible that the biological nature of your sample means that you would expect this particular bias in your results.\n\n### Common Issues:\n\nOutput of the tool is ZIP archive. In order to view report on Seven Bridges platform, you can use SBG Html2b64 tool. It is advised to scatter SBG Html2b64 so it would be able to process an array of files. The example can be seen in [FastQC Analysis workflow](https://igor.sbgenomics.com/public/apps#admin/sbg-public-data/fastqc-analysis/) which you can also use instead of this tool.",
                "sbg:image_url": null,
                "sbg:latestRevision": 1,
                "requirements": [
                    {
                        "requirements": [
                            {
                                "dockerPull": "rabix/js-engine",
                                "class": "DockerRequirement"
                            }
                        ],
                        "class": "ExpressionEngineRequirement",
                        "id": "#cwl-js-engine"
                    }
                ],
                "arguments": [
                    {
                        "valueFrom": "--noextract",
                        "separate": true,
                        "prefix": ""
                    },
                    {
                        "valueFrom": ".",
                        "separate": true,
                        "prefix": "--outdir"
                    }
                ],
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "FASTQ-Processing",
                    "Quality-Control",
                    "Quantification"
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499850938,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499850960,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    }
                ],
                "sbg:license": "GNU General Public License v3.0 only",
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 2524,
                        "cpu": 5
                    },
                    "inputs": {
                        "threads": 5,
                        "format": null,
                        "quiet": true,
                        "input_fastq": [
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_fastq-1.fastq",
                                "class": "File"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_fastq-2.fastq",
                                "class": "File"
                            }
                        ]
                    }
                },
                "sbg:revision": 1,
                "y": 365.83336804972834,
                "sbg:toolAuthor": "Babraham Institute",
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:toolDefaultValue": "1",
                        "inputBinding": {
                            "valueFrom": {
                                "script": "{\n//if \"threads\" is not specified\n//number of threads is determined based on number of inputs\n  \nif ($job.inputs.threads)\n    {\n      return $job.inputs.threads;\n    }\n  else if ($job.inputs.input_fastq)\n  {\n    fastq = [].concat($job.inputs.input_fastq.length);\n    //safety\n    if (fastq.length > 7)\n    {\n      return 7;\n    }\n    else \n    {\n      return fastq.length\n    }\n  }\n  else\n  {\n    return 1\n  }\n}",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "prefix": "--threads"
                        },
                        "label": "Threads",
                        "sbg:altPrefix": "-t",
                        "description": "Specifies the number of files which can be processed simultaneously.  Each thread will be allocated 250MB of memory so you shouldn't run more threads than your available memory will cope with, and not more than 6 threads on a 32 bit machine.",
                        "id": "#threads"
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#quiet",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "prefix": "--quiet"
                        },
                        "label": "Quiet",
                        "sbg:altPrefix": "-q",
                        "description": "Supress all progress messages on stdout and only report errors."
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#nogroup",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "--nogroup"
                        },
                        "label": "Nogroup",
                        "description": "Disable grouping of bases for reads >50bp. All reports will show data for every base in the read.  WARNING: Using this option will cause fastqc to crash and burn if you use it on really long reads, and your plots may end up a ridiculous size. You have been warned."
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#nano",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "--nano"
                        },
                        "label": "Nano",
                        "description": "Files come from naopore sequences and are in fast5 format. In this mode you can pass in directories to process and the program will take in all fast5 files within those directories and produce a single output file from the sequences found in all files."
                    },
                    {
                        "sbg:category": "File inputs",
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#limits_file",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "prefix": "--limits"
                        },
                        "label": "Limits",
                        "sbg:altPrefix": "-l",
                        "description": "Specifies a non-default file which contains a set of criteria which will be used to determine the warn/error limits for the various modules.  This file can also be used to selectively remove some modules from the output all together.  The format needs to mirror the default limits.txt file found in the Configuration folder.",
                        "sbg:fileTypes": "TXT",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:toolDefaultValue": "7",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "prefix": "--kmers"
                        },
                        "label": "Kmers",
                        "sbg:altPrefix": "-f",
                        "description": "Specifies the length of Kmer to look for in the Kmer content module. Specified Kmer length must be between 2 and 10. Default length is 7 if not specified.",
                        "id": "#kmers"
                    },
                    {
                        "sbg:category": "File inputs",
                        "type": [
                            {
                                "name": "input_fastq",
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "id": "#input_fastq",
                        "inputBinding": {
                            "itemSeparator": null,
                            "sbg:cmdInclude": true,
                            "position": 100,
                            "separate": true
                        },
                        "label": "Input file",
                        "description": "Input file.",
                        "sbg:fileTypes": "FASTQ, FQ, FASTQ.GZ, FQ.GZ, BAM, SAM",
                        "required": true
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            {
                                "name": "format",
                                "type": "enum",
                                "symbols": [
                                    "bam",
                                    "sam",
                                    "bam_mapped",
                                    "sam_mapped",
                                    "fastq"
                                ]
                            }
                        ],
                        "sbg:toolDefaultValue": "FASTQ",
                        "inputBinding": {
                            "valueFrom": {
                                "script": "{\n  if ($job.inputs.format)\n  {\n    return $job.inputs.format\n  }\n  else if ($job.inputs.format==undefined && $job.inputs.input_fastq)\n  {\n    fastq = [].concat($job.inputs.input_fastq);\n    filename = fastq[0].path.split('/').slice(-1)[0]\n    ext = filename.split('.').slice(-1)[0]\n    ext =  ext.toLowerCase()\n    \n    \n    if (ext == 'bam')\n    {return 'bam'}\n    else if (ext == 'sam')\n    {return 'sam'}\n  }\n  \n}",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "prefix": "--format"
                        },
                        "label": "Format",
                        "sbg:altPrefix": "-f",
                        "description": "Bypasses the normal sequence file format detection and forces the program to use the specified format.  Valid formats are BAM, SAM, BAM_mapped, SAM_mapped and FASTQ.",
                        "id": "#format"
                    },
                    {
                        "sbg:category": "File inputs",
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#contaminants_file",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "prefix": "--contaminants"
                        },
                        "label": "Contaminants",
                        "sbg:altPrefix": "-c",
                        "description": "Specifies a non-default file which contains the list of contaminants to screen overrepresented sequences against. The file must contain sets of named contaminants in the form name[tab]sequence.  Lines prefixed with a hash will be ignored.",
                        "sbg:fileTypes": "TXT",
                        "required": false
                    },
                    {
                        "sbg:category": "Options",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#casava",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "--casava"
                        },
                        "label": "Casava",
                        "description": "Files come from raw casava output. Files in the same sample group (differing only by the group number) will be analysed as a set rather than individually. Sequences with the filter flag set in the header will be excluded from the analysis. Files must have the same names given to them by casava (including being gzipped and ending with .gz) otherwise they won't be grouped together correctly."
                    },
                    {
                        "sbg:category": "File inputs",
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#adapters_file",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "prefix": "--adapters"
                        },
                        "label": "Adapters",
                        "sbg:altPrefix": "-a",
                        "description": "Specifies a non-default file which contains the list of adapter sequences which will be explicity searched against the library. The file must contain sets of named adapters in the form name[tab]sequence.  Lines prefixed with a hash will be ignored.",
                        "sbg:fileTypes": "TXT",
                        "required": false
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:cmdPreview": "fastqc  --noextract --outdir .  /path/to/input_fastq-1.fastq  /path/to/input_fastq-2.fastq",
                "stdout": "",
                "outputs": [
                    {
                        "type": [
                            "null",
                            {
                                "name": "report_zip",
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "id": "#report_zip",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_fastq",
                            "sbg:metadata": {
                                "__inherit__": "input_fastq"
                            },
                            "glob": "*_fastqc.zip"
                        },
                        "label": "Report zip",
                        "sbg:fileTypes": "ZIP",
                        "description": "Zip archive of the report."
                    }
                ],
                "stdin": "",
                "baseCommand": [
                    "fastqc"
                ],
                "cwlVersion": "sbg:draft-2",
                "hints": [
                    {
                        "dockerPull": "images.sbgenomics.com/mladenlsbg/fastqc:0.11.4",
                        "dockerImageId": "759c4c8fbafd",
                        "class": "DockerRequirement"
                    },
                    {
                        "value": {
                            "script": "{\n//we are allocating CPU's based on number of threads, but if \"threads\" is not specified\n//number of threads is determined based on number of inputs\n//and we also determine CPU based on number of inputs\nif ($job.inputs.threads)\n    {\n      return $job.inputs.threads;\n    }\n  else if ($job.inputs.input_fastq)\n  {\n    fastq = [].concat($job.inputs.input_fastq);\n    //safety \n    if (fastq.length > 7)\n    {\n      return 7;\n    }\n    else \n    {\n      return fastq.length;\n    }\n  }\n  else\n  {\n    return 1\n  }\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "class": "sbg:CPURequirement"
                    },
                    {
                        "value": {
                            "script": "{\n//we are allocating memory based on number of threads\n//if threads are not specified number of threads is determined based on number of inputs\n//for each thread FastQC uses 250 mb but we've added a bit more (300mb) because of the overhead\n\nif ($job.inputs.threads)\n    {\n      return (1024 + 300*$job.inputs.threads);\n    }\n  else if ($job.inputs.input_fastq)\n  {\n    fastq = [].concat($job.inputs.input_fastq);\n    //safety\n    if (fastq.length > 7)\n    {\n      return (1024 + 300*7);\n    }\n    else \n    {\n      return (1024 + 300*fastq.length);\n    }\n  }\n  else\n  {\n    return 1\n  }\n}\n\n",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "class": "sbg:MemRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499850938
            },
            "id": "#ChIP_seq_FastQC_1",
            "sbg:y": 365.83336804972834,
            "sbg:x": 607.5000848770165,
            "outputs": [
                {
                    "id": "#ChIP_seq_FastQC_1.report_zip"
                }
            ],
            "scatter": "#ChIP_seq_FastQC_1.input_fastq"
        },
        {
            "inputs": [
                {
                    "id": "#BWA_Alignment_and_Filtering_1.trimming_param"
                },
                {
                    "source": [
                        "#threads"
                    ],
                    "id": "#BWA_Alignment_and_Filtering_1.threads"
                },
                {
                    "source": [
                        "#reference_index_tar"
                    ],
                    "id": "#BWA_Alignment_and_Filtering_1.reference_index_tar"
                },
                {
                    "id": "#BWA_Alignment_and_Filtering_1.minimum_seed_length"
                },
                {
                    "id": "#BWA_Alignment_and_Filtering_1.memory_limit"
                },
                {
                    "id": "#BWA_Alignment_and_Filtering_1.maximum_edit_distance_seed"
                },
                {
                    "source": [
                        "#SBG_Scatter_Prepare.grouped_files"
                    ],
                    "id": "#BWA_Alignment_and_Filtering_1.input_reads"
                },
                {
                    "default": 7,
                    "id": "#BWA_Alignment_and_Filtering_1.cpu"
                }
            ],
            "run": {
                "sbg:sbgMaintained": false,
                "id": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo/bwa-alignment-and-filtering/1",
                "sbg:links": [
                    {
                        "label": "Homepage BWA",
                        "id": "http://bio-bwa.sourceforge.net/"
                    },
                    {
                        "label": "Source code BWA",
                        "id": "https://github.com/lh3/bwa"
                    },
                    {
                        "label": "Wiki BWA",
                        "id": "http://bio-bwa.sourceforge.net/bwa.shtml"
                    },
                    {
                        "label": "Publication BWA algorithm BWA",
                        "id": "http://www.ncbi.nlm.nih.gov/pubmed/19451168"
                    },
                    {
                        "label": "Homepage SAMTools",
                        "id": "http://www.htslib.org"
                    },
                    {
                        "label": "Source code SAMTools",
                        "id": "https://github.com/samtools/"
                    },
                    {
                        "label": "Wiki SAMTools",
                        "id": "http://www.htslib.org"
                    },
                    {
                        "label": "Publication SAMTools",
                        "id": "https://github.com/samtools/"
                    }
                ],
                "sbg:project": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo",
                "sbg:revision": 1,
                "label": "BWA Alignment and Filtering",
                "arguments": [
                    {
                        "valueFrom": {
                            "script": "{ \n  \tif (!$job.inputs.trimming_param) {$job.inputs.trimming_param = 5}\n\tif (!$job.inputs.minimum_seed_length) {$job.inputs.minimum_seed_length = 32}\n\tif (!$job.inputs.maximum_edit_distance_seed) {$job.inputs.maximum_edit_distance_seed = 2}\n\n\treturn \"\\'-q \" + $job.inputs.trimming_param +\" -l \" + $job.inputs.minimum_seed_length + \" -k \" + $job.inputs.maximum_edit_distance_seed + \"\\'\"\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 5,
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n  function baseName(str)\n\t{\n   base = str.substr(0,str.lastIndexOf('.')); \n   if(base.split('.').pop() == 'tar')       \n        base = base.substr(0,base.lastIndexOf('.'));\n   return base;\n\t}\n  reference_file = $job.inputs.reference_index_tar.path.split('/').pop()\n  //name = reference_file.slice(0, -4) // cut .tar extension \n  name = baseName(reference_file)\n  return name\n  \n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n  \n  // function to get common substring\n  function common_substring(a,b) {\n  \tvar i = 0;\n  \twhile(a[i] === b[i] && i < a.length)\n  \t{\n    i = i + 1;\n  \t}\n\n  \treturn a.slice(0, i);\n  }\n  \n  // return output name if exists\n  if($job.inputs.output_name){\n    name = $job.inputs.output_name \n    return name + \" -\"\n  }\n  \n  \n  // Set output file name\n  if($job.inputs.input_reads[0] instanceof Array){\n    input_1 = $job.inputs.input_reads[0][0] // scatter mode\n    input_2 = $job.inputs.input_reads[0][1]\n  } else if($job.inputs.input_reads instanceof Array){\n    input_1 = $job.inputs.input_reads[0]\n    input_2 = $job.inputs.input_reads[1]\n  }else {\n    input_1 = [].concat($job.inputs.input_reads)[0]\n    input_2 = input_1\n  }\n  \n  \n  // Single-end case \n  if ($job.inputs.input_reads.length == 1){ \n    name = input_1.path.split('/')[input_1.path.split('/').length-1]\n\n    if(name.slice(-3, name.length) === '.gz' || name.slice(-3, name.length) === '.GZ')\n      name = name.slice(0, -3)   \n    if(name.slice(-3, name.length) === '.fq' || name.slice(-3, name.length) === '.FQ')\n      name = name.slice(0, -3)\n    if(name.slice(-6, name.length) === '.fastq' || name.slice(-6, name.length) === '.FASTQ')\n      name = name.slice(0, -6)\n    if(name.slice(-1, name.length) === '-')\n      name = name.slice(0, -1)\n    if(name.slice(-4, name.length) === '.sai')\n      name = name.slice(0, -4)\n      \n    name = name + \".bam\"\n  \n  // paired-end case\n  }else{\n    full_name = input_1.path.split('/')[input_1.path.split('/').length-1]\n    full_name2 = input_2.path.split('/')[input_2.path.split('/').length-1] \n    name = common_substring(full_name, full_name2)\n    \n    if (name == \"\") { \n      name = full_name // if there is no common subtring just use the name of the first pair\n      if(name.slice(-3, name.length) === '.gz' || name.slice(-3, name.length) === '.GZ')\n      \t\tname = name.slice(0, -3)   \n      if(name.slice(-3, name.length) === '.fq' || name.slice(-3, name.length) === '.FQ')\n      \t\tname = name.slice(0, -3)\n      if(name.slice(-6, name.length) === '.fastq' || name.slice(-6, name.length) === '.FASTQ')\n        \tname = name.slice(0, -6)\n      if(name.slice(-1, name.length) === '-')\n      \t\tname = name.slice(0, -1)\n      if(name.slice(-4, name.length) === '.sai')\n      \t\tname = name.slice(0, -4)\n      \n      name = name + \".paired_out\" \n      }\n    \n    //otherwise just strip\n    if(name.slice(-1, name.length) === '_' || name.slice(-1, name.length) === '.')\n      name = name.slice(0, -1)\n    if(name.slice(-2, name.length) === 'p_' || name.slice(-1, name.length) === 'p.')\n      name = name.slice(0, -2)\n    if(name.slice(-2, name.length) === 'P_' || name.slice(-1, name.length) === 'P.')\n      name = name.slice(0, -2)\n    if(name.slice(-3, name.length) === '_p_' || name.slice(-3, name.length) === '.p.')\n      name = name.slice(0, -3)\n    if(name.slice(-3, name.length) === '_pe' || name.slice(-3, name.length) === '.pe')\n      name = name.slice(0, -3)\n    if(name.slice(-1, name.length) === '-')\n      name = name.slice(0, -1)\n    if(name.slice(-4, name.length) === '.sai')\n      name = name.slice(0, -4)\n      \n    name = name + \".bam\"\n  }\n  \n  \n  return name + \" /dev/stdin\"\n}\n\n",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "separate": true,
                        "position": 50,
                        "prefix": ""
                    },
                    {
                        "valueFrom": {
                            "script": "{\n  \tthreads = 8\n  \tif($job.inputs.threads)\n    {\n    \tthreads = $job.inputs.threads\n    }\n  \t\n  \tsize_sum = 0\n  \tinput_reads = $job.inputs.input_reads\n    \n  \tfor (i = 0; i < input_reads.length; i++)\n  \t{\n  \t\tsize_sum += input_reads[i].size\n  \t}\n  \tif(size_sum > 5368709120)\n  \t{\n   \t\tthreads = 8\n  \t}\t\n    else{ threads = 16 }\n\tif([].concat($job.inputs.input_reads)[0].metadata && [].concat($job.inputs.input_reads)[0].metadata.paired_end)\n    {\n    \tcmd = \" | tee tmp.bam | /opt/samtools/samtools view -h -F 1804 -f 2 -q 30 -u /dev/stdin \"\n        cmd += \" | /opt/samtools/samtools sort -@ \" + threads.toString() + \" -n -o /dev/stdout -T ./tmpdir1 /dev/stdin\"\n        cmd += \" | /opt/samtools/samtools fixmate -r /dev/stdin /dev/stdout \"\n        cmd += \" | /opt/samtools/samtools view -h -F 1804 -f 2 -u /dev/stdin\"\n        cmd += \" | /opt/samtools/samtools sort -@ \" + threads.toString() + \" -T ./tmpdir -o \"\n        return cmd\n    }\n  \telse\n    {\n      cmd = \" | tee tmp.bam | /opt/samtools/samtools view -h -F 1804 -q 30 -b /dev/stdin\"\n      cmd += \" | /opt/samtools/samtools sort -@ \" + threads.toString() + \" -T ./tmpdir -o \"\n      return cmd\n      \n    }\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 6,
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n  \n  // function to get common substring\n  function common_substring(a,b) {\n  \tvar i = 0;\n  \twhile(a[i] === b[i] && i < a.length)\n  \t{\n    i = i + 1;\n  \t}\n\n  \treturn a.slice(0, i);\n  }\n  \n  // return output name if exists\n  if($job.inputs.output_name){\n    name = $job.inputs.output_name \n    return name + \" -\"\n  }\n  \n  \n  // Set output file name\n  if($job.inputs.input_reads[0] instanceof Array){\n    input_1 = $job.inputs.input_reads[0][0] // scatter mode\n    input_2 = $job.inputs.input_reads[0][1]\n  } else if($job.inputs.input_reads instanceof Array){\n    input_1 = $job.inputs.input_reads[0]\n    input_2 = $job.inputs.input_reads[1]\n  }else {\n    input_1 = [].concat($job.inputs.input_reads)[0]\n    input_2 = input_1\n  }\n  \n  \n  // Single-end case \n  if ($job.inputs.input_reads.length == 1){ \n    name = input_1.path.split('/')[input_1.path.split('/').length-1]\n\n    if(name.slice(-3, name.length) === '.gz' || name.slice(-3, name.length) === '.GZ')\n      name = name.slice(0, -3)   \n    if(name.slice(-3, name.length) === '.fq' || name.slice(-3, name.length) === '.FQ')\n      name = name.slice(0, -3)\n    if(name.slice(-6, name.length) === '.fastq' || name.slice(-6, name.length) === '.FASTQ')\n      name = name.slice(0, -6)\n    if(name.slice(-1, name.length) === '-')\n      name = name.slice(0, -1)\n    if(name.slice(-4, name.length) === '.sai')\n      name = name.slice(0, -4)\n      \n    name = name\n  \n  // paired-end case\n  }else{\n    full_name = input_1.path.split('/')[input_1.path.split('/').length-1]\n    full_name2 = input_2.path.split('/')[input_2.path.split('/').length-1] \n    name = common_substring(full_name, full_name2) \n  }\n      //otherwise just strip\n    if(name.slice(-1, name.length) === '_' || name.slice(-1, name.length) === '.')\n      name = name.slice(0, -1)\n    if(name.slice(-2, name.length) === 'p_' || name.slice(-1, name.length) === 'p.')\n      name = name.slice(0, -2)\n    if(name.slice(-2, name.length) === 'P_' || name.slice(-1, name.length) === 'P.')\n      name = name.slice(0, -2)\n    if(name.slice(-3, name.length) === '_p_' || name.slice(-3, name.length) === '.p.')\n      name = name.slice(0, -3)\n    if(name.slice(-3, name.length) === '_pe' || name.slice(-3, name.length) === '.pe')\n      name = name.slice(0, -3)\n    if(name.slice(-1, name.length) === '-')\n      name = name.slice(0, -1)\n    if(name.slice(-4, name.length) === '.sai')\n      name = name.slice(0, -4)\n \n  return \" && /opt/samtools/samtools flagstat tmp.bam > \" + name + \".qc_flagstats.txt && rm tmp.bam\\\"\"\n  \n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 100,
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n  cmd = \";declare -i pipe_statuses=(\\\\${PIPESTATUS[*]});len=\\\\${#pipe_statuses[@]};declare -i tot=0;echo \\\\${pipe_statuses[*]};for (( i=0; i<\\\\${len}; i++ ));do if [ \\\\${pipe_statuses[\\\\$i]} -ne 0 ];then tot=\\\\${pipe_statuses[\\\\$i]}; fi;done;if [ \\\\$tot -ne 0 ]; then >&2 echo Error in piping. Pipe statuses: \\\\${pipe_statuses[*]};fi; if [ \\\\$tot -ne 0 ]; then false;fi\"\n  return cmd\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 55,
                        "separate": true
                    }
                ],
                "x": 600.8334182103499,
                "requirements": [
                    {
                        "requirements": [
                            {
                                "dockerPull": "rabix/js-engine",
                                "class": "DockerRequirement"
                            }
                        ],
                        "class": "ExpressionEngineRequirement",
                        "id": "#cwl-js-engine"
                    },
                    {
                        "class": "CreateFileRequirement",
                        "fileDef": [
                            {
                                "filename": "bwa_aln.py",
                                "fileContent": "import subprocess\nimport sys\n\n\ndef main():\n\tsamtools = \"/opt/samtools/samtools\"\n\tbwa = \"/opt/bwa-0.7.15/bwa\"\n\n\treference_fname = sys.argv[1]\n\treads_fname = sys.argv[2]\n\t#bam_filename =  sys.argv[3]\n\tthreads = sys.argv[3]\n\tbwa_params = sys.argv[4] \n\t\n\t#create input params\n\tpaired_end = False\n\treads_fname = reads_fname.split(\",\")\n\tif len(reads_fname) == 2: paired_end = True\n\telif len(reads_fname) == 1: paired_end = False\n\telse: raise Exception(\"unexpected number of input files\")\n\n\t#commands to run bwa aln\n\tcommands1 = []\n\tsai_fname = []\n\tfor reads in reads_fname:\n\t\tsai_filename = '%s.sai' % (reads)\n\t\tbwa_command = \"%s aln -t %s %s %s %s > %s\" %(bwa, str(threads), bwa_params, reference_fname, reads, sai_filename)\n\t\tcommands1.append(bwa_command)\n\t\tsai_fname.append(sai_filename)\n\n\t#run bwa samse or sampe\n\tif paired_end:\n\t    reads1_filename = sai_fname[0]\n\t    reads2_filename = sai_fname[1]\n\t    #sam_filename = bam_filename + \".sam\"\n\t    steps = [\"%s sampe -P %s %s %s %s %s\"\n\t            % (bwa, reference_fname, reads1_filename, reads2_filename,\n\t               reads_fname[0], reads_fname[1])]\n\n\telse:  # single end\n\t    reads_filename = sai_fname[0]\n\t    steps = [\"%s samse %s %s %s\" % (bwa, reference_fname, reads_filename, reads_fname[0])]\n\n\t           \n\tsteps.extend([\"%s view -hSu -\" % (samtools), \"%s sort -@ %s - \" % (samtools, str(threads))])\n\n\tcommand = \" | \".join(steps)\n\tcommands1.append(command)\n\n\tfor c in commands1:\n\t\tsys.stderr.write(c)\n\t\tsubprocess.check_call(c, shell=True)\n\n\nif __name__ == '__main__':\n    main()"
                            }
                        ]
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:id": "admin/sbg-public-data/bwa-alignment-and-filtering/1",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "ChIP-seq BWA Alignment and Peak Calling Demo",
                "description": "**BWA Alignment and Filtering** consists of three parts:\n\n1. Aligning sequence reads onto a reference genome using BWA-backtrack\n2. Calculating statistics of unfiltered BAM file using samtools flagstat\n3. Filtering BAM file\n\n\n1.**BWA 0.7.15** is an algorithm designed for aligning sequence reads onto a reference genome. **BWA aln** is implemented as a component of BWA. The algorithm will take a FASTQ file of sequencing reads and produce a SAI output. BWA algorithm is designed for Illumina sequence reads up to 100bp.\nBWA **samse** or **sampe** are run after **BWA aln** to produce SAM files. Finally, **samtools** is run to report alignments in the BAM format sorted by leftmost coordinates.\n\n**Notes:**\n\n* BWA requires fasta reference file accompanied with **bwa fasta indices** in TAR file. \n* Multi-hits will be randomly chosen but can later on be eliminated from the resulting BAM file by filtering reads by mapping quality.\n\n2.**Samtools flagstat 1.4-23** does a full pass through the input BAM file to calculate and print statistics. Provides counts for each of 13 categories based primarily on bit flags in the FLAG field. Each category in the output is broken down into QC pass and QC fail, which is presented as \"#PASS + #FAIL\" followed by a description of the category. Categories are given for reads which are:\n\n* secondary: 0x100 bit set\n* supplementary: 0x800 bit set\n* duplicates: 0x400 bit set\n* mapped: 0x4 bit not set\n* paired in sequencing: 0x1 bit set\n* read1: both 0x1 and 0x40 bits set\n* read2: both 0x1 and 0x80 bits set\n* properly paired: both 0x1 and 0x2 bits set and 0x4 bit not set with itself and mate mapped: 0x1 bit set and neither 0x4 nor 0x8 bits set\n* singletons: both 0x1 and 0x8 bits set and bit 0x4 not set\n* and finally, two rows are given that additionally filter on the reference name (RNAME), mate reference name (MRNM), and mapping quality (MAPQ) fields.\n\n3.**Filtering BAM file** performs the following steps:\n\n* Identifies if the input BAM file is singe-ended or paired-ended. \n* Removes  unmapped and mate unmapped reads, secondary alignments, reads failing platform, PCR or optical duplicates (-F 1804)\n* Removes low MAPQ reads (-q 30)\n* If paired end, it will only keep properly paired reads (-f 2) and remove orphan reads (pair was removed by using samtools fixmate)\n* Outputs BAM files sorted by left-most coordinates.",
                "sbg:image_url": null,
                "sbg:latestRevision": 1,
                "sbg:modifiedOn": 1499853306,
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "SAM/BAM-Processing",
                    "Alignment"
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499853284,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499853306,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    }
                ],
                "baseCommand": [
                    {
                        "script": "{\n\n  cmd = \"/bin/bash -c \\\"\"\n\n  reference_file = $job.inputs.reference_index_tar.path.split('/').pop()\n  ext = reference_file.split('.').pop()\n  if (ext == \"tar\") {\n  \t\treturn cmd += 'tar -xf ' + reference_file + ' ; '\n  } else if (ext == \"gz\") {\n        return cmd += 'tar -zxf ' + reference_file + ' ; '\n  }\n  return cmd\n}",
                        "class": "Expression",
                        "engine": "#cwl-js-engine"
                    },
                    "python",
                    "bwa_aln.py"
                ],
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 7,
                        "cpu": 16
                    },
                    "inputs": {
                        "input_reads": [
                            {
                                "size": 3368709120,
                                "secondaryFiles": [],
                                "metadata": {
                                    "size": "3368709120",
                                    "paired_end": "1"
                                },
                                "path": "/path/to/input_reads_1.fastq.gz",
                                "class": "File"
                            },
                            {
                                "size": 3368709120,
                                "secondaryFiles": [],
                                "metadata": {
                                    "size": "3368709120",
                                    "paired_end": "2"
                                },
                                "path": "/path/to/input_reads_2.fastq.gz",
                                "class": "File"
                            }
                        ],
                        "minimum_seed_length": 10,
                        "maximum_edit_distance_seed": 7,
                        "cpu": 0,
                        "threads": null,
                        "trimming_param": 0,
                        "reference_index_tar": {
                            "size": 0,
                            "secondaryFiles": [],
                            "path": "/path/to/reference_index.fa.tar.gz",
                            "class": "File"
                        },
                        "memory_limit": 7
                    }
                },
                "outputs": [
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#alignment_statistics",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_reads",
                            "glob": "*.txt"
                        },
                        "label": "Alignment statistics",
                        "sbg:fileTypes": "TXT",
                        "description": "Alignment statistics before filtering."
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#aligned_reads",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_reads",
                            "glob": "*.bam",
                            "secondaryFiles": [
                                "*.bai"
                            ]
                        },
                        "label": "Filtered BAM",
                        "sbg:fileTypes": "BAM",
                        "description": "Filtered BAM."
                    }
                ],
                "y": 227.5000039868882,
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "sbg:category": "BWA algorithm options",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:stageInput": null,
                        "id": "#trimming_param",
                        "sbg:toolDefaultValue": "5",
                        "label": "Parameter for read trimming.",
                        "description": "Parameter for read trimming. BWA trims a read down to argmax_x{\\sum_{i=x+1}^l(INT-q_i)} if q_l<INT where l is the original read length."
                    },
                    {
                        "sbg:category": "CPU",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:toolDefaultValue": "8",
                        "inputBinding": {
                            "valueFrom": {
                                "script": "{\n  if($job.inputs.threads)\n\treturn $job.inputs.threads\n  return 8\n}",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            },
                            "sbg:cmdInclude": true,
                            "position": 4,
                            "separate": true
                        },
                        "label": "Number of threads",
                        "description": "Number of threads for BWA aln.",
                        "id": "#threads"
                    },
                    {
                        "sbg:category": "Input files",
                        "type": [
                            "null",
                            "File"
                        ],
                        "sbg:stageInput": "link",
                        "id": "#reference_index_tar",
                        "required": false,
                        "label": "Reference Index TAR",
                        "description": "Reference fasta file with BWA index files packed in TAR.",
                        "sbg:fileTypes": "TAR, TAR.GZ"
                    },
                    {
                        "sbg:category": "BWA algorithm options",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:stageInput": null,
                        "id": "#minimum_seed_length",
                        "sbg:toolDefaultValue": "32",
                        "label": "Minimum seed length",
                        "description": "Take the first INT subsequence as seed. If INT is larger than the query sequence, seeding will be disabled. For long reads, this option is typically ranged from 25 to 35 for \u2018-k 2\u2019."
                    },
                    {
                        "sbg:category": "Memory",
                        "type": [
                            "null",
                            "int"
                        ],
                        "id": "#memory_limit",
                        "sbg:toolDefaultValue": "8192",
                        "label": "RAM memory",
                        "description": "RAM memory needed to execute tool."
                    },
                    {
                        "sbg:category": "BWA algorithm options",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:stageInput": null,
                        "id": "#maximum_edit_distance_seed",
                        "sbg:toolDefaultValue": "2",
                        "label": "Maximum edit distance in the seed",
                        "description": "Maximum edit distance in the seed."
                    },
                    {
                        "sbg:category": "Input files",
                        "type": [
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "id": "#input_reads",
                        "inputBinding": {
                            "itemSeparator": ",",
                            "sbg:cmdInclude": true,
                            "position": 1,
                            "separate": true
                        },
                        "label": "Input reads",
                        "description": "Input sequence reads.",
                        "sbg:fileTypes": "FASTQ, FASTQ.GZ, FQ, FQ.GZ",
                        "required": true
                    },
                    {
                        "sbg:category": "CPU",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:stageInput": null,
                        "id": "#cpu",
                        "sbg:toolDefaultValue": "8",
                        "label": "Number of CPUs",
                        "description": "Number of CPUs needed to execute tool."
                    }
                ],
                "successCodes": [],
                "sbg:cmdPreview": "/bin/bash -c \"tar -zxf reference_index.fa.tar.gz ;  python bwa_aln.py  reference_index.fa  /path/to/input_reads_1.fastq.gz,/path/to/input_reads_2.fastq.gz  '-q 5 -l 10 -k 7'   | tee tmp.bam | /opt/samtools/samtools view -h -F 1804 -f 2 -q 30 -u /dev/stdin  | /opt/samtools/samtools sort -@ 8 -n -o /dev/stdout -T ./tmpdir1 /dev/stdin | /opt/samtools/samtools fixmate -r /dev/stdin /dev/stdout  | /opt/samtools/samtools view -h -F 1804 -f 2 -u /dev/stdin | /opt/samtools/samtools sort -@ 8 -T ./tmpdir -o   input_reads.bam /dev/stdin  ;declare -i pipe_statuses=(\\${PIPESTATUS[*]});len=\\${#pipe_statuses[@]};declare -i tot=0;echo \\${pipe_statuses[*]};for (( i=0; i<\\${len}; i++ ));do if [ \\${pipe_statuses[\\$i]} -ne 0 ];then tot=\\${pipe_statuses[\\$i]}; fi;done;if [ \\$tot -ne 0 ]; then >&2 echo Error in piping. Pipe statuses: \\${pipe_statuses[*]};fi; if [ \\$tot -ne 0 ]; then false;fi   && /opt/samtools/samtools flagstat tmp.bam > input_reads.qc_flagstats.txt && rm tmp.bam\"",
                "stdout": "",
                "stdin": "",
                "cwlVersion": "sbg:draft-2",
                "hints": [
                    {
                        "value": {
                            "script": "{\n  size_sum = 0\n  input_reads = $job.inputs.input_reads\n    \n  for (i = 0; i < input_reads.length; i++)\n  {\n  \tsize_sum += input_reads[i].size\n  }\n  if(size_sum > 5368709120)\n  {\n   \treturn 16\n  }\n  \n  if($job.inputs.cpu)\n  {\n    return $job.inputs.cpu\n  }\n  return 8\n}\n\n\n",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "class": "sbg:CPURequirement"
                    },
                    {
                        "value": {
                            "script": "{\n  if($job.inputs.memory_limit) \n    return $job.inputs.memory_limit\n  else return 4098\n}\n",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "class": "sbg:MemRequirement"
                    },
                    {
                        "dockerPull": "images.sbgenomics.com/milos_jordanski/bwa_samtools:0.7.15_1.4.1",
                        "dockerImageId": "",
                        "class": "DockerRequirement"
                    },
                    {
                        "value": "c4.8xlarge;ebs-gp2;700",
                        "class": "sbg:AWSInstanceType"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499853284
            },
            "id": "#BWA_Alignment_and_Filtering_1",
            "sbg:y": 227.5000039868882,
            "sbg:x": 600.8334182103499,
            "outputs": [
                {
                    "id": "#BWA_Alignment_and_Filtering_1.alignment_statistics"
                },
                {
                    "id": "#BWA_Alignment_and_Filtering_1.aligned_reads"
                }
            ],
            "scatter": "#BWA_Alignment_and_Filtering_1.input_reads"
        },
        {
            "inputs": [
                {
                    "default": 6,
                    "id": "#SBG_Filter_ChIP_seq_BAM_1.threads"
                },
                {
                    "id": "#SBG_Filter_ChIP_seq_BAM_1.output_name_prefix"
                },
                {
                    "source": [
                        "#Picard_MarkDuplicates_1.deduped_bam"
                    ],
                    "id": "#SBG_Filter_ChIP_seq_BAM_1.input_bam"
                }
            ],
            "run": {
                "sbg:modifiedOn": 1499854081,
                "id": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo/sbg-filter-chip-seq-bam/1",
                "sbg:links": [
                    {
                        "label": "SAMtools-Homepage",
                        "id": "http://www.htslib.org"
                    },
                    {
                        "label": "SAMtools-Source code",
                        "id": "https://github.com/samtools/"
                    },
                    {
                        "label": "SAMtools-Download",
                        "id": "https://sourceforge.net/projects/samtools/files/samtools/"
                    },
                    {
                        "label": "SAMtools-Publication",
                        "id": "http://www.ncbi.nlm.nih.gov/pubmed/19505943"
                    },
                    {
                        "label": "SAMtools-Documentation",
                        "id": "http://www.htslib.org/doc/samtools.html"
                    },
                    {
                        "label": "SAMtools-Wiki",
                        "id": "http://www.htslib.org"
                    },
                    {
                        "label": "ENCODE publication",
                        "id": "https://www.ncbi.nlm.nih.gov/pubmed/22955991"
                    }
                ],
                "sbg:project": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo",
                "sbg:toolkit": "Samtools",
                "label": "SBG Filter ChIP-seq BAM",
                "sbg:sbgMaintained": false,
                "sbg:toolkitVersion": "v1.3",
                "x": 915.000426756027,
                "successCodes": [],
                "sbg:id": "admin/sbg-public-data/sbg-filter-chip-seq-bam/1",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "ChIP-seq BWA Alignment and Peak Calling Demo",
                "description": "**Filter BAM** performs the following steps:\n\n* Identifies if the input BAM file is singe-ended or paired-ended. \n* Removes  unmapped and mate unmapped reads, secondary alignments, reads failing platform, PCR or optical duplicates (-F 1804)\n* Removes low MAPQ reads (-q 30)\n* If paired end, it will only keep properly paired reads (-f 2) and remove orphan reads (pair was removed by using samtools fixmate)\n* Outputs BAM files sorted by left-most coordinates.",
                "sbg:image_url": null,
                "sbg:latestRevision": 1,
                "requirements": [
                    {
                        "requirements": [
                            {
                                "dockerPull": "rabix/js-engine",
                                "class": "DockerRequirement"
                            }
                        ],
                        "class": "ExpressionEngineRequirement",
                        "id": "#cwl-js-engine"
                    }
                ],
                "arguments": [
                    {
                        "valueFrom": {
                            "script": "{\n  \tthreads = 8\n    \n  \tif($job.inputs.output_name_prefix)\n    {\n      output_name = $job.inputs.output_name_prefix + \".filter.srt.bam\"\n    }\n  \telse\n    {\n        name = $job.inputs.input_bam.path.split('/').pop()\n  \t\tif(name.slice(-4, name.length) === '.bam')\n      \tname = name.slice(0, -4)\n  \t\toutput_name = name + \".filter.srt.bam\"\n    }\n  \n  \tif($job.inputs.threads)\n    {\n    \tthreads = $job.inputs.threads\n    }\n  \n  \tcmd = \"tmp=$(/opt/samtools-1.3/samtools view \" + $job.inputs.input_bam.path + \" | head -n 1 | cut -f 2)\"\n\tcmd += \" && tmp=$(($tmp%2))\"\n\tcmd += \" && if [ $tmp -ne 0 ]; then\"\n    \n    cmd += \" /opt/samtools-1.3/samtools view -h -F 1804 -f 2 -u \" + $job.inputs.input_bam.path\n    cmd += \" | /opt/samtools-1.3/samtools sort -@ \" + threads.toString() + \" -n -o /dev/stdout -T ./tmpdir1 /dev/stdin\"\n    cmd += \" | /opt/samtools-1.3/samtools fixmate -r /dev/stdin /dev/stdout \"\n    cmd += \" | /opt/samtools-1.3/samtools view -h -F 1804 -f 2 -u /dev/stdin\"\n    cmd += \" | /opt/samtools-1.3/samtools sort -@ \" + threads.toString() + \" -T ./tmpdir -o \" + output_name + \" /dev/stdin;\"\n    \n    cmd += \" else\"\n\tcmd += \" /opt/samtools-1.3/samtools view -h -F 1804 -b \" + $job.inputs.input_bam.path\n    cmd += \" | /opt/samtools-1.3/samtools sort -@ \" + threads.toString() + \" -T ./tmpdir -o \" + output_name + \" /dev/stdin; fi\"\n\n\treturn cmd    \n\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 1,
                        "separate": true
                    }
                ],
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "SAM/BAM-Processing"
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499854019,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499854081,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    }
                ],
                "sbg:license": "BSD License, MIT License",
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 2048,
                        "cpu": 1
                    },
                    "inputs": {
                        "threads": null,
                        "input_bam": {
                            "size": 0,
                            "secondaryFiles": [],
                            "metadata": {
                                "paired_end": "1"
                            },
                            "path": "/path/to/input_reads.bam",
                            "class": "File"
                        },
                        "output_name_prefix": "output_name"
                    }
                },
                "sbg:revision": 1,
                "y": 385.0001496606458,
                "sbg:toolAuthor": "Heng Li/Sanger Institute,  Bob Handsaker/Broad Institute, James Bonfield/Sanger Institute,",
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "sbg:category": "Configuration",
                        "type": [
                            "null",
                            "int"
                        ],
                        "label": "Number of threads",
                        "description": "Number of threads.",
                        "id": "#threads"
                    },
                    {
                        "type": [
                            "null",
                            "string"
                        ],
                        "label": "Output name prefix",
                        "description": "Output name prefix (script will add *filt.srt.bam).",
                        "id": "#output_name_prefix"
                    },
                    {
                        "sbg:category": "Input files",
                        "type": [
                            "File"
                        ],
                        "id": "#input_bam",
                        "required": true,
                        "label": "Input reads",
                        "description": "Input BAM sequence reads.",
                        "sbg:fileTypes": "BAM"
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:cmdPreview": "tmp=$(/opt/samtools-1.3/samtools view /path/to/input_reads.bam | head -n 1 | cut -f 2) && tmp=$(($tmp%2)) && if [ $tmp -ne 0 ]; then /opt/samtools-1.3/samtools view -h -F 1804 -f 2 -u /path/to/input_reads.bam | /opt/samtools-1.3/samtools sort -@ 8 -n -o /dev/stdout -T ./tmpdir1 /dev/stdin | /opt/samtools-1.3/samtools fixmate -r /dev/stdin /dev/stdout  | /opt/samtools-1.3/samtools view -h -F 1804 -f 2 -u /dev/stdin | /opt/samtools-1.3/samtools sort -@ 8 -T ./tmpdir -o output_name.filter.srt.bam /dev/stdin; else /opt/samtools-1.3/samtools view -h -F 1804 -b /path/to/input_reads.bam | /opt/samtools-1.3/samtools sort -@ 8 -T ./tmpdir -o output_name.filter.srt.bam /dev/stdin; fi",
                "stdout": "",
                "outputs": [
                    {
                        "type": [
                            "File"
                        ],
                        "id": "#aligned_reads",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_bam",
                            "glob": "*.bam"
                        },
                        "label": "Filtered BAM",
                        "sbg:fileTypes": "BAM",
                        "description": "Filtered BAM."
                    }
                ],
                "stdin": "",
                "baseCommand": [
                    ""
                ],
                "cwlVersion": "sbg:draft-2",
                "hints": [
                    {
                        "value": {
                            "script": "{\n  if($job.inputs.threads)\n  {\n    return $job.inputs.threads\n  }\n  else\n  {\n    return 1\n  }\n}\n\n\n",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "class": "sbg:CPURequirement"
                    },
                    {
                        "dockerPull": "images.sbgenomics.com/ines_desantiago/bwa_and_samtools:0.7.13_1.3",
                        "dockerImageId": "",
                        "class": "DockerRequirement"
                    },
                    {
                        "value": {
                            "script": "{\n  if($job.inputs.memory_limit) return $job.inputs.memory_limit*1024\n  else return 2048\n}\n\n",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "class": "sbg:MemRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499854019
            },
            "id": "#SBG_Filter_ChIP_seq_BAM_1",
            "sbg:y": 385.0001496606458,
            "sbg:x": 915.000426756027,
            "outputs": [
                {
                    "id": "#SBG_Filter_ChIP_seq_BAM_1.aligned_reads"
                }
            ],
            "scatter": "#SBG_Filter_ChIP_seq_BAM_1.input_bam"
        },
        {
            "inputs": [
                {
                    "source": [
                        "#Picard_MarkDuplicates_1.deduped_bam",
                        "#Picard_MarkDuplicates_1.metrics_file"
                    ],
                    "id": "#SBG_Scatter_Prepare_3.files"
                }
            ],
            "sbg:x": 946.6667510006184,
            "id": "#SBG_Scatter_Prepare_3",
            "sbg:y": 108.33333175712148,
            "run": {
                "sbg:sbgMaintained": false,
                "id": "bix-demo/sbgtools-demo/sbg-scatter-prepare/1",
                "sbg:project": "bix-demo/sbgtools-demo",
                "sbg:revision": 1,
                "label": "SBG Scatter Prepare",
                "arguments": [],
                "x": 946.6667510006184,
                "requirements": [
                    {
                        "class": "CreateFileRequirement",
                        "fileDef": [
                            {
                                "filename": "group_by_metadata.py",
                                "fileContent": "\"\"\"\nUsage:\n    group_by_metadata.py\n\nDescription:\n    Prepare Input Files manipulates / creates an output list as group of files with the same metadata.\n\nOptions:\n\n    -h, --help            Show this message.\n\n    -v, -V, --version     Tool version.\n\"\"\"\n\nimport json\n\n\njob = \"\"\nwith open('job.json') as data_file:\n    job = json.load(data_file)\nfiles = job[\"inputs\"][\"files\"]\n\ncontrol_files = []\nreal_samples = []\n\n\nfor file_list in files:\n    if file_list:\n        for f in file_list:\n            print(f)\n            d = {'class': 'File'}\n            d['path'] = f[\"path\"]\n\n            if \"size\" in f:\n                d['size'] = f[\"size\"]\n\n            if \"contents\" in f:\n                d['contents'] = f[\"contents\"]\n\n            if \"name\" in f:\n                d['name'] = f[\"name\"]\n\n            if \"checksum\" in f:\n                d['checksum'] = f[\"checksum\"]\n\n            if \"location\" in f:\n                d['location'] = f[\"location\"]\n\n            if \"metadata\" in f:\n                d['metadata'] = f[\"metadata\"]\n\n            if \"secondaryFiles\" in f:\n                d['secondaryFiles'] = f[\"secondaryFiles\"]\n\n            if \"metadata\" in f and \"chip-seq\" in f[\"metadata\"] and f[\"metadata\"][\"chip-seq\"]==\"sample\":\n                real_samples.append(d)\n            else:\n                control_files.append(d)\n\nif not control_files:\n    groups=[real_samples]\nelif not real_samples:\n    groups = [control_files]\nelse:\n    groups = [control_files, real_samples]\n            \ndata = {}\ndata['grouped_files'] = groups\nwith open('cwl.output.json', 'w') as w:\n    json.dump(data, w)"
                            }
                        ]
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:id": "admin/sbg-public-data/sbg-scatter-prepare/1",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "SBGTools - Demo New",
                "description": "SBG Scatter Prepare prepares inputs for scattering.",
                "sbg:image_url": null,
                "sbg:latestRevision": 1,
                "sbg:modifiedOn": 1499419866,
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499419831,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499419866,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    }
                ],
                "baseCommand": [
                    "python",
                    "group_by_metadata.py"
                ],
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 1000,
                        "cpu": 1
                    },
                    "inputs": {
                        "files": [
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_bam-1.ext",
                                "class": "File"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_bam-2.ext",
                                "class": "File"
                            }
                        ]
                    }
                },
                "outputs": [
                    {
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "label": "Grouped files",
                        "id": "#grouped_files",
                        "outputBinding": {
                            "glob": "*.*"
                        },
                        "description": "Grouped files."
                    }
                ],
                "y": 108.33333175712148,
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "sbg:category": "Input",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "id": "#files",
                        "required": false,
                        "label": "Input files",
                        "description": "Input files."
                    }
                ],
                "successCodes": [],
                "sbg:cmdPreview": "python group_by_metadata.py",
                "stdout": "",
                "stdin": "",
                "cwlVersion": "sbg:draft-2",
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
                        "dockerPull": "images.sbgenomics.com/milos_jordanski/python:2.7",
                        "dockerImageId": "",
                        "class": "DockerRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499419831
            },
            "outputs": [
                {
                    "id": "#SBG_Scatter_Prepare_3.grouped_files"
                }
            ]
        },
        {
            "inputs": [
                {
                    "source": [
                        "#SBG_Filter_ChIP_seq_BAM_1.aligned_reads"
                    ],
                    "id": "#ChIP_seq_SAMtools_flagstat_1.input_file"
                }
            ],
            "run": {
                "sbg:modifiedOn": 1499850909,
                "id": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo/chip-seq-samtools-flagstat/1",
                "sbg:links": [
                    {
                        "label": "Homepage",
                        "id": "http://www.htslib.org"
                    },
                    {
                        "label": "Source code",
                        "id": "https://github.com/samtools/"
                    },
                    {
                        "label": "Download",
                        "id": "https://sourceforge.net/projects/samtools/files/samtools/"
                    },
                    {
                        "label": "Publication",
                        "id": "http://www.ncbi.nlm.nih.gov/pubmed/19505943"
                    },
                    {
                        "label": "Documentation",
                        "id": "http://www.htslib.org/doc/samtools.html"
                    },
                    {
                        "label": "Wiki",
                        "id": "http://www.htslib.org"
                    }
                ],
                "sbg:project": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo",
                "sbg:toolkit": "SAMtools",
                "label": "ChIP-seq SAMtools flagstat",
                "sbg:sbgMaintained": false,
                "sbg:toolkitVersion": "v1.3",
                "x": 1228.33366558288,
                "successCodes": [],
                "sbg:id": "admin/sbg-public-data/chip-seq-samtools-flagstat/1",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "ChIP-seq BWA Alignment and Peak Calling Demo",
                "description": "Does a full pass through the input file to calculate and print statistics to stdout. Accepts Accepts SAM, BAM or CRAM files.\n\nProvides counts for each of 13 categories based primarily on bit flags in the FLAG field. Each category in the output is broken down into QC pass and QC fail, which is presented as \"#PASS + #FAIL\" followed by a description of the category.\n\nThe first row of output gives the total number of reads that are QC pass and fail (according to flag bit 0x200). For example:\n\n122 + 28 in total (QC-passed reads + QC-failed reads)\n\nWhich would indicate that there are a total of 150 reads in the input file, 122 of which are marked as QC pass and 28 of which are marked as \"not passing quality controls\"\n\nFollowing this, additional categories are given for reads which are:\n\n* secondary: 0x100 bit set\n* supplementary: 0x800 bit set\n* duplicates: 0x400 bit set\n* mapped: 0x4 bit not set\n* paired in sequencing: 0x1 bit set\n* read1: both 0x1 and 0x40 bits set\n* read2: both 0x1 and 0x80 bits set\n* properly paired: both 0x1 and 0x2 bits set and 0x4 bit not set with itself and mate mapped: 0x1 bit set and neither 0x4 nor 0x8 bits set\n* singletons: both 0x1 and 0x8 bits set and bit 0x4 not set\n* And finally, two rows are given that additionally filter on the reference name (RNAME), mate reference name (MRNM), and mapping quality (MAPQ) fields.",
                "sbg:image_url": null,
                "sbg:latestRevision": 1,
                "requirements": [
                    {
                        "requirements": [
                            {
                                "dockerPull": "rabix/js-engine",
                                "class": "DockerRequirement"
                            }
                        ],
                        "class": "ExpressionEngineRequirement",
                        "id": "#cwl-js-engine"
                    }
                ],
                "arguments": [],
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "SAM/BAM-Processing"
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499850889,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499850909,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    }
                ],
                "sbg:license": "BSD License, MIT License",
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 4096,
                        "cpu": 8
                    },
                    "inputs": {
                        "input_file": {
                            "size": 0,
                            "secondaryFiles": [],
                            "path": "/path/to/inupt_file_long.bam",
                            "class": "File"
                        }
                    }
                },
                "sbg:revision": 1,
                "y": 307.5001042683955,
                "sbg:toolAuthor": "Heng Li/Sanger Institute,  Bob Handsaker/Broad Institute, James Bonfield/Sanger Institute,",
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "type": [
                            "File"
                        ],
                        "id": "#input_file",
                        "inputBinding": {
                            "sbg:cmdInclude": true,
                            "separate": true
                        },
                        "label": "input file",
                        "description": "input file",
                        "sbg:fileTypes": "BAM,CRAM,SAM",
                        "required": true
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:cmdPreview": "/opt/samtools-1.3/samtools flagstat  /path/to/inupt_file_long.bam > inupt_file_long.qc_flagstats.txt",
                "stdout": {
                    "script": "{ \n  input_1 = [].concat($job.inputs.input_file)[0]\n  name = input_1.path.split('/')[input_1.path.split('/').length-1] \n  \n  //filepath = $job.inputs.input_file.path\n  complete_filename = name.split(\"/\").pop()\n  complete_filename = complete_filename.split(\".\")\n  ext = complete_filename.pop()\n  complete_filename = complete_filename.join(\".\")\n  \n  return complete_filename + \".qc_flagstats.txt\"\n}",
                    "class": "Expression",
                    "engine": "#cwl-js-engine"
                },
                "outputs": [
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#alignment_statistics",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_file",
                            "glob": "*.txt"
                        },
                        "label": "Output file",
                        "sbg:fileTypes": "TXT",
                        "description": "Output file"
                    }
                ],
                "stdin": "",
                "baseCommand": [
                    "/opt/samtools-1.3/samtools",
                    "flagstat"
                ],
                "cwlVersion": "sbg:draft-2",
                "hints": [
                    {
                        "value": {
                            "script": "{\n  if($job.inputs.threads)\n  {\n    return $job.inputs.threads\n  }\n  else\n  {\n    return 8\n  }\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "class": "sbg:CPURequirement"
                    },
                    {
                        "value": {
                            "script": "{\n  if($job.inputs.memory_limit) return $job.inputs.memory_limit*1024\n  else return 4096\n}\n\n",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "class": "sbg:MemRequirement"
                    },
                    {
                        "dockerPull": "images.sbgenomics.com/marouf/samtools:1.3",
                        "dockerImageId": "",
                        "class": "DockerRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499850889
            },
            "id": "#ChIP_seq_SAMtools_flagstat_1",
            "sbg:y": 307.5001042683955,
            "sbg:x": 1228.33366558288,
            "outputs": [
                {
                    "id": "#ChIP_seq_SAMtools_flagstat_1.alignment_statistics"
                }
            ],
            "scatter": "#ChIP_seq_SAMtools_flagstat_1.input_file"
        },
        {
            "inputs": [
                {
                    "id": "#ChIP_seq_Cross_Correlation.threads"
                },
                {
                    "id": "#ChIP_seq_Cross_Correlation.output_name_prefix"
                },
                {
                    "source": [
                        "#SBG_Filter_ChIP_seq_BAM_1.aligned_reads"
                    ],
                    "id": "#ChIP_seq_Cross_Correlation.input_bam"
                }
            ],
            "run": {
                "sbg:modifiedOn": 1499851009,
                "id": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo/chip-seq-cross-correlation/1",
                "sbg:links": [
                    {
                        "label": "Homepage",
                        "id": "https://code.google.com/archive/p/phantompeakqualtools/"
                    },
                    {
                        "label": "Download",
                        "id": "https://code.google.com/archive/p/phantompeakqualtools/downloads"
                    },
                    {
                        "label": "Publication",
                        "id": "http://www.nature.com/nbt/journal/v26/n12/full/nbt.1508.html"
                    },
                    {
                        "label": "Documentation",
                        "id": "https://code.google.com/archive/p/phantompeakqualtools/"
                    },
                    {
                        "label": "ENCODE ChIP-seq pipeline Homepage (TFs)",
                        "id": "https://www.encodeproject.org/chip-seq/transcription_factor/"
                    },
                    {
                        "label": "ENCODE ChIP-seq pipeline Homepage (Histones)",
                        "id": "https://www.encodeproject.org/chip-seq/histone/"
                    },
                    {
                        "label": "Code",
                        "id": "https://code.google.com/archive/p/phantompeakqualtools/"
                    }
                ],
                "sbg:project": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo",
                "sbg:toolkit": "PhantomPeakQualTools",
                "label": "ChIP-seq Cross-Correlation",
                "sbg:sbgMaintained": false,
                "sbg:toolkitVersion": "1.1",
                "x": 1070.0005307992492,
                "successCodes": [],
                "sbg:id": "admin/sbg-public-data/chip-seq-cross-correlation/1",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "ChIP-seq BWA Alignment and Peak Calling Demo",
                "description": "Takes a de-duplicated BAM (no duplicates present) from single end or paired end sequencing reads and computes the predominant fragment length and data quality metrics (NSC and RSC)  based on cross-correlation of stranded read density profiles.\n\n**Inputs:**\n\nIt takes as input a BAM file and uses BEDtools (v2.25.0) to generate a tagAlign file with 15,000,000 randomly selected reads. It will then run PhantomPeakQualTools (v1.1) script _run\\_spp\\_nodups.R_ to compute the predominant fragment length and data quality characteristics based on cross-correlation analysis. \n\n**Outputs:**\n\nGenerates two output files: the cross-correlation QC metrics JSON file and the cross-correlation plot in PDF.\n\nThe final QC metrics output file is created in the JSON format with the following information:\n\n*  **CC\\_plot\\_file**: Name of the PDF file showing the cross-correlation plot\n* **paired_end**: Boolean indicating if the input BAM file was paired end or not\n* **numReads**: Total number of mapped reads in input file (15,000,000 by default)\n* **corr_estFragLen**: Comma separated strand cross-correlation value(s) in decreasing order\n* **PhantomPeak**: Read length/phantom peak strand shift\n* **corr_phantomPeak**: Correlation value at phantom peak\n* **argmin_corr**: Strand shift at which cross-correlation is lowest\n* **min_corr**: Minimum value of cross-correlation\n* **QualityTag**: Quality tag based on thresholded RSC (codes: -2:veryLow,-1:Low,0:Medium,1:High,2:veryHigh)\n* **RSC**: Relative strand cross-correlation coefficient (RSC = corr_estFragLen / min_corr)\n* **NSC**: Normalized strand cross-correlation coefficient  (NSC = corr_estFragLen - min_corr  / corr_phantomPeak - min_corr)\n* **estFragLen**: comma separated strand cross-correlation peak(s) in decreasing order of correlation. The top 3 local maxima locations that are within 90% of the maximum cross-correlation value are output. In almost all cases, the top (first) value in the list represents the predominant fragment length.\n\nNSC values range from a minimum of 1 to larger positive numbers. 1.1 is the critical threshold. \nDatasets with NSC values much less than 1.1 (< 1.05) tend to have low signal to noise or few peaks (this could be biological eg.a factor that truly binds only a few sites in a particular tissue type OR it could be due to poor quality)\n\nRSC values range from 0 to larger positive values. 1 is the critical threshold.\nRSC values significantly lower than 1 (< 0.8) tend to have low signal to noise. The low scores can be due to failed and poor quality ChIP, low read sequence quality and hence lots of mismappings, shallow sequencing depth (significantly below saturation) or a combination of these. Like the NSC, datasets with few binding sites (< 200) which is biologically justifiable also show low RSC scores.\n\nQtag is a thresholded version of RSC.\n\n**Notes:**\n\n(1) The output ratios (NSC, RSC) are meaningfull only for narrow peaks (e.g. NRSF, SRF, MAX,CTCF, Oct2, Sox4, Nanog, P300, CBP) or punctate marks (e.g. H3k9ac, H3k27ac, H3k4me3, H3k4me1 etc.). It is not meaningful for  broad peaks like H3K36me3, H3K27me3, H3K9me3, H3K79me2 or H4K20me1.\n\n(2) Assumes de-duplicated input BAM file (WHERE DUPLICATES ARE REMOVED i.e. MAX 1 READ STARTING AT ANY GENOMIC LOCATION).",
                "sbg:image_url": null,
                "sbg:latestRevision": 1,
                "requirements": [
                    {
                        "requirements": [
                            {
                                "dockerPull": "rabix/js-engine",
                                "class": "DockerRequirement"
                            }
                        ],
                        "class": "ExpressionEngineRequirement",
                        "id": "#cwl-js-engine"
                    },
                    {
                        "class": "CreateFileRequirement",
                        "fileDef": [
                            {
                                "filename": "parse_json.py",
                                "fileContent": "#!/usr/bin/python\n\nimport sys\nimport json\n\ndef parse(fname):\n    with open(fname, 'r') as file:\n        if not file:\n            return None\n\n        lines = file.read().splitlines()\n        line = lines[0].rstrip('\\n')\n\n        headers = ['Filename',\n                   'numReads',\n                   'estFragLen',\n                   'corr_estFragLen',\n                   'PhantomPeak',\n                   'corr_phantomPeak',\n                   'argmin_corr',\n                   'min_corr',\n                   'phantomPeakCoef',\n                   'relPhantomPeakCoef',\n                   'QualityTag']\n        metrics = line.split('\\t')\n        headers.pop(0)\n        metrics.pop(0)\n\n        qc = dict(zip(headers, metrics))\n    return qc\n\ninput_bam_filename =  sys.argv[1]\nis_paired = sys.argv[2]\n\npaired_end = ''\nif is_paired == 1:\n    paired_end = 'true'\nelse:\n    paired_end = 'false'\n    \nqc = parse(input_bam_filename)\n\noutputDic = {\n        \"CC_plot_file\": str(input_bam_filename.split('.')[0]) + '.xcor.png',\n        \"paired_end\": paired_end,\n        'numReads': float(qc.get('numReads')),\n        'corr_estFragLen': float(qc.get('corr_estFragLen')),\n        'PhantomPeak': float(qc.get('PhantomPeak')),\n        'corr_phantomPeak': float(qc.get('corr_phantomPeak')),\n        'argmin_corr': float(qc.get('argmin_corr')),\n        'min_corr': float(qc.get('min_corr')),\n        'QualityTag': float(qc.get('QualityTag')),\n        \"RSC\": float(qc.get('relPhantomPeakCoef')),\n        \"NSC\": float(qc.get('phantomPeakCoef')),\n        \"estFragLen\": float(qc.get('estFragLen'))\n    }\n    \nsys.stderr.write(str(outputDic))\nwith open(input_bam_filename, 'w') as outfile:\n        json.dump(outputDic, outfile)"
                            }
                        ]
                    }
                ],
                "arguments": [
                    {
                        "valueFrom": {
                            "script": "{\n  \n  cmd = \"-i \" + $job.inputs.input_bam.path\n  cmd += \" | awk 'BEGIN{OFS=\\\"\\\\t\\\"}{$4=\\\"N\\\";$5=\\\"1000\\\";print $0}'\"\n  cmd += \" > \" + $job.inputs.input_bam.path.split('/').pop().slice(0,-4) + \".tagAlign &&\"\n  return cmd\n  \n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 1,
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n\n  \ncmd = \"tmp=$(samtools view \" + $job.inputs.input_bam.path + \" | head -n 1 | cut -f 2)\"\ncmd += \" && tmp=$(($tmp%2))\"\ncmd += \" && if [ $tmp==0 ]; then\"\ncmd += \" grep -v 'chrM' *.tagAlign | find . -name '*.tagAlign' -exec shuf -n 15000000 --random-source={} \\\\; | gzip -cn > \" +  $job.inputs.input_bam.path.split('/').pop().slice(0,-4) + \".filt.subsample.tagAlign.gz;\"\ncmd += \" else\"\ncmd += \" grep -v 'chrM' *.tagAlign | find . -name '*.tagAlign' -exec shuf -n 15000000 --random-source={} \\\\; | awk 'BEGIN{OFS=\\\"\\\\t\\\"}{$4=\\\"N\\\";$5=\\\"1000\\\";print $0}' | gzip -cn > \" + $job.inputs.input_bam.path.split('/').pop().slice(0,-4) + \".filt.subsample.tagAlign.gz;\"\ncmd += \" fi &&\"\n\n//# cmd += \" rm *.tagAlign\"\n\nreturn cmd\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 2,
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n\n  threads = 8\n  \tif($job.inputs.threads)\n    {\n    \tthreads = $job.inputs.threads\n    }\n  \n  cmd = \"Rscript /opt/phantompeakqualtools/run_spp_nodups_new.R -c=\" + $job.inputs.input_bam.path.split('/').pop().slice(0,-4) + \".filt.subsample.tagAlign.gz  -p=\" \n  \t\t+ threads + \" -filtchr=chrM -savp=\" + $job.inputs.input_bam.path.split('/').pop().slice(0,-4)\n  \t\t+ \".xcor.png\" + \" -out=\" + $job.inputs.input_bam.path.split('/').pop().slice(0,-4) + \".xcor.qc.json\"\n    \n  \n  cmd += \" && rm \" + $job.inputs.input_bam.path.split('/').pop().slice(0,-4) + \".filt.subsample.tagAlign.gz\"\n  cmd += \" && sed -r  's/,[^\\\\t]+//g' \" + $job.inputs.input_bam.path.split('/').pop().slice(0,-4) + \".xcor.qc.json > temp.\" + $job.inputs.input_bam.path.split('/').pop().slice(0,-4) + \".xcor.qc.json\" \n  cmd += \" && mv temp.\" + $job.inputs.input_bam.path.split('/').pop().slice(0,-4) + \".xcor.qc.json \" + $job.inputs.input_bam.path.split('/').pop().slice(0,-4) + \".xcor.qc.json\"\n  return cmd\n\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 3,
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n\nreturn \"&& echo $tmp | python parse_json.py *.xcor.qc.json -\"\n\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 4,
                        "separate": true
                    }
                ],
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "SAM/BAM-Processing",
                    "ChIP-seq"
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499850985,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499851009,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    }
                ],
                "sbg:license": "MIT License, Copyright (c) 2016 ENCODE DCC",
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 2048,
                        "cpu": 1
                    },
                    "inputs": {
                        "threads": null,
                        "input_bam": {
                            "size": 0,
                            "secondaryFiles": [],
                            "path": "/path/to/input_reads.merged.bam",
                            "class": "File"
                        },
                        "output_name_prefix": "output_file"
                    }
                },
                "sbg:revision": 1,
                "y": 267.50007156531257,
                "sbg:toolAuthor": "Anshul Kundaje, ENCODE Consortium",
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "sbg:category": "Configuration",
                        "type": [
                            "null",
                            "int"
                        ],
                        "label": "Threads",
                        "description": "Number of threads for PhantomPeakQualTools.",
                        "id": "#threads"
                    },
                    {
                        "sbg:category": "Output files",
                        "type": [
                            "null",
                            "string"
                        ],
                        "label": "Output_name_prefix",
                        "description": "Output name prefix (script will add *.json and *.pdf).",
                        "id": "#output_name_prefix"
                    },
                    {
                        "sbg:category": "Input files",
                        "type": [
                            "File"
                        ],
                        "id": "#input_bam",
                        "required": true,
                        "label": "Input reads BAM",
                        "description": "Input reads BAM.",
                        "sbg:fileTypes": "BAM"
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:cmdPreview": "bamToBed   -i /path/to/input_reads.merged.bam | awk 'BEGIN{OFS=\"\\t\"}{$4=\"N\";$5=\"1000\";print $0}' > input_reads.merged.tagAlign &&  tmp=$(samtools view /path/to/input_reads.merged.bam | head -n 1 | cut -f 2) && tmp=$(($tmp%2)) && if [ $tmp==0 ]; then grep -v 'chrM' *.tagAlign | find . -name '*.tagAlign' -exec shuf -n 15000000 --random-source={} \\; | gzip -cn > input_reads.merged.filt.subsample.tagAlign.gz; else grep -v 'chrM' *.tagAlign | find . -name '*.tagAlign' -exec shuf -n 15000000 --random-source={} \\; | awk 'BEGIN{OFS=\"\\t\"}{$4=\"N\";$5=\"1000\";print $0}' | gzip -cn > input_reads.merged.filt.subsample.tagAlign.gz; fi &&  Rscript /opt/phantompeakqualtools/run_spp_nodups_new.R -c=input_reads.merged.filt.subsample.tagAlign.gz  -p=8 -filtchr=chrM -savp=input_reads.merged.xcor.png -out=input_reads.merged.xcor.qc.json && rm input_reads.merged.filt.subsample.tagAlign.gz && sed -r  's/,[^\\t]+//g' input_reads.merged.xcor.qc.json > temp.input_reads.merged.xcor.qc.json && mv temp.input_reads.merged.xcor.qc.json input_reads.merged.xcor.qc.json  && echo $tmp | python parse_json.py *.xcor.qc.json -",
                "stdout": "",
                "outputs": [
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#output_xcor_plot",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_bam",
                            "glob": "*.png"
                        },
                        "label": "Output xcor plot",
                        "sbg:fileTypes": "PNG",
                        "description": "Output xcor plot."
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#output_xcor_metrics",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_bam",
                            "glob": "*.qc.json"
                        },
                        "label": "Output xcor metrics",
                        "sbg:fileTypes": "JSON",
                        "description": "Output xcor metrics."
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#intermediate_TA_file",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_bam",
                            "glob": "*.tagAlign"
                        },
                        "label": "TagAlign",
                        "sbg:fileTypes": "tagAlign",
                        "description": "Intermediate tagAlign file."
                    }
                ],
                "stdin": "",
                "baseCommand": [
                    {
                        "script": "{\n\treturn \"bamToBed\"\n}",
                        "class": "Expression",
                        "engine": "#cwl-js-engine"
                    },
                    ""
                ],
                "cwlVersion": "sbg:draft-2",
                "hints": [
                    {
                        "value": {
                            "script": "{\n  if($job.inputs.threads)\n  {\n    return $job.inputs.threads\n  }\n  else\n  {\n    return 1\n  }\n}\n\n\n",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "class": "sbg:CPURequirement"
                    },
                    {
                        "value": {
                            "script": "{\n  if($job.inputs.memory_limit) return $job.inputs.memory_limit*1024\n  else return 2048\n}\n",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "class": "sbg:MemRequirement"
                    },
                    {
                        "dockerPull": "images.sbgenomics.com/nemanja_vucic/spp_and_phantomqualtools:1.14_1.1",
                        "dockerImageId": "",
                        "class": "DockerRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499850985
            },
            "id": "#ChIP_seq_Cross_Correlation",
            "sbg:y": 267.50007156531257,
            "sbg:x": 1070.0005307992492,
            "outputs": [
                {
                    "id": "#ChIP_seq_Cross_Correlation.output_xcor_plot"
                },
                {
                    "id": "#ChIP_seq_Cross_Correlation.output_xcor_metrics"
                },
                {
                    "id": "#ChIP_seq_Cross_Correlation.intermediate_TA_file"
                }
            ],
            "scatter": "#ChIP_seq_Cross_Correlation.input_bam"
        },
        {
            "inputs": [
                {
                    "id": "#SBG_ChIP_seq_Library_Complexity.threads"
                },
                {
                    "id": "#SBG_ChIP_seq_Library_Complexity.output_name"
                },
                {
                    "source": [
                        "#SBG_Scatter_Prepare_3.grouped_files"
                    ],
                    "id": "#SBG_ChIP_seq_Library_Complexity.input_files"
                }
            ],
            "run": {
                "sbg:modifiedOn": 1499850871,
                "id": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo/sbg-chip-seq-library-complexity/1",
                "sbg:links": [
                    {
                        "label": "ENCODE ChIP-seq pipeline Homepage (TFs)",
                        "id": "https://www.encodeproject.org/chip-seq/transcription_factor/"
                    },
                    {
                        "label": "Library Complexity ENCODE documentation",
                        "id": "https://www.encodeproject.org/data-standards/terms/#library"
                    },
                    {
                        "label": "Publication",
                        "id": "https://www.ncbi.nlm.nih.gov/pubmed/22955991"
                    }
                ],
                "sbg:project": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo",
                "sbg:toolkit": "SAMtools, BEDtools",
                "label": "SBG ChIP-seq Library Complexity",
                "sbg:sbgMaintained": false,
                "sbg:toolkitVersion": "v0.0.1, v1.3.1, v2.25.0",
                "x": 1116.6668320496924,
                "successCodes": [],
                "sbg:id": "admin/sbg-public-data/sbg-chip-seq-library-complexity/1",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "ChIP-seq BWA Alignment and Peak Calling Demo",
                "description": "Takes a raw aligned bam file from SE or PE sequencing with marked duplicated reads (duplicated reads should not be removed)  and calculates ChIP-seq Library complexity metrics. Library complexity is measured using the Non-Redundant Fraction (NRF) and PCR Bottlenecking Coefficients 1 and 2, or PBC1 and PBC2. \n\n**Inputs:**\n\nIt takes as input a BAM file and uses BEDtools (v2.25.0) to generate a BED (single-end) or BEDPE (paired-end) file and obtain fragment coordinates. \nThen, through a series of *awk* commands it will parse the generated BED/BEDPE file and obtain unique count statistics. \nIt will also required a previously generated  Picard DuplicationMetrics file (generated from the same BAM).\n\n**Outputs:**\n\nThe final output file is created in the JSON format with the following QC metrics:\n\n* **TotalReadPairs**: Total number of reads\n* **DistinctReadPairs**: Number of distinct uniquely mapping reads (i.e. after removing duplicates)\n* **OneReadPair**: Number of genomic locations where exactly one read maps uniquely\n* **TwoReadPairs**: Number of genomic locations where two reads map uniquely\n* **Non-Redundant Fraction (NRF)**: Number of distinct uniquely mapping reads  / Total number of reads (or DistinctReadPairs/TotalReadPairs)\n* **PCR Bottlenecking Coefficients 1 (PBC1)**: PBC1 = OneReadPair/DistinctReadPairs\n* **PCR Bottlenecking Coefficients 2 (PBC2)**: PBC2 = OneReadPair/TwoReadPairs\n* **percent_duplication**: This metric is taken directly from PICARD MarkDuplicates output *.metrics file and corresponds to the percentage of mapped sequence that is marked as duplicate.\n\nPreferred values are as follows: NRF>0.9, PBC1 \u2265 0.9, and PBC2 \u2265 10.",
                "sbg:image_url": null,
                "sbg:latestRevision": 1,
                "requirements": [
                    {
                        "requirements": [
                            {
                                "dockerPull": "rabix/js-engine",
                                "class": "DockerRequirement"
                            }
                        ],
                        "class": "ExpressionEngineRequirement",
                        "id": "#cwl-js-engine"
                    },
                    {
                        "class": "CreateFileRequirement",
                        "fileDef": [
                            {
                                "filename": "parse_qc.py",
                                "fileContent": "import sys\nimport json\nimport os\n\n#Input \nMARK_DUPS_METRICS_NAME = sys.argv[1]\nOUTPUT_QC_NAME = sys.argv[2]\nOUTPUT_METRICS_FILENAME = sys.argv[3]\n\n#Function to parse Picard DuplicationMetrics file\ndef dup_parse(fname):\n    with open(fname, 'r') as dup_file:\n        if not dup_file:\n            return None\n\n        lines = iter(dup_file.read().splitlines())\n\n        for line in lines:\n            if line.startswith('## METRICS CLASS'):\n                headers = lines.next().rstrip('\\n').lower()\n                metrics = lines.next().rstrip('\\n')\n                break\n\n        headers = headers.split('\\t')\n        metrics = metrics.split('\\t')\n        headers.pop(0)\n        metrics.pop(0)\n\n        dup_qc = dict(zip(headers, metrics))\n    return dup_qc\n\n#Function to parse the BED file\ndef pbc_parse(fname):\n    with open(fname, 'r') as pbc_file:\n        if not pbc_file:\n            return None\n\n        lines = pbc_file.read().splitlines()\n        line = lines[0].rstrip('\\n')\n\n        headers = ['TotalReadPairs',\n                   'DistinctReadPairs',\n                   'OneReadPair',\n                   'TwoReadPairs',\n                   'NRF',\n                   'PBC1',\n                   'PBC2']\n        metrics = line.split('\\t')\n\n        pbc_qc = dict(zip(headers, metrics))\n    return pbc_qc\n\n\n# =============================\n# Compute library complexity\n# =============================\ndef main():\n    \n    dup_qc = dup_parse(MARK_DUPS_METRICS_NAME)\n    pbc_qc = pbc_parse(OUTPUT_QC_NAME)\n    \n    # Return Json file\n    outputDic = {\n        \"NRF\": pbc_qc.get('NRF'),\n        \"PBC1\": pbc_qc.get('PBC1'),\n        \"PBC2\": pbc_qc.get('PBC2'),\n        'TotalReadPairs': pbc_qc.get('TotalReadPairs'),\n        'DistinctReadPairs': pbc_qc.get('DistinctReadPairs'),\n        'OneReadPair':pbc_qc.get('OneReadPair'),\n        'TwoReadPairs':pbc_qc.get('TwoReadPairs'),\n        \"percent_duplication\": dup_qc.get('percent_duplication')\n    }\n    \n    sys.stderr.write(str(outputDic))\n    with open(OUTPUT_METRICS_FILENAME, 'w') as outfile:\n        json.dump(outputDic, outfile)\n\n\nif __name__ == '__main__':\n    main()"
                            }
                        ]
                    }
                ],
                "arguments": [
                    {
                        "valueFrom": {
                            "script": "{\n  \tthreads = 8\n    input_files = [].concat($job.inputs.input_files)\n    \n    if(input_files.length != 2)\n      return \"echo \\\"You should have two input files\\\"\"\n    \n   \n    name_tmp = input_files[0].path.split('/').pop()\n    if(name_tmp.slice(-4, name_tmp.length) === '.bam')\n    { \n      input_bam = 0\n      input_metrics = 1\n    }\n    else\n    {\n      input_bam = 1\n      input_metrics = 0\n    }\n\n    output_name_tmp = [].concat($job.inputs.input_files)[input_bam].path.split('/').pop()\n    if(output_name_tmp.slice(-4, output_name_tmp.length) === '.bam')\n      output_name_tmp = output_name_tmp.slice(0, -4)\n    else\n      output_name_tmp = output_name_tmp.split('.').slice(0, output_name_tmp.split('.').length - 1).join('.')\n    \n  \tif($job.inputs.output_name)\n    {\n      output_name = $job.inputs.output_name + \".json\"\n    }\n  \telse\n    {\n        name = [].concat($job.inputs.input_files)[input_bam].path.split('/').pop()\n     \n  \t\tif(name.slice(-4, name.length) === '.bam')\n      \t\toutput_name = name.slice(0, -4) + \".libcomp.qc.json\"\n        \n    }\n  \n  \tif($job.inputs.threads)\n    {\n    \tthreads = $job.inputs.threads\n    }\n  \n  \tcmd = \"tmp=$(/opt/samtools-1.3.1/samtools view \" + [].concat($job.inputs.input_files)[input_bam].path + \" | head -n 1 | cut -f 2)\"\n\tcmd += \" && tmp=$(($tmp%2))\"\n\tcmd += \" && if [ $tmp -ne 0 ]; then\"\n    \n    cmd += \" /opt/samtools-1.3.1/samtools sort -@ \" + threads.toString() + \" -n \" + [].concat($job.inputs.input_files)[input_bam].path\n    cmd += \" | /opt/bedtools2/bin/bamToBed -bedpe -i -\"\n    cmd += \" | awk 'BEGIN{OFS=\\\"\\\\t\\\"}{print $1,$2,$4,$6,$9,$10}'\"\n    cmd += \" | grep -v 'chrM'\"\n    cmd += \" | sort\"\n    cmd += \" | uniq -c\"\n    cmd += \" | awk 'BEGIN{mt=0;m0=0;m1=0;m2=0} ($1==1){m1=m1+1} ($1==2){m2=m2+1} {m0=m0+1} {mt=mt+$1} END{printf \\\"%d\\\\t%d\\\\t%d\\\\t%d\\\\t%f\\\\t%f\\\\t%f\\\\n\\\",mt,m0,m1,m2,m0/mt,m1/m0,m1/m2}'\"\n    cmd += \" > \" + output_name_tmp + \".pbc.qc;\"\n    \n    cmd += \" else\"\n    cmd += \" /opt/bedtools2/bin/bamToBed -i \" + [].concat($job.inputs.input_files)[input_bam].path\n    cmd += \" | awk 'BEGIN{OFS=\\\"\\\\t\\\"}{print $1,$2,$3,$6}'\"\n    cmd += \" | grep -v 'chrM'\"\n    cmd += \" | sort\"\n    cmd += \" | uniq -c\"\n    cmd += \" | awk 'BEGIN{mt=0;m0=0;m1=0;m2=0} ($1==1){m1=m1+1} ($1==2){m2=m2+1} {m0=m0+1} {mt=mt+$1} END{printf \\\"%d\\\\t%d\\\\t%d\\\\t%d\\\\t%f\\\\t%f\\\\t%f\\\\n\\\",mt,m0,m1,m2,m0/mt,m1/m0,m1/m2}'\"\n    cmd += \" > \" + output_name_tmp + \".pbc.qc; fi\"\n    \n    cmd += \" && python parse_qc.py \" + [].concat($job.inputs.input_files)[input_metrics].path + \" \" + output_name_tmp + \".pbc.qc\" + \" \" + output_name\n\tcmd += \" && rm \" + output_name_tmp + \".pbc.qc\"   \n\treturn cmd    \n\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "separate": true
                    }
                ],
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "SAM/BAM-Processing",
                    "ChIP-seq"
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499850842,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499850871,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    }
                ],
                "sbg:license": "MIT License, Copyright (c) 2016 ENCODE DCC",
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 2048,
                        "cpu": 1
                    },
                    "inputs": {
                        "threads": null,
                        "input_files": [
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_files-1.bam",
                                "class": "File"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_files-2.txt",
                                "class": "File"
                            }
                        ],
                        "output_name": ""
                    }
                },
                "sbg:revision": 1,
                "y": 108.33332739935939,
                "sbg:toolAuthor": "ENCODE consortium",
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "sbg:category": "Configuration",
                        "type": [
                            "null",
                            "int"
                        ],
                        "label": "Number of threads",
                        "description": "Number of threads for samtools sort.",
                        "id": "#threads"
                    },
                    {
                        "sbg:category": "Output files",
                        "type": [
                            "null",
                            "string"
                        ],
                        "label": "Output name for JSON output file",
                        "description": "Output name for JSON output file.",
                        "id": "#output_name"
                    },
                    {
                        "sbg:category": "Input",
                        "type": [
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "sbg:stageInput": null,
                        "id": "#input_files",
                        "required": true,
                        "label": "Input files",
                        "description": "Input files."
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:cmdPreview": "tmp=$(/opt/samtools-1.3.1/samtools view /path/to/input_files-1.bam | head -n 1 | cut -f 2) && tmp=$(($tmp%2)) && if [ $tmp -ne 0 ]; then /opt/samtools-1.3.1/samtools sort -@ 8 -n /path/to/input_files-1.bam | /opt/bedtools2/bin/bamToBed -bedpe -i - | awk 'BEGIN{OFS=\"\\t\"}{print $1,$2,$4,$6,$9,$10}' | grep -v 'chrM' | sort | uniq -c | awk 'BEGIN{mt=0;m0=0;m1=0;m2=0} ($1==1){m1=m1+1} ($1==2){m2=m2+1} {m0=m0+1} {mt=mt+$1} END{printf \"%d\\t%d\\t%d\\t%d\\t%f\\t%f\\t%f\\n\",mt,m0,m1,m2,m0/mt,m1/m0,m1/m2}' > input_files-1.pbc.qc; else /opt/bedtools2/bin/bamToBed -i /path/to/input_files-1.bam | awk 'BEGIN{OFS=\"\\t\"}{print $1,$2,$3,$6}' | grep -v 'chrM' | sort | uniq -c | awk 'BEGIN{mt=0;m0=0;m1=0;m2=0} ($1==1){m1=m1+1} ($1==2){m2=m2+1} {m0=m0+1} {mt=mt+$1} END{printf \"%d\\t%d\\t%d\\t%d\\t%f\\t%f\\t%f\\n\",mt,m0,m1,m2,m0/mt,m1/m0,m1/m2}' > input_files-1.pbc.qc; fi && python parse_qc.py /path/to/input_files-2.txt input_files-1.pbc.qc input_files-1.libcomp.qc.json && rm input_files-1.pbc.qc",
                "stdout": "",
                "outputs": [
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#metrics_json",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_files",
                            "glob": "*.qc.json"
                        },
                        "label": "Library complexity QC metrics",
                        "sbg:fileTypes": "JSON",
                        "description": "Library complexity QC metrics."
                    }
                ],
                "stdin": "",
                "baseCommand": [
                    ""
                ],
                "cwlVersion": "sbg:draft-2",
                "hints": [
                    {
                        "value": {
                            "script": "{\n  if($job.inputs.threads)\n  {\n    return $job.inputs.threads\n  }\n  else\n  {\n    return 1\n  }\n}\n\n\n",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "class": "sbg:CPURequirement"
                    },
                    {
                        "dockerPull": "images.sbgenomics.com/ines_desantiago/bedtools_and_samtools:2.25.0_1.3.1",
                        "dockerImageId": "",
                        "class": "DockerRequirement"
                    },
                    {
                        "value": {
                            "script": "{\n  if($job.inputs.memory_limit) return $job.inputs.memory_limit*1024\n  else return 2048\n}\n",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "class": "sbg:MemRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499850842
            },
            "id": "#SBG_ChIP_seq_Library_Complexity",
            "sbg:y": 108.33332739935939,
            "sbg:x": 1116.6668320496924,
            "outputs": [
                {
                    "id": "#SBG_ChIP_seq_Library_Complexity.metrics_json"
                }
            ],
            "scatter": "#SBG_ChIP_seq_Library_Complexity.input_files"
        },
        {
            "inputs": [
                {
                    "source": [
                        "#ChIP_seq_Cross_Correlation.output_xcor_metrics",
                        "#ChIP_seq_SAMtools_flagstat_1.alignment_statistics",
                        "#BWA_Alignment_and_Filtering_1.alignment_statistics",
                        "#ChIP_seq_Cross_Correlation.output_xcor_plot",
                        "#SBG_ChIP_seq_Library_Complexity.metrics_json"
                    ],
                    "id": "#SBG_Scatter_Prepare_1.files"
                }
            ],
            "sbg:x": 1368.333997229759,
            "id": "#SBG_Scatter_Prepare_1",
            "sbg:y": 204.16668491893384,
            "run": {
                "sbg:sbgMaintained": false,
                "id": "bix-demo/sbgtools-demo/sbg-scatter-prepare/1",
                "sbg:project": "bix-demo/sbgtools-demo",
                "sbg:revision": 1,
                "label": "SBG Scatter Prepare",
                "arguments": [],
                "x": 1368.333997229759,
                "requirements": [
                    {
                        "class": "CreateFileRequirement",
                        "fileDef": [
                            {
                                "filename": "group_by_metadata.py",
                                "fileContent": "\"\"\"\nUsage:\n    group_by_metadata.py\n\nDescription:\n    Prepare Input Files manipulates / creates an output list as group of files with the same metadata.\n\nOptions:\n\n    -h, --help            Show this message.\n\n    -v, -V, --version     Tool version.\n\"\"\"\n\nimport json\n\n\njob = \"\"\nwith open('job.json') as data_file:\n    job = json.load(data_file)\nfiles = job[\"inputs\"][\"files\"]\n\ncontrol_files = []\nreal_samples = []\n\n\nfor file_list in files:\n    if file_list:\n        for f in file_list:\n            print(f)\n            d = {'class': 'File'}\n            d['path'] = f[\"path\"]\n\n            if \"size\" in f:\n                d['size'] = f[\"size\"]\n\n            if \"contents\" in f:\n                d['contents'] = f[\"contents\"]\n\n            if \"name\" in f:\n                d['name'] = f[\"name\"]\n\n            if \"checksum\" in f:\n                d['checksum'] = f[\"checksum\"]\n\n            if \"location\" in f:\n                d['location'] = f[\"location\"]\n\n            if \"metadata\" in f:\n                d['metadata'] = f[\"metadata\"]\n\n            if \"secondaryFiles\" in f:\n                d['secondaryFiles'] = f[\"secondaryFiles\"]\n\n            if \"metadata\" in f and \"chip-seq\" in f[\"metadata\"] and f[\"metadata\"][\"chip-seq\"]==\"sample\":\n                real_samples.append(d)\n            else:\n                control_files.append(d)\n\nif not control_files:\n    groups=[real_samples]\nelif not real_samples:\n    groups = [control_files]\nelse:\n    groups = [control_files, real_samples]\n            \ndata = {}\ndata['grouped_files'] = groups\nwith open('cwl.output.json', 'w') as w:\n    json.dump(data, w)"
                            }
                        ]
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:id": "admin/sbg-public-data/sbg-scatter-prepare/1",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "SBGTools - Demo New",
                "description": "SBG Scatter Prepare prepares inputs for scattering.",
                "sbg:image_url": null,
                "sbg:latestRevision": 1,
                "sbg:modifiedOn": 1499419866,
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499419831,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499419866,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    }
                ],
                "baseCommand": [
                    "python",
                    "group_by_metadata.py"
                ],
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 1000,
                        "cpu": 1
                    },
                    "inputs": {
                        "files": [
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_bam-1.ext",
                                "class": "File"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_bam-2.ext",
                                "class": "File"
                            }
                        ]
                    }
                },
                "outputs": [
                    {
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "label": "Grouped files",
                        "id": "#grouped_files",
                        "outputBinding": {
                            "glob": "*.*"
                        },
                        "description": "Grouped files."
                    }
                ],
                "y": 204.16668491893384,
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "sbg:category": "Input",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "id": "#files",
                        "required": false,
                        "label": "Input files",
                        "description": "Input files."
                    }
                ],
                "successCodes": [],
                "sbg:cmdPreview": "python group_by_metadata.py",
                "stdout": "",
                "stdin": "",
                "cwlVersion": "sbg:draft-2",
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
                        "dockerPull": "images.sbgenomics.com/milos_jordanski/python:2.7",
                        "dockerImageId": "",
                        "class": "DockerRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499419831
            },
            "outputs": [
                {
                    "id": "#SBG_Scatter_Prepare_1.grouped_files"
                }
            ]
        },
        {
            "inputs": [
                {
                    "id": "#SBG_Merge_ChIP_seq_QC_metrics_1.output_name_prefix"
                },
                {
                    "source": [
                        "#SBG_Scatter_Prepare_1.grouped_files"
                    ],
                    "id": "#SBG_Merge_ChIP_seq_QC_metrics_1.input_files"
                }
            ],
            "run": {
                "sbg:modifiedOn": 1499850819,
                "id": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo/sbg-merge-chip-seq-qc-metrics/1",
                "sbg:categories": [
                    "Other",
                    "Converters",
                    "ChIP-seq"
                ],
                "sbg:project": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo",
                "sbg:toolkit": "SBGTools",
                "label": "SBG Merge ChIP-seq QC metrics",
                "sbg:sbgMaintained": false,
                "x": 1548.333759248271,
                "successCodes": [],
                "sbg:id": "admin/sbg-public-data/sbg-merge-chip-seq-qc-metrics/1",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "ChIP-seq BWA Alignment and Peak Calling Demo",
                "description": "Tool is a simple re-packer for the final output file of the *\"SBG ChIP-seq filter and QC\"* workflow. Accepts SAMtools flagstat reports and JSON files as the inputs and groups all QC metrics in order for a nice HTML render to be produced. The resulting output is a JSON file with all QC metrics grouped together and an HTML file with a table of all QC metrics.",
                "sbg:image_url": null,
                "sbg:latestRevision": 1,
                "requirements": [
                    {
                        "class": "CreateFileRequirement",
                        "fileDef": [
                            {
                                "filename": "parse_QC_outputs.py",
                                "fileContent": "import docopt\nimport re\nimport json\nimport sys\n\n#Reads in Flagstat QC, Lib Complexity QC and Xcor QC outputs and saves all in a JSON file\nUSAGE = \"\"\"\n    Usage:\n    \tparse_QC_outputs.py --unfilteredFlagStat <unfilteredFlagStat> --filteredFlagStat <filteredFlagStat> --xcorJS <xcorJS> --libcompJS <libcompJS> --out <out>\n\n    Description:\n        Tool accepts the output file of samtools flagstat and creates a human readable tab delimited output.\n        the output is a table of QC metrics written in JSON and HTML formats\n\n    Options:\n\n        --help                              Show help dialog\n        --version                           Tool version.\n        --xcorJS <xcorJS>   the input JSON file for cross-correlaiton QC step\n        --libcompJS <libcompJS> the input JSON file for Library Complexity QC step\n        --unfilteredFlagStat <unfilteredFlagStat>   the input flagstat file (unfiltered)\n        --filteredFlagStat <filteredFlagStat>   the input flagstat file (filtered)\n        --out <out> the output file name for JSON and HTML outputs\n\"\"\"\n\n#python parse_QC_outputs.py --filteredFlagStat example_flagstatOutput.txt --xcorJS sample_1.filt.subsample.cc.qc.json --unfilteredFlagStat example_flagstatOutput.txt --libcompJS sample_1.deduped.filt.srt.LibComp.qc.json --out sample_1.final.qc\n\ndef flagstat_parse(fname):\n    with open(fname, 'r') as flagstat_file:\n        if not flagstat_file:\n            return None\n        flagstat_lines = flagstat_file.read().splitlines()\n    qc_dict = {\n        # values are regular expressions,\n        # will be replaced with scores [hiq, lowq]\n        'in_total': 'in total',\n        'duplicates': 'duplicates',\n        'mapped': 'mapped',\n        'paired_in_sequencing': 'paired in sequencing',\n        'read1': 'read1',\n        'read2': 'read2',\n        'properly_paired': 'properly paired',\n        'with_self_mate_mapped': 'with itself and mate mapped',\n        'singletons': 'singletons',\n        # i.e. at the end of the line\n        'mate_mapped_different_chr': 'with mate mapped to a different chr$',\n        # RE so must escape\n        'mate_mapped_different_chr_hiQ':\n            'with mate mapped to a different chr \\(mapQ>=5\\)'\n    }\n    for (qc_key, qc_pattern) in qc_dict.items():\n        qc_metrics = next(re.split(qc_pattern, line)\n                          for line in flagstat_lines\n                          if re.search(qc_pattern, line))\n        (hiq, lowq) = qc_metrics[0].split(' + ')\n        qc_dict[qc_key] = [int(hiq.rstrip()), int(lowq.rstrip())]\n    return qc_dict\n\ndef main():\n    args = docopt.docopt(USAGE, version = 1.0)\n    #read in mapping_stats_unfiltered\n    mapping_stats_unfiltered = args[\"--unfilteredFlagStat\"]\n    mapping_stats_unfiltered = flagstat_parse(mapping_stats_unfiltered)\n\n    #read in mapping_stats_filtered\n    mapping_stats_filtered = args[\"--filteredFlagStat\"]\n    mapping_stats_filtered = flagstat_parse(mapping_stats_filtered)\n\n    #read in library_complexity_QC\n    library_complexity_QC = args[\"--libcompJS\"]\n    with open(library_complexity_QC, \"r\") as infile:\n    \tlibrary_complexity_QC = json.load(infile)\n\n    #read in xcor_QC\n    xcor_QC = args[\"--xcorJS\"]\n    with open(xcor_QC, \"r\") as infile:\n    \txcor_QC = json.load(infile)\n    \n    #read in txt\n    png = open('tmp.txt')\n    \n    #write output - Json\n    output = args[\"--out\"]\n    output = output + \".qc.json\"\n    finalDic = {\"mapping_stats_unfiltered\":mapping_stats_unfiltered,\n    \"library_complexity_QC\":library_complexity_QC,\n    \"xcor_QC\":xcor_QC, \n    \"mapping_stats_filtered\":mapping_stats_filtered}\n    with open(output, 'w') as f:\n        json.dump(finalDic, f)\n    \n    #write output - HTML table\n    output = args[\"--out\"]\n    output = output + \".qc.html\"\n\n    flagstat_keys = [\"in_total\",\"duplicates\",\"mapped\",\n                     \"paired_in_sequencing\",\"read1\",\"read2\",\n                     \"properly_paired\", \"with_self_mate_mapped\",\"singletons\",\n                     \"mate_mapped_different_chr\",\n                     \"mate_mapped_different_chr_hiQ\"]\n    xcor_keys= ['CC_plot_file', 'paired_end','numReads',\n    'corr_estFragLen','PhantomPeak','corr_phantomPeak',\n    'argmin_corr','min_corr','QualityTag','RSC','NSC','estFragLen']\n    libcomp_keys = [\"NRF\",\"PBC1\",\"PBC2\",\"TotalReadPairs\", \"DistinctReadPairs\",\"OneReadPair\",\"TwoReadPairs\",\"percent_duplication\"]   \n    \n    \n    \n    htmlLine1 = \"\"\"<!DOCTYPE html>\\n<html>\\n<head>\\n<style>\\ntable { \\\n    \\nfont-family: arial, sans-serif;\\nborder-collapse: collapse;\\n} \\\n    td, th {\\nborder: 1px solid #dddddd;\\ntext-align: left;\\npadding: 4px;\\n}\\n \\\n    tr:nth-child(even) {\\nbackground-color: #dddddd;\\n}\\n</style>\\n</head>\\n<body>\\n\\n\"\"\"\n\n    html = htmlLine1 + \"\"\"<font face=\"arial, sans-serif\" size=2>\\n\\n\"\"\" \n    html = html + \"\"\"<p><font size=5>Unfiltered BAM mapping stats</font></p>\\n<table>\\n\"\"\"\n    \n    html = html + '\\t<tr>\\n\\t\\t<th>label</th>\\n\\t\\t<th>value</th>\\n\\t</tr>\\n' \n    for k in flagstat_keys: html += '\\t<tr>\\n\\t\\t<td>' + k + \"</td>\\n\\t\\t<td>\" + str(mapping_stats_unfiltered[k]) + '</td>\\n\\t</tr>\\n'  \n    html += \"\"\"</table>\\n\\n<p><font size=5>Library Complexity QC metrics</font></p>\\n<table>\"\"\"\n    html = html + '\\t<tr>\\n\\t\\t<th>label</th>\\n\\t\\t<th>value</th>\\n\\t</tr>\\n' \n    for k in libcomp_keys: html += '\\t<tr>\\n\\t\\t<td>' + k + \"</td>\\n\\t\\t<td>\" + str(library_complexity_QC[k]) + '</td>\\n\\t</tr>\\n'   \n    html += \"\"\"</table>\\n\\n<p><font size=5>Cross-correlation QC metrics</font></p></p>\\n<table>\"\"\"\n    html = html + '\\t<tr>\\n\\t\\t<th>label</th>\\n\\t\\t<th>value</th>\\n\\t</tr>\\n' \n        \n    for k in xcor_keys: \n        if k == 'CC_plot_file':\n            html += '\\t<tr>\\n\\t\\t<td>' + k + '</td>\\n\\t\\t<td>' + str(xcor_QC[k])[:-4] + '.png' + '</td>\\n\\t\\t'r'''<td rowspan=\"12\"><img style='display:block; width:500px;height:500px;' id='base64image' ''' + '\\n' r'''src='data:image/jpeg;base64, ''' + png.read() + \"'\" + '\\n /></td>\\n\\t</tr>\\n'\n        else:\n            html += '\\t<tr>\\n\\t\\t<td>' + k + \"</td>\\n\\t\\t<td>\" + str(xcor_QC[k]) + '</td>\\n\\t</tr>\\n' \n    html += \"\"\"</table>\\n\\n<p><font size=5>Filtered BAM mapping stats</font></p>\\n<table>\"\"\"\n    html = html + '\\t<tr>\\n\\t\\t<th>label</th>\\n\\t\\t<th>value</th>\\n\\t</tr>\\n' \n    for k in flagstat_keys: html += '\\t<tr>\\n\\t\\t<td>'+ k + \"</td>\\n\\t\\t<td>\" + str(mapping_stats_filtered[k]) + '</td>\\n\\t</tr>\\n' \n    html += '</font></table>\\n\\n</body>\\n</html>'\n    with open(output, 'w') as f:\n        f.write(html)\n\nif __name__ == '__main__':\n    main()"
                            },
                            {
                                "filename": "sbg_html_to_b64_new.py",
                                "fileContent": "\"\"\"\nUsage:\n    sbg_html_to_b64.py --input FILE [--select FILE]\n\nDescription:\n    This tool is used for conversion of html file to b64 html file so it can be easily displayed in browsers.\n\nOptions:\n    -h, --help      Show this help message and exit. (For third class of tools it's required to put\n                    this option).\n\n    -v, --version   Show version and exit.\n\n    --input FILE    Input file is archive containing html and all other files included in the html file(images, etc).\n\n    --select FILE If we wish to select specific html file from folder that we wish to parse.\n\nExamples:\n    python sbg_html_to_b64.py --input sample_fastqc.zip\n\"\"\"\n\nimport os\nfrom docopt import docopt\nimport os.path\nimport base64\nimport mimetypes\nfrom bs4 import BeautifulSoup\nfrom path import Path\nfrom subprocess import call, check_output\n\n\ndef dataurl(data, mime=None):\n    isfile = os.path.isfile(data)\n    if not isfile and not mime:\n        raise Exception('Mimetype must be provided when encoding data is not a valid file path.')\n    if not mime:\n        mimetypes.init()\n        mime, enc = mimetypes.guess_type(os.path.join('file://', data))\n        if mime is None:\n            raise Exception('rfc2397: failed to determine file type')\n    if isfile:\n        with open(data, 'r') as fp:\n            data = fp.read()\n    return 'data:%s;base64,%s' % (mime, base64.b64encode(data))\n\n\ndef compact_html(html_file):\n    with open(html_file) as f:\n        html = f.read()\n\n    if 'snpEff_summary' in html_file:\n        for l in html.split('\\n'):\n            if str(l).startswith('<a name'):\n                html = html.replace(str(l), str(l) + '</a>')\n        html = html.replace('<p>', '<p></p>')\n        html = html[:-358]\n        soup = BeautifulSoup(html, \"html5lib\")\n\n        js = \"javascript: void(0); document.getElementById('%s').scrollIntoView(true);\"\n        for anchor in soup.findAll('a'):\n            if 'href' in str(anchor):\n                if anchor['href'].startswith('#'):\n                    anchor['href'] = js % anchor['href'][1:]\n                else:\n                    anchor.decompose()\n            else:\n                anchor['id'] = anchor['name']\n\n        return soup.prettify()\n\n    else:\n        base_dir = os.path.split(html_file)[0]\n        soup = BeautifulSoup(html, \"html5lib\")\n        # soup = BeautifulSoup(html.decode('utf-8', 'ignore'), \"html5lib\")\n        # soup.decode('utf-8')\n        for img in soup.findAll('img'):\n            if img['src'].find('data:') == 0:\n                durl_img = img['src']\n            else:\n                durl_img = dataurl(os.path.join(base_dir, img['src']))\n            img['src'] = durl_img\n\n        js = \"javascript: void(0); document.getElementById('%s').scrollIntoView(true);\"\n        for anchor in soup.findAll('a'):\n            if anchor['href'].startswith('#'):\n                anchor['href'] = js % anchor['href'][1:]\n            else:\n                del anchor['href']  # = '#'\n        return soup.prettify()\n\n\ndef html_to_dataurl(html_file):\n    return dataurl(compact_html(html_file), mime='text/html')\n\nif __name__ == \"__main__\":\n    args = docopt(__doc__, version='1.0')\n    filename = args.get('--input')\n\n    # unzipping the archive\n    #cmd = [\"unzip\", filename, \"-d\", \"./unzip\"]\n    #call(cmd)\n    html_file = check_output([\"find\", \"./\", \"-iname\", \"*.html\"]).split('\\n')[:-1]\n    b64_html = Path(filename).namebase + '.b64html'\n    html_file = html_file[0]\n\n    # check if we need to process single or list of html files. if it is a single file then html_file is type string\n    if type(html_file) is str:\n        with open(b64_html, 'wa') as fp:\n            fp.write(html_to_dataurl(html_file))\n    elif type(b64_html) is list:\n        for i, elem in enumerate(b64_html):\n            with open(elem, 'wa') as fp:\n                print html_file[i]\n                fp.write(html_to_dataurl(html_file[i]))\n    else:\n        raise Exception('This is not good.')"
                            },
                            {
                                "filename": "extract_frag_length.py",
                                "fileContent": "import sys\nimport json\n\ncwlname = \"output.tmp\"\n\nname = sys.argv[1]\nwith open(name, \"r\") as infile:\n    a = json.load(infile)\n    if \"xcor_QC\" in a.keys():\n        length =  a[\"xcor_QC\"][\"estFragLen\"]\n    else:\n        length = a[\"estFragLen\"]\n\nwith open(cwlname, \"w\") as outfile:\n    outfile.write(str(length))"
                            }
                        ]
                    },
                    {
                        "requirements": [
                            {
                                "dockerPull": "rabix/js-engine",
                                "class": "DockerRequirement"
                            }
                        ],
                        "class": "ExpressionEngineRequirement",
                        "id": "#cwl-js-engine"
                    }
                ],
                "arguments": [
                    {
                        "valueFrom": {
                            "script": "{\n  \n  if($job.inputs.output_name_prefix){\n    return $job.inputs.output_name_prefix\n  }\n  \n  //else{\n  //  input_1 = [].concat($job.inputs.input_files)[0]\n\n  //  if (input_1.metadata && input_1.metadata.sample_id){\n   \t\t\n  //    return input_1.metadata.sample_id\n  //  }\n  \n    else {\n  \t\n      return [].concat($job.inputs.input_files)[0].path.split('/').pop().split('.')[0]\n  \t}\n  //}\n}\n\n",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "separate": true,
                        "position": 5,
                        "prefix": "--out"
                    },
                    {
                        "valueFrom": {
                            "script": "{\n\treturn \"&& find . -name '*.html' -exec python sbg_html_to_b64_new.py --input {} \\\\;\"\n\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 6,
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n\n  if($job.inputs.output_name_prefix){\n    input = $job.inputs.output_name_prefix\n  }\n  \n  //else{\n  //  input = [].concat($job.inputs.input_files)[0]\n  //\n  //  if (input.metadata && input.metadata.sample_id){\n   \t\t\n  //    input = input.metadata.sample_id\n  //  }\n  \n  else {\n  \t\n      input = [].concat($job.inputs.input_files)[0].path.split('/').pop().split('.')[0]\n  //\t}\n  }\n  return \"&& find . -name \" + input + \".qc.json -exec python extract_frag_length.py {} \\\\;\"\n}\n\n\n",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 7,
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n\tinput_files = [].concat($job.inputs.input_files)\n    //return input_files[0].path\n    \n    //if(input_files.length != 4)\n      //return \"echo \\\"You should have four input files\\\"\"\n    \n    for(count = 0; count < 5; count++){\n      input_name = input_files[count].path.split('/').pop();\n      //return input_name.slice(-17, input_name.length)\n      {\n      \tif (input_name.slice(-21, input_name.length) === '.srt.qc_flagstats.txt'){\n      \t  input_filteredFlagStat = count\n      }\n      \telse if (input_name.slice(-17, input_name.length) === '.qc_flagstats.txt'){\n           input_unfilteredFlagStat = count\n      }\n      \telse if(input_name.slice(-13, input_name.length) === '.xcor.qc.json'){\n           input_xcorJS = count\n      }\n      \telse if (input_name.slice(-16, input_name.length) === '.libcomp.qc.json'){\n           input_libcompJS = count\n      }\n      \t}\n            }\n  cmd = \"--unfilteredFlagStat \" + input_files[input_unfilteredFlagStat].path\n  cmd += \" --filteredFlagStat \" + input_files[input_filteredFlagStat].path\n  cmd += \" --xcorJS \" + input_files[input_xcorJS].path\n  cmd += \" --libcompJS \" + input_files[input_libcompJS].path\n  \n  \n  return cmd\n\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 1,
                        "separate": true
                    }
                ],
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499850785,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499850819,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    }
                ],
                "baseCommand": [
                    {
                        "script": "{\n\nreturn \"test=\" +\"\\\"$(cat *.png | base64)\\\"\" + \" && echo $test > tmp.txt && \"\n\n}",
                        "class": "Expression",
                        "engine": "#cwl-js-engine"
                    },
                    "python",
                    "parse_QC_outputs.py"
                ],
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 1024,
                        "cpu": 6
                    },
                    "inputs": {
                        "input_files": [
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "metadata": {
                                    "sample_id": "ENCFF00QEX",
                                    "chip-seq": "sample"
                                },
                                "path": "/path/to/ENCFF000QEX_con1.qc_flagstats.txt",
                                "class": "File"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "metadata": {
                                    "sample_id": "ENCFF00QEX",
                                    "chip-seq": "sample"
                                },
                                "path": "/path/to/ENCFF000QEX_con1.deduped.libcomp.qc.json",
                                "class": "File"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "metadata": {
                                    "sample_id": "ENCFF00QEX"
                                },
                                "path": "/path/to/ENCFF000QEX_con1.deduped.filter.srt.xcor.qc.json",
                                "class": "File"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "metadata": {
                                    "sample_id": "ENCFF00QEX"
                                },
                                "path": "/path/to/ENCFF000QEX_con1.deduped.filter.srt.qc_flagstats.txt",
                                "class": "File"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/ENCFF000ARK.deduped.filter.srt.xcor.pdf",
                                "class": "File"
                            }
                        ],
                        "output_name_prefix": ""
                    }
                },
                "sbg:revision": 1,
                "y": 204.16669512457395,
                "sbg:toolAuthor": "Seven Bridges Genomics",
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "sbg:category": "Output files",
                        "type": [
                            "null",
                            "string"
                        ],
                        "id": "#output_name_prefix",
                        "label": "Output_name_prefix",
                        "sbg:altPrefix": "Output_name",
                        "description": "The output name prefix for JSON and HTML outputs."
                    },
                    {
                        "sbg:category": "Input",
                        "type": [
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "sbg:stageInput": "link",
                        "id": "#input_files",
                        "required": true,
                        "label": "Input files",
                        "description": "Input files.",
                        "sbg:fileTypes": "TXT, JSON"
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:cmdPreview": "test=\"$(cat *.png | base64)\" && echo $test > tmp.txt &&  python parse_QC_outputs.py  --unfilteredFlagStat /path/to/ENCFF000QEX_con1.qc_flagstats.txt --filteredFlagStat /path/to/ENCFF000QEX_con1.deduped.filter.srt.qc_flagstats.txt --xcorJS /path/to/ENCFF000QEX_con1.deduped.filter.srt.xcor.qc.json --libcompJS /path/to/ENCFF000QEX_con1.deduped.libcomp.qc.json --out ENCFF000QEX_con1  && find . -name '*.html' -exec python sbg_html_to_b64_new.py --input {} \\;  && find . -name ENCFF000QEX_con1.qc.json -exec python extract_frag_length.py {} \\;",
                "stdout": "",
                "outputs": [
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#output_json",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#unfilteredFlagStat",
                            "glob": {
                                "script": "{\n\n  return $job.inputs.input_files[0].path.split('/').pop().split('.').shift() + \".qc.json\"\n}",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            }
                        },
                        "label": "Output json",
                        "sbg:fileTypes": "JSON",
                        "description": "Output JSON."
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#output_html",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#unfilteredFlagStat",
                            "glob": "*.qc.html"
                        },
                        "label": "Output html",
                        "sbg:fileTypes": "HTML",
                        "description": "Output HTML."
                    },
                    {
                        "type": [
                            "null",
                            "int"
                        ],
                        "label": "Fragment length",
                        "id": "#fragLen",
                        "outputBinding": {
                            "glob": "output.tmp",
                            "loadContents": true,
                            "outputEval": {
                                "script": "{\n  \n  if([].concat($job.inputs.input_files)[0].metadata && [].concat($job.inputs.input_files)[0].metadata['chip-seq']=='sample'){\n    return parseInt($self[0].contents)\n  }  \n}",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            }
                        },
                        "description": "Fragment length."
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#b64html",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#filteredFlagStat",
                            "glob": "*b64html"
                        },
                        "label": "B64html",
                        "sbg:fileTypes": "HTML, B64HTML",
                        "description": "Output file, b64html."
                    }
                ],
                "stdin": "",
                "cwlVersion": "sbg:draft-2",
                "hints": [
                    {
                        "value": {
                            "script": "{\n  if($job.inputs.threads)\n  {\n    return $job.inputs.threads\n  }\n  else\n  {\n    return 6\n  }\n}\n\n\n",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "class": "sbg:CPURequirement"
                    },
                    {
                        "dockerPull": "images.sbgenomics.com/nemanja_vucic/python:2.7.6",
                        "dockerImageId": "",
                        "class": "DockerRequirement"
                    },
                    {
                        "value": 1024,
                        "class": "sbg:MemRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499850785
            },
            "id": "#SBG_Merge_ChIP_seq_QC_metrics_1",
            "sbg:y": 204.16669512457395,
            "sbg:x": 1548.333759248271,
            "outputs": [
                {
                    "id": "#SBG_Merge_ChIP_seq_QC_metrics_1.output_json"
                },
                {
                    "id": "#SBG_Merge_ChIP_seq_QC_metrics_1.output_html"
                },
                {
                    "id": "#SBG_Merge_ChIP_seq_QC_metrics_1.fragLen"
                },
                {
                    "id": "#SBG_Merge_ChIP_seq_QC_metrics_1.b64html"
                }
            ],
            "scatter": "#SBG_Merge_ChIP_seq_QC_metrics_1.input_files"
        },
        {
            "inputs": [
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.write_overlap_additional"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.write_overlap"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.write_ovelap_b"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.write_original_a"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.write_no_overlap_b"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.write_in_b"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.write_in_a"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.uncompressed_bam_output"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.strand_same"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.strand_diff"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.split"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.sorted"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.sort_output_db"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.show_filenames"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.req_min_frac"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.req_frac_overlap"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.output_bed"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.nonamecheck"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.names_alias"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.left_outer_join"
                },
                {
                    "source": [
                        "#input_files_b_1"
                    ],
                    "id": "#ChIP_seq_BEDTools_intersect_1.input_files_b"
                },
                {
                    "source": [
                        "#MACS2.NARROW_PEAK",
                        "#MACS2.BROAD_PEAK"
                    ],
                    "id": "#ChIP_seq_BEDTools_intersect_1.input_files_a"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.input_buf_size"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.header"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.genome_file"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.fraction_b"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.fraction_a"
                },
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.disable_buf_out"
                }
            ],
            "sbg:x": 2247.5010658701644,
            "id": "#ChIP_seq_BEDTools_intersect_1",
            "sbg:y": 573.3336409462935,
            "run": {
                "sbg:modifiedOn": 1499851080,
                "id": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo/chip-seq-bedtools-intersect/1",
                "sbg:links": [
                    {
                        "label": "Homepage",
                        "id": "http://bedtools.readthedocs.org/"
                    },
                    {
                        "label": "Source code",
                        "id": "https://github.com/arq5x/bedtools2"
                    },
                    {
                        "label": "Publication",
                        "id": "http://bioinformatics.oxfordjournals.org/content/26/6/841"
                    },
                    {
                        "label": "Documentation",
                        "id": "https://media.readthedocs.org/pdf/bedtools/latest/bedtools.pdf"
                    },
                    {
                        "label": "Download",
                        "id": "https://github.com/arq5x/bedtools2/releases/download/v2.25.0/bedtools-2.25.0.tar.gz"
                    },
                    {
                        "label": "Wiki",
                        "id": "http://seqanswers.com/wiki/BEDTools"
                    }
                ],
                "sbg:project": "nemanja.vucic/chip-seq-bwa-alignment-and-peak-calling-demo",
                "sbg:toolkit": "BEDTools",
                "label": "ChIP-seq BEDTools intersect",
                "sbg:sbgMaintained": false,
                "sbg:toolkitVersion": "2.25.0",
                "x": 2247.5010658701644,
                "successCodes": [],
                "sbg:id": "admin/sbg-public-data/chip-seq-bedtools-intersect/1",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "ChIP-seq BWA Alignment and Peak Calling Demo",
                "description": "BEDTools intersect reports the overlap between multiple feature files.\n\nThe most common question asked of two sets of genomic features is whether or not any of the features in the two sets \u201coverlap\u201d with one another. This is known as feature intersection. BEDTools intersect screens for overlaps between two sets of genomic features. Moreover, it allows the user to have fine control over how the intersections are reported. BEDTools intersect allows both BED/GFF/VCF and BAM files as inputs.\n\nWhen you are using Bedtools intersect, you can choose the compress level of bam, or output bed files. The time and storage would be quit different. All Input = 6GB bam file:\n\n* Output compressed bam file (-ubam=false, equal to bwa's compress level=1, most compressed): output bam=5.6Gb, Duration: 18 minutes\n* Output uncompressed bam file (-ubam=true, equal to bwa's compress level=9, least compressed): output bam=30.2Gb, Duration: 17 minutes\n* Output bed file (-bed=true): output bed=9.2Gb, Duration: 10 minutes",
                "sbg:image_url": null,
                "sbg:latestRevision": 1,
                "requirements": [
                    {
                        "requirements": [
                            {
                                "dockerPull": "rabix/js-engine",
                                "class": "DockerRequirement"
                            }
                        ],
                        "class": "ExpressionEngineRequirement",
                        "id": "#cwl-js-engine"
                    }
                ],
                "arguments": [
                    {
                        "valueFrom": {
                            "script": "{\n  inputs = [].concat($job.inputs.input_files_a)\n  \n  for (i = 0; i < inputs.length; i++)\n    if(inputs[i]){\n  \tfileA = inputs[i]\n    }\n  filepath = fileA.path\n  filename = filepath.split(\"/\").pop()\n  basename = filename.substr(0,filename.lastIndexOf(\".\"))\n  \n  file_dot_sep = filename.split(\".\")\n  file_ext = file_dot_sep[file_dot_sep.length-1]\n  \n  sufix_ext = file_ext\n  if (file_ext == \"bam\") {return \"-abam \" + filepath}\n  else {return \"-a \" + filepath} \n\n     }",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 99,
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n  if([].concat($job.inputs.input_files_b)[0]){\n  fileB = [].concat($job.inputs.input_files_b)[0]\n  \n  filepath = fileB.path\n  return \"-b \" + filepath\n  }\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 100,
                        "separate": true
                    },
                    {
                        "valueFrom": "-v",
                        "position": 101,
                        "separate": true
                    }
                ],
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "BED-Processing"
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499851056,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499851080,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    }
                ],
                "sbg:license": "GNU General Public License v2.0 only",
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 1000,
                        "cpu": 1
                    },
                    "inputs": {
                        "write_no_overlap_b": false,
                        "input_file_b": {
                            "size": 0,
                            "secondaryFiles": [],
                            "path": "input_file_b.bed",
                            "class": "File"
                        },
                        "input_buf_size": null,
                        "sorted": false,
                        "nonamecheck": false,
                        "output_bed": false,
                        "write_original_a": false,
                        "fraction_b": null,
                        "uncompressed_bam_output": false,
                        "write_in_b": false,
                        "strand_diff": false,
                        "fraction_a": null,
                        "disable_buf_out": false,
                        "sort_output_db": false,
                        "input_files_b": {
                            "size": 0,
                            "secondaryFiles": [],
                            "path": "/path/to/blacklist_file",
                            "class": "File"
                        },
                        "show_filenames": "",
                        "strand_same": false,
                        "split": "",
                        "input_files_a": [
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "path": "/path/to/input_file-a.narrowPeaks",
                                "class": "File"
                            }
                        ],
                        "header": false,
                        "names_alias": "",
                        "req_frac_overlap": false,
                        "write_overlap_additional": false,
                        "req_min_frac": false,
                        "write_ovelap_b": false,
                        "write_overlap": false,
                        "left_outer_join": false,
                        "write_in_a": false
                    }
                },
                "sbg:revision": 1,
                "y": 573.3336409462935,
                "sbg:toolAuthor": "Aaron R. Quinlan & Neil Kindlon",
                "sbg:createdBy": "nemanja.vucic",
                "inputs": [
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#write_overlap_additional",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-wao"
                        },
                        "label": "Write A, B, overlap and additional",
                        "description": "Write the original A and B entries plus the number of base pairs of overlap between the two features. Overlapping features restricted by -f and -r. However, A features w/o overlap are also reported \twith a NULL B feature and overlap = 0."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#write_overlap",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-wo"
                        },
                        "label": "Write A, B and overlap",
                        "description": "Write the original A and B entries plus the number of base pairs of overlap between the two features. Overlaps restricted by -f and -r. Only A features with overlap are reported."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#write_ovelap_b",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-c"
                        },
                        "label": "For entry in A write overlap if in B",
                        "description": "For each entry in A, report the number of overlaps with B. Reports 0 for A entries that have no overlap with B. Overlaps restricted by -f and -r."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#write_original_a",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-u"
                        },
                        "label": "Write original A entry if found in B",
                        "description": "Write the original A entry _once_ if _any_ overlaps found in B. \t\t- In other words, just report the fact >=1 hit was found. \t\t- Overlaps restricted by -f and -r."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#write_no_overlap_b",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-v"
                        },
                        "label": "Write entry in A and no overlaps in B",
                        "description": "Only report those entries in A that have _no overlaps_ with B. Similar to \"grep -v\" (an homage)."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#write_in_b",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-wb"
                        },
                        "label": "Write the original entry in B",
                        "description": "Write the original entry in B for each overlap. \t\t- Useful for knowing _what_ A overlaps. Restricted by -f and -r."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#write_in_a",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-wa"
                        },
                        "label": "Write the original entry in A",
                        "description": "Write the original entry in A for each overlap."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#uncompressed_bam_output",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-ubam"
                        },
                        "label": "Uncompressed BAM output",
                        "description": "Write uncompressed BAM output. Default writes compressed BAM."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#strand_same",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-s"
                        },
                        "label": "Require same strandedness",
                        "description": "Require same strandedness. That is, only report hits in B that overlap A on the _same_ strand. By default, overlaps are reported without respect to strand."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#strand_diff",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-S"
                        },
                        "label": "Require different strandedness",
                        "description": "Require different strandedness. That is, only report hits in B that overlap A on the _opposite_ strand. By default, overlaps are reported without respect to strand."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "string"
                        ],
                        "id": "#split",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-split"
                        },
                        "label": "Split",
                        "description": "Treat \"split\" BAM or BED12 entries as distinct BED intervals."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#sorted",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-sorted"
                        },
                        "label": "Sorted",
                        "description": "Use the \"chromsweep\" algorithm for sorted (-k1,1 -k2,2n) input."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#sort_output_db",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-sortout"
                        },
                        "label": "Sort output DB hits for each record",
                        "description": "When using multiple databases, sort the output DB hits for each record."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "string"
                        ],
                        "id": "#show_filenames",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "prefix": "-filenames"
                        },
                        "label": "Show complete filename",
                        "description": "When using multiple databases, show each complete filename instead of a fileId when also printing the DB record."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#req_min_frac",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-e"
                        },
                        "label": "Require minimum fraction",
                        "description": "Require that the minimum fraction be satisfied for A _OR_ B. In other words, if -e is used with -f 0.90 and -F 0.10 this requires that either 90% of A is covered OR 10% of B is covered. Without -e, both fractions would have to be satisfied."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#req_frac_overlap",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-r"
                        },
                        "label": "Require fraction overlap",
                        "description": "Require that the fraction of overlap be reciprocal for A and B. In other words, if -f is 0.90 and -r is used, this requires that B overlap at least 90% of A and that A also overlaps at least 90% of B."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#output_bed",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-bed"
                        },
                        "label": "Output as bed",
                        "description": "If using BAM input, write output as BED."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#nonamecheck",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-nonamecheck"
                        },
                        "label": "No name check",
                        "description": "For sorted data, don't throw an error if the file has different naming conventions \t\t\tfor the same chromosome. ex. \"chr1\" vs \"chr01\"."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "string"
                        ],
                        "id": "#names_alias",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "prefix": "-names"
                        },
                        "label": "Names alias",
                        "description": "When using multiple databases, provide an alias for each that will appear instead of a fileId when also printing the DB record."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#left_outer_join",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-loj"
                        },
                        "label": "Perform left outer join",
                        "description": "Perform a \"left outer join\". That is, for each feature in A report each overlap with B.  If no overlaps are found, report a NULL feature for B."
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#input_files_b",
                        "required": false,
                        "label": "blacklist file",
                        "description": "BAM/BED/GFF/VCF file(s) \u201cB\u201d.",
                        "sbg:fileTypes": "BAM, BED, GFF, VCF, BED.GZ, broadPeak, narrowPeak.gz, narrowPeak"
                    },
                    {
                        "type": [
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "sbg:stageInput": null,
                        "id": "#input_files_a",
                        "required": true,
                        "label": "Input file A",
                        "description": "BAM/BED/GFF/VCF file \u201cA\u201d. When a BAM file is used for the A file, the alignment is retained if overlaps exist, and exlcuded if an overlap cannot be found. If multiple overlaps exist, they are not reported, as we are only testing for one or more overlaps.",
                        "sbg:fileTypes": "BAM, BED, GFF, VCF, BED.GZ, broadPeak, narrowPeak.gz, narrowPeak"
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "int"
                        ],
                        "id": "#input_buf_size",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "prefix": "-iobuf"
                        },
                        "label": "Input buffer size",
                        "description": "Specify amount of memory to use for input buffer. Takes an integer argument. Optional suffixes K/M/G supported. Note: currently has no effect with compressed files."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#header",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-header"
                        },
                        "label": "Header",
                        "description": "Print the header from the A file prior to results."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#genome_file",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "prefix": "-g"
                        },
                        "label": "Genome file",
                        "description": "Provide a genome file to enforce consistent chromosome sort order across input files. Only applies when used with -sorted option.",
                        "sbg:fileTypes": "TXT",
                        "required": false
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "float"
                        ],
                        "id": "#fraction_b",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "prefix": "-F"
                        },
                        "label": "Fraction of B",
                        "description": "Minimum overlap required as a fraction of B. Default is 1E-9 (i.e., 1bp)."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "float"
                        ],
                        "id": "#fraction_a",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "prefix": "-f"
                        },
                        "label": "Fraction of A",
                        "description": "Minimum overlap required as a fraction of A. Default is 1E-9 (i.e. 1bp)."
                    },
                    {
                        "sbg:category": "Execution",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "id": "#disable_buf_out",
                        "inputBinding": {
                            "separate": false,
                            "sbg:cmdInclude": true,
                            "prefix": "-nobuf"
                        },
                        "label": "Disable buffered output",
                        "description": "Disable buffered output. Using this option will cause each line of output to be printed as it is generated, rather than saved in a buffer. This will make printing large output files noticeably slower, but can be useful in conjunction with other software tools and scripts that need to process one line of bedtools output at a time."
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:cmdPreview": "bedtools intersect  -a /path/to/input_file-a.narrowPeaks  -b /path/to/blacklist_file  -v > input_file-a.blacklisted.narrowPeaks",
                "stdout": {
                    "script": "{\n  if([].concat($job.inputs.input_files_b)[0]){\n    fileB = [].concat($job.inputs.input_files_b)[0]\n  }\n  \n  inputs = [].concat($job.inputs.input_files_a)\n  \n  for (i = 0; i < inputs.length; i++)\n    if(inputs[i]){\n  \tfileA = inputs[i]\n    }\n  //fileA = [].concat($job.inputs.input_files_a)[0]\n  \n  \n  filepath = fileA.path\n  filename = filepath.split(\"/\").pop()\n  basename = filename.substr(0,filename.lastIndexOf(\".\"))\n  \n  if(filename.split('.').pop() == 'gz'){\n    basename = filename.split('.').shift()\n  }\n  \n  \n  file_dot_sep = filename.split(\".\")\n  file_ext = file_dot_sep[file_dot_sep.length-1]\n  if(file_ext == 'gz'){\n    file_ext = file_dot_sep[file_dot_sep.length-2]\n  }\n  \n  sufix_ext = file_ext\n  \n  if ($job.inputs.output_bed && (file_ext == 'bam')) sufix_ext = \"bed\"\n  \n  basename1 = basename\n  filepath = fileA.path\n  filename = filepath.split(\"/\").pop()\n  basename2 = filename.substr(0,filename.lastIndexOf(\".\"))\n  \n  if (($job.inputs.input_files_b) && ($job.inputs.input_files_b.length > 1)) {\n    new_filename = basename1 + \".multi_intersect.\" + sufix_ext\n  } else {\n    new_filename = basename1 + \".blacklisted.\" + sufix_ext\n  }\n  \n  return new_filename;\n}",
                    "class": "Expression",
                    "engine": "#cwl-js-engine"
                },
                "outputs": [
                    {
                        "type": [
                            "File"
                        ],
                        "id": "#output_file",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_file_a",
                            "glob": {
                                "script": "{ \n  inputs = [].concat($job.inputs.input_files_a)\n \n  \n  //fileA = [].concat($job.inputs.input_files_a)[0]\n  fileB = [].concat($job.inputs.input_files_b)[0]\n  \n  if(!$job.inputs.input_files_b){\n    return \"\"\n  }\n  \n  for (i = 0; i < inputs.length; i++)\n    if(inputs[i]){\n  \tfileA = inputs[i]\n    }\n  \n \n  filepath = fileA.path\n  filename = filepath.split(\"/\").pop()\n  basename = filename.substr(0,filename.lastIndexOf(\".\"))\n  \n  if(filename.split('.').pop() == 'gz'){\n    basename = filename.split('.').shift()\n  }\n  \n  \n  file_dot_sep = filename.split(\".\")\n  file_ext = file_dot_sep[file_dot_sep.length-1]\n  if(file_ext == 'gz'){\n    file_ext = file_dot_sep[file_dot_sep.length-2]\n  }\n  \n  sufix_ext = file_ext\n  \n  if ($job.inputs.output_bed && (file_ext == 'bam')) sufix_ext = \"bed\"\n  \n  basename1 = basename\n  filepath = fileA.path\n  filename = filepath.split(\"/\").pop()\n  basename2 = filename.substr(0,filename.lastIndexOf(\".\"))\n  \n  if ($job.inputs.input_files_b.length > 1) {\n    new_filename = basename1 + \".multi_intersect.\" + sufix_ext\n  } else {\n    new_filename = basename1 + \".blacklisted.\" + sufix_ext\n  }\n    \n  return new_filename;\n}",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            }
                        },
                        "label": "Output result file",
                        "sbg:fileTypes": "BAM, BED, GFF, VCF",
                        "description": "After each entry in A, reports: 1) The number of features in B that overlapped the A interval. 2) The number of bases in A that had non-zero coverage. 3) The length of the entry in A. 4) The fraction of bases in A that had non-zero coverage."
                    }
                ],
                "stdin": "",
                "baseCommand": [
                    "bedtools",
                    "intersect"
                ],
                "cwlVersion": "sbg:draft-2",
                "hints": [
                    {
                        "dockerPull": "images.sbgenomics.com/thedzo/bedtools:2.25.0",
                        "dockerImageId": "ad2043b902a2",
                        "class": "DockerRequirement"
                    },
                    {
                        "value": 1,
                        "class": "sbg:CPURequirement"
                    },
                    {
                        "value": 1000,
                        "class": "sbg:MemRequirement"
                    }
                ],
                "class": "CommandLineTool",
                "sbg:createdOn": 1499851056
            },
            "outputs": [
                {
                    "id": "#ChIP_seq_BEDTools_intersect_1.output_file"
                }
            ]
        },
        {
            "inputs": [
                {
                    "source": [
                        "#SBG_Filter_ChIP_seq_BAM_1.aligned_reads"
                    ],
                    "id": "#MACS2.input_bams"
                },
                {
                    "default": "AUTO",
                    "id": "#MACS2.format"
                },
                {
                    "source": [
                        "#Organism"
                    ],
                    "id": "#MACS2.Organism"
                },
                {
                    "id": "#MACS2.buffer_size"
                },
                {
                    "default": "all",
                    "id": "#MACS2.KEEP_DUPS"
                },
                {
                    "source": [
                        "#EXP_NAME"
                    ],
                    "id": "#MACS2.EXP_NAME"
                },
                {
                    "id": "#MACS2.BDG"
                },
                {
                    "id": "#MACS2.verbose"
                },
                {
                    "id": "#MACS2.spmr"
                },
                {
                    "id": "#MACS2.band_width"
                },
                {
                    "id": "#MACS2.MFOLD"
                },
                {
                    "id": "#MACS2.FIX_BIMODAL"
                },
                {
                    "default": true,
                    "id": "#MACS2.NOMODEL"
                },
                {
                    "id": "#MACS2.SHIFT"
                },
                {
                    "source": [
                        "#SBG_ChIP_seq_Select_by_Metadata.output_fraglen"
                    ],
                    "id": "#MACS2.EXTSIZE"
                },
                {
                    "source": [
                        "#QVALUE"
                    ],
                    "id": "#MACS2.QVALUE"
                },
                {
                    "source": [
                        "#PVALUE"
                    ],
                    "id": "#MACS2.PVALUE"
                },
                {
                    "id": "#MACS2.TO_LARGE"
                },
                {
                    "id": "#MACS2.RATIO"
                },
                {
                    "id": "#MACS2.DOWN_SAMPLE"
                },
                {
                    "id": "#MACS2.SEED"
                },
                {
                    "id": "#MACS2.TMPDIR"
                },
                {
                    "id": "#MACS2.NO_LAMBDA"
                },
                {
                    "id": "#MACS2.S_LOCAL"
                },
                {
                    "id": "#MACS2.L_LOCAL"
                },
                {
                    "id": "#MACS2.BROAD"
                },
                {
                    "source": [
                        "#BROAD_CUTOFF"
                    ],
                    "id": "#MACS2.BROAD_CUTOFF"
                },
                {
                    "id": "#MACS2.CUTOFF_ANALYSIS"
                },
                {
                    "id": "#MACS2.CALL_SUMMITS"
                },
                {
                    "id": "#MACS2.FECUTOFF"
                },
                {
                    "id": "#MACS2.TAG_SIZE"
                },
                {
                    "source": [
                        "#analysis_type"
                    ],
                    "id": "#MACS2.analysis_type"
                }
            ],
            "sbg:x": 1936.500686407113,
            "id": "#MACS2",
            "sbg:y": 409.66689650218535,
            "run": {
                "sbg:modifiedOn": 1500029763,
                "id": "https://api.sbgenomics.com/v2/apps/nemanja.vucic/macs2-demo/macs2/2/raw/",
                "sbg:links": [
                    {
                        "label": "Homepage",
                        "id": "https://github.com/taoliu/MACS"
                    },
                    {
                        "label": "Source code",
                        "id": "https://github.com/taoliu/MACS/"
                    },
                    {
                        "label": "Wiki",
                        "id": "https://github.com/taoliu/MACS/wiki/Advanced:-Call-peaks-using-MACS2-subcommands"
                    },
                    {
                        "label": "Download",
                        "id": "https://pypi.python.org/packages/source/M/MACS2/MACS2-2.1.1.20160309.tar.gz"
                    },
                    {
                        "label": "Publication",
                        "id": "https://www.ncbi.nlm.nih.gov/pubmed/18798982"
                    },
                    {
                        "label": "Google Group",
                        "id": "https://groups.google.com/forum/#!forum/macs-announcement"
                    }
                ],
                "sbg:project": "nemanja.vucic/macs2-demo",
                "sbg:toolkit": "MACS",
                "label": "MACS2 2.1.1",
                "sbg:sbgMaintained": false,
                "sbg:revisionNotes": "added toolkit version to Label",
                "successCodes": [],
                "sbg:id": "admin/sbg-public-data/macs2/2",
                "sbg:contributors": [
                    "nemanja.vucic"
                ],
                "temporaryFailCodes": [],
                "sbg:projectName": "MACS2 2.1.1 Demo",
                "description": "Model-based Analysis of ChIP-Seq (**MACS**) is an algorithm for identifying peaks from ChIP-seq data. \n\n**MACS2** performs several steps, ranging from duplicate filtering and peak model building to the actual peak detection and multiple testing corrections. It has also an option to link nearby peaks together in order to call broad peaks.\n\nMACS2 captures the influence of genome complexity to evaluate the significance of enriched ChIP regions and improves the spatial resolution of binding sites through combining the information of both sequencing tag position and orientation. \n    \nMACS2 peak caller is widely used for analysing:\n* narrow histone marks (to be run with MACS2 narrow option): H3K4me3, H3K4me1, H3K27ac.\n* broad histone marks (to be run MACS2 broad option): H3K9me3, H3K27me3, H3K79me2, H3K36me3, H4k20me1.\n* to call enriched regions from DNAase-seq data (to be run MACS2 broad option). \n\n**Outputs:**\n\nThe analysis results consist of the following files:\n\n* **NAME_peaks.xls**: A tabular file which contains information about called peaks: their length, and height, absolute peak summit position, as well as their fold change, p- and q-value. \n\n* **NAME_peaks.narrowPeak**: A BED6+4 format file which contains the peak locations together with peak summit, pvalue and qvalue. \n\n* **NAME\\_summits.bed** a BED format, which contains the peak summits locations for every peaks. The 5th column in this file is -log10pvalue the same as NAME_peaks.bed. If you want to find the motifs at the binding sites, this file is recommended. \n\n* **NAME_peaks.broadPeak** a BED6+3 format which is similar to narrowPeak file, except for missing the 10th column for annotating peak summits.\n\n* **NAME_model.r**: An R script which you can use to produce a PDF image about the model based on the ChIP-seq data. The shape of the modeled peaks allows to assess the quality of the model.\n\n* **NAME\\_treat\\_pileup.bdg**: Extended fragment pileup at every bp saved in a bedGraph file. Files in the bedGraph format can be imported into a genome browser or be converted into even smaller bigWig files. \n\n* **NAME\\_control\\_lambda.bdg**: Local lambda values from control saved in a bedGraph file. Files in the bedGraph format can be imported into a genome browser or be converted into even smaller bigWig files.",
                "sbg:image_url": null,
                "sbg:latestRevision": 2,
                "requirements": [
                    {
                        "requirements": [
                            {
                                "dockerPull": "rabix/js-engine",
                                "class": "DockerRequirement"
                            }
                        ],
                        "class": "ExpressionEngineRequirement",
                        "id": "#cwl-js-engine"
                    }
                ],
                "arguments": [
                    {
                        "valueFrom": {
                            "script": "{\n  input_bams = [].concat($job.inputs.input_bams)\n  \n  \n  cmd = \"\"\n  \n  if(input_bams[0].metadata && input_bams[0].metadata['chip-seq'] == 'sample')\n  {\n  \tcmd += \"-t \" + input_bams[0].path\n    if(input_bams.length > 1)\n  \t{\n  \t\tcmd += \" -c \" + input_bams[1].path\n  \t}\n  }\n  \n\n  else if(input_bams[1].metadata && input_bams[1].metadata['chip-seq'] == 'sample')\n  {\n  \tcmd += \"-t \" + input_bams[1].path\n    if(input_bams.length > 1)\n  \t{\n  \t\tcmd += \" -c \" + input_bams[0].path\n  \t}\n  }\n  \n\treturn cmd\n\n}",
                            "class": "Expression",
                            "engine": "#cwl-js-engine"
                        },
                        "position": 1,
                        "separate": true
                    }
                ],
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "ChIP-seq"
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1499358571,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 0,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1499358703,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 1,
                        "sbg:revisionNotes": null
                    },
                    {
                        "sbg:modifiedOn": 1500029763,
                        "sbg:modifiedBy": "nemanja.vucic",
                        "sbg:revision": 2,
                        "sbg:revisionNotes": "added toolkit version to Label"
                    }
                ],
                "sbg:license": "Artistic License",
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 1024,
                        "cpu": 1
                    },
                    "inputs": {
                        "SEED": null,
                        "BROAD_CUTOFF": null,
                        "EXTSIZE": 120,
                        "FIX_BIMODAL": false,
                        "input_bams": [
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "metadata": {
                                    "sample_id": "CTCF",
                                    "chip-seq": "control"
                                },
                                "path": "/path/to/ENCFF001HID.deduped.filter.srt.bam",
                                "class": "File"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "metadata": {
                                    "sample_id": "CTCF",
                                    "chip-seq": "sample"
                                },
                                "path": "/path/to/ENCFF001EXM.deduped.filter.srt.bam",
                                "class": "File"
                            }
                        ],
                        "DOWN_SAMPLE": false,
                        "NOMODEL": true,
                        "Organism": "hs",
                        "verbose": "3",
                        "QVALUE": "",
                        "buffer_size": null,
                        "TMPDIR": "",
                        "CUTOFF_ANALYSIS": false,
                        "format": "BED",
                        "analysis_type": "narrow peaks",
                        "TAG_SIZE": null,
                        "spmr": false,
                        "CALL_SUMMITS": true,
                        "SHIFT": 0,
                        "band_width": null,
                        "NO_LAMBDA": false,
                        "BROAD": false,
                        "BDG": false,
                        "L_LOCAL": null,
                        "KEEP_DUPS": "all",
                        "PVALUE": "1e-2",
                        "RATIO": null,
                        "TO_LARGE": false,
                        "MFOLD": "",
                        "EXP_NAME": "",
                        "FECUTOFF": null,
                        "S_LOCAL": null
                    }
                },
                "sbg:revision": 2,
                "sbg:toolAuthor": "Yong Zhang, Tao Liu, Xiaole Shirley Liu",
                "hints": [
                    {
                        "value": 1,
                        "class": "sbg:CPURequirement"
                    },
                    {
                        "value": 1024,
                        "class": "sbg:MemRequirement"
                    },
                    {
                        "dockerPull": "images.sbgenomics.com/rejarohit/macs:2",
                        "dockerImageId": "",
                        "class": "DockerRequirement"
                    }
                ],
                "inputs": [
                    {
                        "type": [
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ],
                        "label": "ChIP-seq treatment file.",
                        "description": "ChIP-seq treatment file. If multiple files are given, then they will all be read and pooled together.",
                        "sbg:fileTypes": "AUTO, BAM, SAM, BED, ELAND, ELANDMULTI, ELANDEXPORT, BOWTIE, BAMPE, BEDPE",
                        "id": "#input_bams"
                    },
                    {
                        "type": [
                            {
                                "name": "format",
                                "type": "enum",
                                "symbols": [
                                    "AUTO",
                                    "BAM",
                                    "SAM",
                                    "BED",
                                    "ELAND",
                                    "ELANDMULTI",
                                    "ELANDEXPORT",
                                    "BOWTIE",
                                    "BAMPE",
                                    "BEDPE"
                                ]
                            }
                        ],
                        "sbg:stageInput": null,
                        "id": "#format",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 3,
                            "prefix": "-f"
                        },
                        "label": "Format of Input files",
                        "description": "Format of tag file, \"AUTO\", \"BED\" or \"ELAND\" or \"ELANDMULTI\" or \"ELANDEXPORT\" or \"SAM\" or \"BAM\" or \"BOWTIE\" or \"BAMPE\" or \"BEDPE\". The default AUTO option will let MACS decide which format (except for BAMPE and BEDPE which should be implicitly set) the  file is. Please check the definition in README. Please note that if the format is set as BAMPE or BEDPE,  MACS2 will call its special Paired-end mode to call peaks by piling up the actual ChIPed fragments defined by both aligned ends, instead of predicting the fragment size first and extending reads. Also please note that the BEDPE only contains three columns, and  is NOT the same BEDPE format used by BEDTOOLS."
                    },
                    {
                        "type": [
                            {
                                "name": "Organism",
                                "type": "enum",
                                "symbols": [
                                    "hs",
                                    "mm",
                                    "ce",
                                    "dm"
                                ]
                            }
                        ],
                        "label": "Name of organism to calculate effective genome size. 'hs' for human (2.7e9), 'mm' for mouse   (1.87e9), 'ce' for C. elegans (9e7) and 'dm' for fruitfly (1.2e8)",
                        "id": "#Organism",
                        "description": "Effective genome size. It can be 1.0e+9 or 1000000000, or shortcuts:'hs' for human (2.7e9), 'mm' for mouse  (1.87e9), 'ce' for C. elegans (9e7) and 'dm' for fruitfly (1.2e8).",
                        "inputBinding": {
                            "valueFrom": {
                                "script": "{\n  \nif ($job.inputs.Organism == \"hs\"){\n  \treturn 2.7e9;\n}\n  else if ($job.inputs.Organism == \"mm\"){\n    return 1.87e9;\n    \n  }else if ($job.inputs.Organism == \"ce\"){\n    return 9e7;\n  }\n   else if ($job.inputs.Organism == \"dm\")\n     return 1.2e8;\n\n}",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 4,
                            "prefix": "-g"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:toolDefaultValue": "100000",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 5,
                            "prefix": "--buffer-size"
                        },
                        "label": "Buffer size",
                        "description": "Buffer size for incrementally increasing internal array size to store reads alignment information. In most cases, you don't have to change this parameter. However, if there are large number of chromosomes/contigs/scaffolds in your alignment, it's recommended to specify a smaller buffer size in order to decrease memory usage (but it will take longer time to read alignment files). Minimum memory requested for reading an alignment file is about # of CHROMOSOME *  BUFFER_SIZE * 2 Bytes. DEFAULT: 100000.",
                        "id": "#buffer_size"
                    },
                    {
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:toolDefaultValue": "1",
                        "inputBinding": {
                            "valueFrom": {
                                "script": "{\n  if ($job.inputs.KEEP_DUPS){\n    return $job.inputs.KEEP_DUPS;\n  }\n  else{\n    return 1;\n  }\n}",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 6,
                            "prefix": "--keep-dup"
                        },
                        "label": "Keep duplicates",
                        "description": "It controls the MACS behavior towards duplicate tags at the exact same location -- the same coordination and the same strand. The 'auto' option makes MACS calculate the maximum tags at the exact same location based on binomial distribution using 1e-5 as pvalue cutoff; and the 'all' option keeps every tags. If an integer is given, at most this number of tags will be kept at the same location. Note, if you've used  samtools or picard to flag reads as 'PCR/Optical duplicate' in bit 1024, MACS2 will still read them although the reads may be decided by MACS2 as duplicate later. The default is to keep one tag at the same location.",
                        "id": "#KEEP_DUPS"
                    },
                    {
                        "type": [
                            "null",
                            "string"
                        ],
                        "label": "Experiment name which will be used to name the output files",
                        "id": "#EXP_NAME",
                        "description": "Experiment name, which will be used to generate output ile names. DEFAULT: \"NA\".",
                        "inputBinding": {
                            "valueFrom": {
                                "script": "{\n   input_1 = [].concat($job.inputs.input_bams)[0]\n   input_2 = [].concat($job.inputs.input_bams)[1]\n   \n   if ($job.inputs.EXP_NAME){\n    return $job.inputs.EXP_NAME\n  }\n    else{\n      if (input_1.metadata['chip-seq'] == 'sample'){\n        return input_1.path.split('/').pop().split(\".\")[0]}\n      else{\n        return input_2.path.split('/').pop().split(\".\")[0]}\n    }\n}",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 7,
                            "prefix": "-n"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:stageInput": null,
                        "id": "#BDG",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 8,
                            "prefix": "-B"
                        },
                        "label": "Save extended fragment pileup, and local lambda tracks into a bedgraph file.",
                        "sbg:altPrefix": "--bdg",
                        "description": "Whether or not to save extended fragment pileup, and local lambda tracks (two files) at every bp into a bedGraph file."
                    },
                    {
                        "type": [
                            "null",
                            {
                                "name": "verbose",
                                "type": "enum",
                                "symbols": [
                                    "0",
                                    "1",
                                    "2",
                                    "3"
                                ]
                            }
                        ],
                        "label": "Set verbose level of runtime message",
                        "id": "#verbose",
                        "description": "Set verbose level of runtime message. 0: only show critical message, 1: show additional warning message, 2: show process information, 3: show debug messages. DEFAULT:2.",
                        "inputBinding": {
                            "valueFrom": {
                                "script": "{\n  if ($job.inputs.verbose){\n    return $job.inputs.verbose;\n  }\n  else{\n    return 2;\n  }\n}",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 9,
                            "prefix": "--verbose"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "label": "If True, MACS will save signal per million  reads for fragment pileup profiles",
                        "id": "#spmr",
                        "description": "If True, MACS will save signal per million  reads for fragment pileup profiles. Require -B to be set.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 10,
                            "prefix": "--SPMR"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "int"
                        ],
                        "label": "Band width for picking regions to compute fragment size",
                        "id": "#band_width",
                        "description": "Band width for picking regions to compute fragment size. This value is only used while building the shifting model.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 11,
                            "prefix": "--bw"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "string"
                        ],
                        "label": "Select the regions within MFOLD range of high-confidence enrichment ratio against background to build model",
                        "id": "#MFOLD",
                        "description": "Select the regions within MFOLD range of high-confidence enrichment ratio against background to build model. Fold-enrichment in regions must be lower than upper limit, and higher than the lower limit. Use as \"-m 10 30\". DEFAULT:5 50.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 12,
                            "prefix": "-m"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "label": "Whether turn on the auto pair model process",
                        "id": "#FIX_BIMODAL",
                        "description": "Whether turn on the auto pair model process. If set, when MACS failed to build paired model, it will use the nomodel settings, the --exsize parameter to extend each tags towards 3' direction. Not to use this  automate fixation is a default behavior now. DEFAULT: False.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 13,
                            "prefix": "--fix-bimodal"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "label": "Whether or not to build the shifting model",
                        "id": "#NOMODEL",
                        "description": "If True: do not to build the shifting model. If True, MACS will not build model. by default it means shifting size = 100, try to set extsize to change it. DEFAULT: False.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 14,
                            "prefix": "--nomodel"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "int"
                        ],
                        "label": "(NOT the legacy --shiftsize option!) The arbitrary shift in bp",
                        "id": "#SHIFT",
                        "description": "(NOT the legacy --shiftsize option!) The arbitrary shift in bp. Use discretion while setting it other than default value. When NOMODEL is set, MACS will use this value to move cutting ends (5') towards 5'->3' direction then apply EXTSIZE to extend them to  fragments. When this value is negative, ends will be moved toward 3'->5' direction. Recommended to keep it as default 0 for ChIP-Seq datasets, or -1 * half of EXTSIZE together with EXTSIZE option for detecting enriched cutting loci such as certain DNAseI-Seq datasets. Note, you can't set values other than 0 if format is BAMPE or BEDPE for paired-end data. DEFAULT: 0.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 29,
                            "prefix": "--shift"
                        }
                    },
                    {
                        "sbg:includeInPorts": true,
                        "type": [
                            "null",
                            "int"
                        ],
                        "id": "#EXTSIZE",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 15,
                            "prefix": "--extsize"
                        },
                        "label": "The arbitrary extension size in bp",
                        "description": "The arbitrary extension size in bp. When nomodel is true, MACS will use this value as fragment size to extend each read towards 3' end, then pile them up. It's exactly twice the number of obsolete SHIFTSIZE. In previous language, each read is moved 5'->3'  direction to middle of fragment by 1/2 d, then extended to both direction with 1/2 d. This is equivalent to say each read is extended towards 5'->3'  into a d size fragment. DEFAULT: 200. EXTSIZE and SHIFT can be combined when necessary. Check SHIFT option."
                    },
                    {
                        "type": [
                            "null",
                            "string"
                        ],
                        "id": "#QVALUE",
                        "inputBinding": {
                            "valueFrom": {
                                "script": "{\n\t//return $job.inputs.QVALUE\n    //return $job.inputs.analysis_type == 'narrow peaks'\n  \n  if ($job.inputs.QVALUE){\n  \treturn $job.inputs.QVALUE\n  }\n  else {\n    if ($job.inputs.analysis_type == 'narrow peaks'){\n      return 0.01\n    }\n    else if ($job.inputs.analysis_type == 'broad peaks'){\n      return 0.05\n    }\n  }\n}",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 30,
                            "prefix": "-q"
                        },
                        "label": "Minimum FDR (q-value) cutoff for peak detection",
                        "sbg:altPrefix": "--qvalue",
                        "description": "Minimum FDR (q-value) cutoff for peak detection. DEFAULT: 0.05. -q, and -p are mutually exclusive."
                    },
                    {
                        "type": [
                            "null",
                            "string"
                        ],
                        "id": "#PVALUE",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 16,
                            "prefix": "-p"
                        },
                        "label": "Pvalue cutoff for peak detection",
                        "sbg:altPrefix": "--pvalue",
                        "description": "Pvalue cutoff for peak detection. DEFAULT: not set. -q, and -p are mutually exclusive. If pvalue cutoff is set, qvalue will not be calculated and reported as -1 in the final .xls file."
                    },
                    {
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "label": "When set, scale the small sample up to the bigger sample",
                        "id": "#TO_LARGE",
                        "description": "When set, scale the small sample up to the bigger sample. By default, the bigger dataset will be scaled down towards the smaller dataset, which will lead to smaller p/qvalues and more specific results. Keep in mind that scaling down will bring down background noise more. DEFAULT: False.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 17,
                            "prefix": "--to-large"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "float"
                        ],
                        "label": "When set, use a custom scaling ratio of ChIP/control (e.g. calculated using NCIS) for linear scaling",
                        "id": "#RATIO",
                        "description": "When set, use a custom scaling ratio of ChIP/control (e.g. calculated using NCIS) for linear scaling. DEFAULT: ingore.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 18,
                            "prefix": "--ratio"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "label": "When set, random sampling method will scale down the bigger sample",
                        "id": "#DOWN_SAMPLE",
                        "description": "When set, random sampling method will scale down the bigger sample. By default, MACS uses linear scaling. Warning: This option will make your result unstable and irreproducible since each time, random reads would  be selected. Consider to use 'randsample' script instead. <not implmented>If used together with --SPMR, 1 million unique reads will be randomly picked.</not implemented> Caution: due to the implementation, the final number of selected reads may not be as you expected! DEFAULT: False.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 19,
                            "prefix": "--down-sample"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "int"
                        ],
                        "label": "Set the random seed while down sampling data",
                        "id": "#SEED",
                        "description": "Set the random seed while down sampling data. Must be a non-negative integer in order to be effective. DEFAULT: not set.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 19,
                            "prefix": "--seed"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "string"
                        ],
                        "label": "Optional directory to store temp files",
                        "id": "#TMPDIR",
                        "description": "Optional directory to store temp files.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 20,
                            "prefix": "--tempdir"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:stageInput": null,
                        "id": "#NO_LAMBDA",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 21,
                            "prefix": "--nolambda"
                        },
                        "label": "If True, MACS will use fixed background lambda as local lambda for every peak region",
                        "description": "If True, MACS will use fixed background lambda as local lambda for every peak region. Normally, MACS  calculates a dynamic local lambda to reflect the local bias due to potential chromatin structure."
                    },
                    {
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:stageInput": null,
                        "id": "#S_LOCAL",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 22,
                            "prefix": "--slocal"
                        },
                        "label": "The small nearby region in basepairs to calculate dynamic lambda",
                        "description": "The small nearby region in basepairs to calculate dynamic lambda. This is used to capture the bias near the peak summit region. Invalid if there is no control data. If you set this to 0, MACS will skip slocal lambda calculation. *Note* that MACS will always perform a d-size local lambda calculation. The final local bias should be the maximum of the lambda value from d, slocal, and llocal size windows. DEFAULT: 1000."
                    },
                    {
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:stageInput": null,
                        "id": "#L_LOCAL",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 23,
                            "prefix": "--llocal"
                        },
                        "label": "The large nearby region in basepairs to calculate dynamic lambda",
                        "description": "The large nearby region in basepairs to calculate dynamic lambda. This is used to capture the surround bias. If you set this to 0, MACS will skip llocal lambda calculation. *Note* that MACS will always perform a d-size local lambda calculation. The final local bias should be the maximum of the lambda value from d, slocal, and llocal size windows. DEFAULT: 10000."
                    },
                    {
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "label": "If set, MACS will try to call broad peaks by linking nearby highly enriched regions",
                        "id": "#BROAD",
                        "description": "If set, MACS will try to call broad peaks by linking nearby highly enriched regions. The linking region is controlled by another cutoff through --linking-cutoff. The maximum linking region length is 4 times of d from MACS. DEFAULT: False.",
                        "inputBinding": {
                            "valueFrom": {
                                "script": "{\n  if ($job.inputs.analysis_type == 'broad peaks'){\n    return true\n  }\n  else {\n    return $job.inputs.BROAD\n  }\n}",
                                "class": "Expression",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 24,
                            "prefix": "--broad"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "float"
                        ],
                        "label": "Cutoff for broad region",
                        "id": "#BROAD_CUTOFF",
                        "description": "Cutoff for broad region. This option is not available unless --broad is set. If -p is set, this is a pvalue cutoff, otherwise, it's a qvalue cutoff. DEFAULT: 0.1.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 25,
                            "prefix": "--broad-cutoff"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "label": "While set, MACS2 will analyze number or total length of peaks that can be called by different p-value cutoff then output a summary table to help user decide a better cutoff",
                        "id": "#CUTOFF_ANALYSIS",
                        "description": "While set, MACS2 will analyze number or total length of peaks that can be called by different p-value cutoff then output a summary table to help user decide a better cutoff. The table will be saved in NAME_cutoff_analysis.txt file. Note, minlen and maxgap may affect the results. WARNING: May take ~30 folds longer time to finish. DEFAULT: False.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 26,
                            "prefix": "--cutoff-analysis"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "label": "If set, MACS will use a more sophisticated signal processing approach to find subpeak summits in each enriched peak region",
                        "id": "#CALL_SUMMITS",
                        "description": "If set, MACS will use a more sophisticated signal processing approach to find subpeak summits in each enriched peak region. DEFAULT: False.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 27,
                            "prefix": "--call-summits"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "float"
                        ],
                        "label": "When set, the value will be used to filter out peaks with low fold-enrichment",
                        "id": "#FECUTOFF",
                        "description": "When set, the value will be used to filter out peaks with low fold-enrichment. Note, MACS2 use 1.0 as pseudocount while calculating fold-enrichment. DEFAULT: 1.0.",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 28,
                            "prefix": "--fe-cutoff"
                        }
                    },
                    {
                        "type": [
                            "null",
                            "int"
                        ],
                        "id": "#TAG_SIZE",
                        "inputBinding": {
                            "separate": true,
                            "sbg:cmdInclude": true,
                            "position": 31,
                            "prefix": "-s"
                        },
                        "label": "Tag Size",
                        "sbg:altPrefix": "--tsize",
                        "description": "Tag size. This will override the auto detected tag size. DEFAULT: Not set."
                    },
                    {
                        "type": [
                            {
                                "name": "analysis_type",
                                "type": "enum",
                                "symbols": [
                                    "narrow peaks",
                                    "broad peaks"
                                ]
                            }
                        ],
                        "label": "Analysis type",
                        "description": "If 'narrow peaks' is selected, MACS2 will try to call narrow peaks and q-value parameter will be set to default value of 0.01. If 'broad peaks' is selected, MACS2 will try to call broad peaks and q-value parameter will be set to default value of 0.05.",
                        "id": "#analysis_type"
                    }
                ],
                "sbg:modifiedBy": "nemanja.vucic",
                "sbg:cmdPreview": "macs2 callpeak  -t /path/to/ENCFF001EXM.deduped.filter.srt.bam -c /path/to/ENCFF001HID.deduped.filter.srt.bam -f BED -g 2700000000",
                "stdout": "",
                "outputs": [
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "label": "Excel file",
                        "id": "#EXCEL_FILE",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#INPUT_BAMS",
                            "glob": "*.xls"
                        },
                        "description": "Excel file."
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "label": "Summit bed file",
                        "id": "#SUMMIT_BED_FILE",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#INPUT_BAMS",
                            "glob": "*.bed"
                        },
                        "description": "Summit bed file."
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#NARROW_PEAK",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#INPUT_BAMS",
                            "glob": "*.narrowPeak"
                        },
                        "label": "Narrow Peak file",
                        "sbg:fileTypes": "narrowPeak",
                        "description": "BED6+4 format file which contains the peak locations together with peak summit, pvalue and qvalue."
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#Rmodel",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#INPUT_BAMS",
                            "glob": "*model.r"
                        },
                        "label": "Rmodel",
                        "sbg:fileTypes": "R",
                        "description": "R script which you can use to produce a PDF image about the model based on your data."
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#BROAD_PEAK",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#INPUT_BAMS",
                            "glob": "*.broadPeak"
                        },
                        "label": "Broad Peak file",
                        "sbg:fileTypes": "broadPeak",
                        "description": "BED6+3 format which is similar to narrowPeak file, except for missing the 10th column for annotating peak summits."
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#SAMPLE_BDG",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#INPUT_BAMS",
                            "glob": "*treat_pileup.bdg"
                        },
                        "label": "Sample pileup",
                        "sbg:fileTypes": "BDG",
                        "description": "Extended fragment pileup at every bp saved in a bedGraph file."
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "id": "#CONTROL_BDG",
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#CONTROL_BAM",
                            "glob": "*control_lambda.bdg"
                        },
                        "label": "Control lambda values",
                        "sbg:fileTypes": "BDG",
                        "description": "Local lambda values from control saved in a bedGraph file."
                    }
                ],
                "stdin": "",
                "baseCommand": [
                    "macs2",
                    "callpeak"
                ],
                "sbg:createdBy": "nemanja.vucic",
                "cwlVersion": "sbg:draft-2",
                "sbg:toolkitVersion": "2.1.1",
                "class": "CommandLineTool",
                "sbg:createdOn": 1499358571
            },
            "outputs": [
                {
                    "id": "#MACS2.EXCEL_FILE"
                },
                {
                    "id": "#MACS2.SUMMIT_BED_FILE"
                },
                {
                    "id": "#MACS2.NARROW_PEAK"
                },
                {
                    "id": "#MACS2.Rmodel"
                },
                {
                    "id": "#MACS2.BROAD_PEAK"
                },
                {
                    "id": "#MACS2.SAMPLE_BDG"
                },
                {
                    "id": "#MACS2.CONTROL_BDG"
                }
            ]
        }
    ],
    "requirements": [],
    "sbg:canvas_zoom": 0.5999999999999996,
    "sbg:revisionNotes": "added toolkit version to the Label",
    "sbg:copyOf": "admin/sbg-public-data/chip-seq-bwa-alignment-and-peak-calling-macs2/3",
    "sbg:modifiedBy": "bixqa",
    "sbg:id": "bixqa/qa-load-2017-07-31-18/chip-seq-bwa-alignment-and-peak-calling-macs2/3",
    "sbg:contributors": [
        "bixqa"
    ],
    "sbg:modifiedOn": 1501518615,
    "sbg:projectName": "qa-load-2017-07-31-18",
    "description": "**Description:**\n\nChIP-seq (Chromatin immunoprecipitation followed by high-throughput sequencing technology) allows researchers to study the landscape of chromatin modifications or the binding patterns of transcription factors or other chromatin-associated proteins like RNA-polymerases or chromatin modulators.\n\nThis pipeline takes you all the way from unaligned raw sequencing reads (FASTQ) to ChIP-seq peaks detection.\n\nSequenced reads are aligned with the BWA-backtrack tool (specific for read size < 100bp). After alignment, BAM files are filtered (to remove low quality and unpaired reads) and de-duplicated. A number of QC metrics that evaluate the library complexity and signal enrichment are calculated. Peak calling is performed on the processed BAM files using MACS2 which is suggested peak caller for analysing histone marks.\n\n**Blacklisting**\n\nPrior to further ChIP-seq analysis, genomic regions that are associated with artifact signal may be removed. The exclusion of these regions, called blacklist regions, aims to remove sources of artifact signal caused by biases from chromatin accessibility and ambiguous alignment in order to increase the accuracy of both peak calling and comparative ChIP analysis. BEDTools intersect tool within this pipeline, when provided with adequate blacklist file, will remove blacklisted regions from peak MACS2 output file. \n\n**Required inputs:**\n\n* Input FASTQ reads (ID: Sample FASTQ) - one fastq file per sample for single-end data, or two files per sample for paired-end data. NOTE: For paired-end reads, it is crucial to set the metadata 'paired-end' field as 1 for one input file, as 2 for the other input file.\n\n* Reference/Index files (ID: Reference index) - TAR bundle of already generated index files.\n\n**Optional input:**\n\n* Control FASTQ reads (ID: Control FASTQ) - one fastq file per sample for single-end data, or two files per sample for paired-end data. NOTE: For paired-end reads, it is crucial to set the metadata 'paired-end' field as 1 for one input file, as 2 for the other input file.\n\n* Blacklist file (ID: Blacklist file) - BED file containing genomic regions that have anomalous, unstructured, high signal/ read counts in next-generation sequencing experiments. It could be used by BEDTools intersect in order to increase the accuracy of peak calling and comparative ChIP analysis.\n\n**Outputs:**\n\n* Sample_FASTQ.qc.b64html: B64HTML file with a table of all QC metrics.\n\n* Sample_FASTQ_deduped.filter.srt.bam: BAM files that are filtered (to remove low quality and unpaired reads) and de-duplicated. Could be used independently in other analyses for peak calling with different peak-callers.\n\n* Control_FASTQ_deduped.filter.srt.bam: BAM files that are filtered (to remove low quality and unpaired reads) and de-duplicated. Could be used independently in other analyses for peak calling with different peak-callers.\n\n* Sample_FASTQ_deduped.filter.srt.bam.bai: BAI index files for processed BAM files. Could be used independently in other analyses for peak calling with different peak-callers.\n\n* Control_FASTQ_deduped.filter.srt.bam.bai: BAI index files for processed BAM files. Could be used independently in other analyses for peak calling with different peak-callers.\n\n* Sample_FASTQ_peaks.xls: A tabular file which contains information about called peaks: their length, and height, absolute peak summit position, as well as their fold change, p- and q-value.\n\n* Sample_FASTQ_summits.bed: a BED format, which contains the peak summits locations for every peak. If you want to find the motifs at the binding sites, this file is recommended.\n\n* Sample_FASTQ_peaks.narrowPeak: a BED6+4 format file which contains the peak locations together with peak summit, pvalue and qvalue. \n\n* Sample_FASTQ_peaks.broadPeak: a BED6+3 format which contains the peak locations together with peak summit, p-value and q-value, missing the column for annotating peak summits.\n\n* Sample_FASTQ_model.r: An R script which you can use to produce a PDF image about the model based on the ChIP-seq data. The shape of the modeled peaks allows assessment of the model quality.\n\n* Sample_FASTQ_peaks.blacklisted.(narrowPeak/broadPeak): Sample_FASTQ_peaks.broadPeak or Sample_FASTQ_peaks.narrowPeak file without blacklist regions.(This file is not created if Blacklist file is not provided on input).\n\n**Important notes:**\n\n* **When providing sample and control files to the input, a \"Case ID\" and \"Sample ID\" metadata should be properly set for each file. PE files should have the same \"Sample ID\" and \"Case ID\". Sample files (PE or SE) should have the same \"Case ID\" as corresponding control files.**\n\n* In some situations read trimming is necessary, for instance, if read ends display poor quality values (most generally the right end) or if control and ChIP-seq samples have inconsistent read lengths. In these cases, read-trimming should be applied.\n\n* Preferred values for library complexity are: NRF > 0.9, PBC1 \u2265 0.9, and PBC2 \u2265 10.\n\n* Preferred values for NSC and RSC metric when analysing sharp histone marks are: NSC > 1.05 and RSC > 0.8 (Even if ChIP-seq data does not meet these guidelines, there could still be significant biological information. The users should evaluate the profiles of the cross-correlation plot to further assess the quality of their data).\n\n* Input and negative control samples should have low NSC and RSC scores.\n\n* SBG ChIP-seq Set Metadata tool adds values 'control' and 'sample' to the metadata field 'chip-seq' of the input files.\n\n**Important notes about MACS2 options:**\n\n**NO_MODEL:** MACS2 uses the estimated fragment length to extend reads in 5'->3' direction to fix-sized fragments. By default, the fragment length is estimated by MACS2 during the model building step. However, for this specific workflow (following ENCODE pipeline), the predominant fragment length is estimated in a previous step (*json output; from 'SBG ChIP-seq filter and QC') and used as an input via the parameter --extsize*. The parameter --nomodel is also set to force MACS2 to bypass the building of the shifting model.\n\n**QVALUE:** The q-value is set to 0.05 to allow for peak calling. Recommended q-values are 0.01 for narrow peaks and 0.05 for broad peaks.Please use the p-value option (leaving the q-value option blank) if a less stringent analysis is needed. Peak sets thresholded in this way (p < 0.01) are not meant to be interpreted as definitive binding events but are rather intended to be used as input for subsequent statistical comparison of replicates which could be done by using IDR public app. \n\n**KEEP_DUPS:** This is set to 'all' to prevent MACS2 from searching for duplicated reads because we expect the input BAM files to be de-duplicated. If this is not the case, this parameter should be set to the default option '1' which is to keep one tag at the same location.\n\n**Common issues and limitations of the workflow:**\n\n* If the total size of the input files in a single task exceeds 20GB perhaps a larger instance should be allocated due to memory requirements.\n\n* In cases when analysing two biological replicates, outputs between replicates should be compared using IDR app. IDR is publicly available app which could be used to combine outputs of chip-seq pipeline in order to perform the comparative analysis between biological replicates in the separate task.",
    "sbg:image_url": "https://brood.sbgenomics.com/static/bixqa/qa-load-2017-07-31-18/chip-seq-bwa-alignment-and-peak-calling-macs2/3.png",
    "sbg:latestRevision": 3,
    "sbg:canvas_x": -521,
    "sbg:appVersion": [
        "sbg:draft-2"
    ],
    "sbg:validationErrors": [],
    "sbg:revisionsInfo": [
        {
            "sbg:modifiedOn": 1501518615,
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 0,
            "sbg:revisionNotes": null
        },
        {
            "sbg:modifiedOn": 1501518615,
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 1,
            "sbg:revisionNotes": "all apps added from demo projects"
        },
        {
            "sbg:modifiedOn": 1501518615,
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 2,
            "sbg:revisionNotes": "added suggested files to reference_tsv_tar and blacklist_file input nodes, changed description, batching by sampleID enabled on sample_files input node."
        },
        {
            "sbg:modifiedOn": 1501518615,
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 3,
            "sbg:revisionNotes": "added toolkit version to the Label"
        }
    ],
    "sbg:project": "bixqa/qa-load-2017-07-31-18",
    "outputs": [
        {
            "sbg:includeInPorts": true,
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#MACS2.BROAD_PEAK"
            ],
            "id": "#BROAD_PEAK",
            "required": false,
            "label": "BROAD_PEAK",
            "sbg:x": 2489.167172445207,
            "sbg:y": 446.66674538453657,
            "sbg:fileTypes": "broadPeak"
        },
        {
            "sbg:includeInPorts": true,
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#MACS2.NARROW_PEAK"
            ],
            "id": "#NARROW_PEAK",
            "required": false,
            "label": "NARROW_PEAK",
            "sbg:x": 2249.16683732139,
            "sbg:y": 409.99999588065594,
            "sbg:fileTypes": "narrowPeak"
        },
        {
            "sbg:includeInPorts": true,
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#MACS2.Rmodel"
            ],
            "id": "#Rmodel",
            "required": false,
            "label": "Rmodel",
            "sbg:x": 2140.8338252041235,
            "sbg:y": 395.0000258021888,
            "sbg:fileTypes": "R"
        },
        {
            "sbg:includeInPorts": true,
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#MACS2.EXCEL_FILE"
            ],
            "id": "#EXCEL_FILE",
            "required": false,
            "label": "EXCEL_FILE",
            "sbg:x": 2366.6670860317145,
            "sbg:y": 422.50003710058076
        },
        {
            "sbg:includeInPorts": true,
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#SAMtools_Index_BAM.generated_index"
            ],
            "id": "#generated_index",
            "required": false,
            "label": "BAM index",
            "sbg:x": 1234.1670020951276,
            "sbg:y": 575.0001247061663,
            "sbg:fileTypes": "BAI"
        },
        {
            "sbg:includeInPorts": true,
            "type": [
                "null",
                {
                    "name": "report_zip",
                    "type": "array",
                    "items": "File"
                }
            ],
            "source": [
                "#ChIP_seq_FastQC_1.report_zip"
            ],
            "id": "#report_zip_1",
            "required": false,
            "label": "report_zip",
            "sbg:x": 777.5001944833394,
            "sbg:y": 477.5001615550839,
            "sbg:fileTypes": "ZIP"
        },
        {
            "sbg:includeInPorts": true,
            "type": [
                "File"
            ],
            "source": [
                "#SBG_Filter_ChIP_seq_BAM_1.aligned_reads"
            ],
            "id": "#aligned_reads_1",
            "required": true,
            "label": "filtered BAM",
            "sbg:x": 1230.0002516971772,
            "sbg:y": 447.50014024973393,
            "sbg:fileTypes": "BAM"
        },
        {
            "sbg:includeInPorts": true,
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#SBG_Merge_ChIP_seq_QC_metrics_1.output_json"
            ],
            "id": "#output_json_1",
            "required": false,
            "label": "output_json",
            "sbg:x": 1683.3338886698277,
            "sbg:y": 124.99999674161243,
            "sbg:fileTypes": "JSON"
        },
        {
            "sbg:includeInPorts": true,
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#SBG_Merge_ChIP_seq_QC_metrics_1.b64html"
            ],
            "id": "#b64html_1",
            "required": false,
            "label": "b64html",
            "sbg:x": 1684.167181776647,
            "sbg:y": 296.66675963667024,
            "sbg:fileTypes": "HTML, B64HTML"
        },
        {
            "sbg:includeInPorts": true,
            "type": [
                "File"
            ],
            "source": [
                "#ChIP_seq_BEDTools_intersect_1.output_file"
            ],
            "id": "#output_file_1",
            "required": true,
            "label": "final_output",
            "sbg:x": 2492.500261836588,
            "sbg:y": 573.3333357705002,
            "sbg:fileTypes": "BAM, BED, GFF, VCF"
        }
    ],
    "sbg:toolAuthor": "Milos Jordanski, Seven Bridges",
    "sbg:createdBy": "bixqa",
    "inputs": [
        {
            "type": [
                "null",
                {
                    "type": "array",
                    "items": "File"
                }
            ],
            "label": "control_files",
            "sbg:x": -53.60525096543294,
            "sbg:y": 398.44748447792267,
            "id": "#control_files"
        },
        {
            "type": [
                "null",
                {
                    "type": "array",
                    "items": "File"
                }
            ],
            "id": "#sample_files",
            "batchType": [
                "metadata.sample_id"
            ],
            "label": "sample_files",
            "sbg:x": -54.82455662189147,
            "sbg:y": 249.2807615179786,
            "sbg:fileTypes": "FQ, FQ.GZ, FASTQ, FASTQ.GZ"
        },
        {
            "sbg:includeInPorts": true,
            "type": [
                "File"
            ],
            "sbg:suggestedValue": {
                "name": "GRCh38_no_alt_analysis_set_GCA_000001405.15.fasta.gz.tar",
                "path": "5966888f507c173d3c9f81de",
                "class": "File"
            },
            "id": "#reference_index_tar",
            "label": "reference_index_tar",
            "sbg:x": 423.00003463692207,
            "sbg:y": 112.50002894136739,
            "sbg:fileTypes": "TAR, TAR.GZ"
        },
        {
            "type": [
                "null",
                "File"
            ],
            "sbg:suggestedValue": {
                "name": "hg38.blacklist.bed.gz",
                "path": "59676f24507c174f55abe074",
                "class": "File"
            },
            "id": "#input_files_b_1",
            "label": "blacklist_file",
            "sbg:x": 1937.500237796048,
            "sbg:y": 543.3334768613228,
            "sbg:fileTypes": "BAM, BED, GFF, VCF, BED.GZ, broadPeak, narrowPeak.gz, narrowPeak"
        },
        {
            "sbg:category": "CPU",
            "type": [
                "null",
                "int"
            ],
            "sbg:suggestedValue": 16,
            "id": "#threads",
            "sbg:toolDefaultValue": "8",
            "label": "Number of threads",
            "description": "Number of threads for BWA aln.",
            "required": false
        },
        {
            "type": [
                {
                    "name": "analysis_type",
                    "type": "enum",
                    "symbols": [
                        "narrow peaks",
                        "broad peaks"
                    ]
                }
            ],
            "label": "Analysis type",
            "description": "If 'narrow peaks' is selected, MACS2 will try to call narrow peaks and q-value parameter will be set to default value of 0.01. If 'broad peaks' is selected, MACS2 will try to call broad peaks and q-value parameter will be set to default value of 0.05.",
            "id": "#analysis_type"
        },
        {
            "type": [
                "null",
                "string"
            ],
            "label": "Minimum FDR (q-value) cutoff for peak detection",
            "sbg:altPrefix": "--qvalue",
            "id": "#QVALUE",
            "description": "Minimum FDR (q-value) cutoff for peak detection. DEFAULT: 0.05. -q, and -p are mutually exclusive."
        },
        {
            "type": [
                "null",
                "string"
            ],
            "label": "Pvalue cutoff for peak detection",
            "sbg:altPrefix": "--pvalue",
            "id": "#PVALUE",
            "description": "Pvalue cutoff for peak detection. DEFAULT: not set. -q, and -p are mutually exclusive. If pvalue cutoff is set, qvalue will not be calculated and reported as -1 in the final .xls file."
        },
        {
            "type": [
                {
                    "name": "Organism",
                    "type": "enum",
                    "symbols": [
                        "hs",
                        "mm",
                        "ce",
                        "dm"
                    ]
                }
            ],
            "label": "Name of organism to calculate effective genome size. 'hs' for human (2.7e9), 'mm' for mouse   (1.87e9), 'ce' for C. elegans (9e7) and 'dm' for fruitfly (1.2e8)",
            "sbg:suggestedValue": "hs",
            "description": "Effective genome size. It can be 1.0e+9 or 1000000000, or shortcuts:'hs' for human (2.7e9), 'mm' for mouse  (1.87e9), 'ce' for C. elegans (9e7) and 'dm' for fruitfly (1.2e8).",
            "id": "#Organism"
        },
        {
            "sbg:includeInPorts": false,
            "type": [
                "null",
                "string"
            ],
            "id": "#EXP_NAME",
            "required": false,
            "label": "Experiment name which will be used to name the output files",
            "description": "Experiment name, which will be used to generate output ile names. DEFAULT: \"NA\"."
        },
        {
            "type": [
                "null",
                "float"
            ],
            "label": "Cutoff for broad region",
            "description": "Cutoff for broad region. This option is not available unless --broad is set. If -p is set, this is a pvalue cutoff, otherwise, it's a qvalue cutoff. DEFAULT: 0.1.",
            "id": "#BROAD_CUTOFF"
        }
    ],
    "sbg:batchBy": {
        "type": "criteria",
        "criteria": [
            "metadata.sample_id"
        ]
    },
    "sbg:canvas_y": 100,
    "cwlVersion": "sbg:draft-2",
    "hints": [
        
    ],
    "class": "Workflow",
    "sbg:createdOn": 1501518615
}
