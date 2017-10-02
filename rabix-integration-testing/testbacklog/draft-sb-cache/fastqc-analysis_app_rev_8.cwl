{
    "sbg:revisionsInfo": [
        {
            "sbg:modifiedBy": "bixqa",
            "sbg:revisionNotes": null,
            "sbg:revision": 0,
            "sbg:modifiedOn": 1501518627
        },
        {
            "sbg:modifiedBy": "bixqa",
            "sbg:revisionNotes": "Tool updates.",
            "sbg:revision": 1,
            "sbg:modifiedOn": 1501518627
        },
        {
            "sbg:modifiedBy": "bixqa",
            "sbg:revisionNotes": "Changed output names.",
            "sbg:revision": 2,
            "sbg:modifiedOn": 1501518627
        },
        {
            "sbg:modifiedBy": "bixqa",
            "sbg:revisionNotes": "Changed output names.",
            "sbg:revision": 3,
            "sbg:modifiedOn": 1501518627
        },
        {
            "sbg:modifiedBy": "bixqa",
            "sbg:revisionNotes": "Updated with the new version of FastQC.",
            "sbg:revision": 4,
            "sbg:modifiedOn": 1501518627
        },
        {
            "sbg:modifiedBy": "bixqa",
            "sbg:revisionNotes": "Description added.",
            "sbg:revision": 5,
            "sbg:modifiedOn": 1501518627
        },
        {
            "sbg:modifiedBy": "bixqa",
            "sbg:revisionNotes": "Tool update.",
            "sbg:revision": 6,
            "sbg:modifiedOn": 1501518627
        },
        {
            "sbg:modifiedBy": "bixqa",
            "sbg:revisionNotes": "Tool update (Revision 9) and pulled the inputs for limits_file, contaminants_file and adapters_file so they can be specified for the workflow.",
            "sbg:revision": 7,
            "sbg:modifiedOn": 1501518627
        },
        {
            "sbg:modifiedBy": "bixqa",
            "sbg:revisionNotes": "Updated FastQC to revision 10.",
            "sbg:revision": 8,
            "sbg:modifiedOn": 1501518627
        }
    ],
    "sbg:latestRevision": 8,
    "sbg:modifiedOn": 1501518627,
    "sbg:sbgMaintained": false,
    "requirements": [],
    "steps": [
        {
            "scatter": "#SBG_Html2b64_1.input_file",
            "sbg:y": 271.66673882802587,
            "sbg:x": 571.6667501529085,
            "inputs": [
                {
                    "id": "#SBG_Html2b64_1.input_file",
                    "source": [
                        "#FastQC.report_zip"
                    ]
                }
            ],
            "id": "#SBG_Html2b64_1",
            "run": {
                "sbg:job": {
                    "inputs": {
                        "input_file": {
                            "size": 0,
                            "secondaryFiles": [],
                            "class": "File",
                            "path": "input_file.ext"
                        }
                    },
                    "allocatedResources": {
                        "cpu": 1,
                        "mem": 1000
                    }
                },
                "y": 271.66673882802587,
                "sbg:modifiedOn": 1459963571,
                "sbg:sbgMaintained": false,
                "stdin": "",
                "requirements": [],
                "temporaryFailCodes": [],
                "successCodes": [],
                "sbg:createdOn": 1450911294,
                "sbg:modifiedBy": "bix-demo",
                "sbg:contributors": [
                    "bix-demo"
                ],
                "description": "Tool for converting archived html output of FastQC and similar tools to b64html so it can easily be displayed in web browsers or on SBG platform.",
                "inputs": [
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "inputBinding": {
                            "separate": true,
                            "prefix": "--input",
                            "sbg:cmdInclude": true
                        },
                        "label": "Input file",
                        "description": "Compressed archive.",
                        "id": "#input_file",
                        "sbg:fileTypes": "ZIP",
                        "sbg:category": "File input.",
                        "required": false
                    }
                ],
                "x": 571.6667501529085,
                "stdout": "",
                "sbg:toolAuthor": "Seven Bridges",
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revisionNotes": null,
                        "sbg:revision": 0,
                        "sbg:modifiedOn": 1450911294
                    },
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revisionNotes": null,
                        "sbg:revision": 1,
                        "sbg:modifiedOn": 1450911294
                    },
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revisionNotes": null,
                        "sbg:revision": 2,
                        "sbg:modifiedOn": 1459963571
                    }
                ],
                "sbg:createdBy": "bix-demo",
                "sbg:project": "bix-demo/sbgtools-demo",
                "class": "CommandLineTool",
                "label": "SBG Html2b64",
                "sbg:image_url": null,
                "sbg:latestRevision": 2,
                "sbg:toolkit": "SBGTools",
                "sbg:categories": [
                    "Converters",
                    "Plotting-and-Rendering"
                ],
                "sbg:license": "Apache License 2.0",
                "sbg:toolkitVersion": "1.0",
                "sbg:validationErrors": [],
                "baseCommand": [
                    "python",
                    "/opt/sbg_html_to_b64.py"
                ],
                "sbg:revision": 2,
                "hints": [
                    {
                        "dockerPull": "images.sbgenomics.com/mladenlsbg/sbg-html-to-b64:1.0.1",
                        "class": "DockerRequirement",
                        "dockerImageId": "8c35d2a2d8d1"
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
                "cwlVersion": "sbg:draft-2",
                "id": "bix-demo/sbgtools-demo/sbg-html2b64/2",
                "arguments": [],
                "sbg:cmdPreview": "python /opt/sbg_html_to_b64.py",
                "sbg:id": "admin/sbg-public-data/sbg-html2b64/5",
                "outputs": [
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_file",
                            "sbg:metadata": {},
                            "glob": "*b64html"
                        },
                        "label": "B64html",
                        "description": "Output file, b64html.",
                        "id": "#b64html",
                        "sbg:fileTypes": "HTML, B64HTML"
                    }
                ]
            },
            "outputs": [
                {
                    "id": "#SBG_Html2b64_1.b64html"
                }
            ]
        },
        {
            "sbg:y": 361.00001513958,
            "sbg:x": 417.33336008919673,
            "inputs": [
                {
                    "id": "#FastQC.input_fastq",
                    "source": [
                        "#FASTQ_Reads"
                    ]
                },
                {
                    "id": "#FastQC.kmers"
                },
                {
                    "id": "#FastQC.limits_file",
                    "source": [
                        "#limits_file"
                    ]
                },
                {
                    "id": "#FastQC.adapters_file",
                    "source": [
                        "#adapters_file"
                    ]
                },
                {
                    "id": "#FastQC.contaminants_file",
                    "source": [
                        "#contaminants_file"
                    ]
                },
                {
                    "id": "#FastQC.format"
                },
                {
                    "id": "#FastQC.nogroup"
                },
                {
                    "id": "#FastQC.nano"
                },
                {
                    "id": "#FastQC.casava"
                },
                {
                    "id": "#FastQC.threads"
                },
                {
                    "id": "#FastQC.quiet"
                },
                {
                    "id": "#FastQC.cpus_per_job"
                },
                {
                    "id": "#FastQC.memory_per_job"
                }
            ],
            "id": "#FastQC",
            "run": {
                "sbg:job": {
                    "inputs": {
                        "cpus_per_job": null,
                        "format": null,
                        "memory_per_job": null,
                        "threads": null,
                        "input_fastq": [
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "class": "File",
                                "path": "/path/to/input_fastq-1.fastq"
                            },
                            {
                                "size": 0,
                                "secondaryFiles": [],
                                "class": "File",
                                "path": "/path/to/input_fastq-2.fastq"
                            }
                        ],
                        "quiet": true
                    },
                    "allocatedResources": {
                        "cpu": 2,
                        "mem": 1624
                    }
                },
                "stdin": "",
                "sbg:categories": [
                    "FASTQ-Processing",
                    "Quality-Control",
                    "Quantification"
                ],
                "sbg:createdBy": "bix-demo",
                "sbg:createdOn": 1450911593,
                "requirements": [
                    {
                        "id": "#cwl-js-engine",
                        "requirements": [
                            {
                                "dockerPull": "rabix/js-engine",
                                "class": "DockerRequirement"
                            }
                        ],
                        "class": "ExpressionEngineRequirement"
                    }
                ],
                "temporaryFailCodes": [],
                "successCodes": [],
                "sbg:appVersion": [
                    "sbg:draft-2"
                ],
                "sbg:modifiedBy": "nikola_jovanovic",
                "inputs": [
                    {
                        "type": [
                            {
                                "items": "File",
                                "name": "input_fastq",
                                "type": "array"
                            }
                        ],
                        "inputBinding": {
                            "position": 100,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "itemSeparator": null
                        },
                        "label": "Input file",
                        "description": "Input file.",
                        "id": "#input_fastq",
                        "sbg:fileTypes": "FASTQ, FQ, FASTQ.GZ, FQ.GZ, BAM, SAM",
                        "sbg:category": "File inputs"
                    },
                    {
                        "type": [
                            "null",
                            "int"
                        ],
                        "inputBinding": {
                            "separate": true,
                            "prefix": "--kmers",
                            "sbg:cmdInclude": true
                        },
                        "label": "Kmers",
                        "description": "Specifies the length of Kmer to look for in the Kmer content module. Specified Kmer length must be between 2 and 10. Default length is 7 if not specified.",
                        "id": "#kmers",
                        "sbg:altPrefix": "-f",
                        "sbg:toolDefaultValue": "7",
                        "sbg:category": "Options"
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "inputBinding": {
                            "separate": true,
                            "prefix": "--limits",
                            "sbg:cmdInclude": true
                        },
                        "label": "Limits",
                        "description": "Specifies a non-default file which contains a set of criteria which will be used to determine the warn/error limits for the various modules.  This file can also be used to selectively remove some modules from the output all together.  The format needs to mirror the default limits.txt file found in the Configuration folder.",
                        "id": "#limits_file",
                        "sbg:altPrefix": "-l",
                        "sbg:fileTypes": "TXT",
                        "sbg:category": "File inputs"
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "inputBinding": {
                            "separate": true,
                            "prefix": "--adapters",
                            "sbg:cmdInclude": true
                        },
                        "label": "Adapters",
                        "description": "Specifies a non-default file which contains the list of adapter sequences which will be explicity searched against the library. The file must contain sets of named adapters in the form name[tab]sequence.  Lines prefixed with a hash will be ignored.",
                        "id": "#adapters_file",
                        "sbg:altPrefix": "-a",
                        "sbg:fileTypes": "TXT",
                        "sbg:category": "File inputs"
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "inputBinding": {
                            "separate": true,
                            "prefix": "--contaminants",
                            "sbg:cmdInclude": true
                        },
                        "label": "Contaminants",
                        "description": "Specifies a non-default file which contains the list of contaminants to screen overrepresented sequences against. The file must contain sets of named contaminants in the form name[tab]sequence.  Lines prefixed with a hash will be ignored.",
                        "id": "#contaminants_file",
                        "sbg:altPrefix": "-c",
                        "sbg:fileTypes": "TXT",
                        "sbg:category": "File inputs"
                    },
                    {
                        "type": [
                            "null",
                            {
                                "symbols": [
                                    "bam",
                                    "sam",
                                    "bam_mapped",
                                    "sam_mapped",
                                    "fastq"
                                ],
                                "name": "format",
                                "type": "enum"
                            }
                        ],
                        "inputBinding": {
                            "separate": true,
                            "prefix": "--format",
                            "sbg:cmdInclude": true
                        },
                        "label": "Format",
                        "description": "Bypasses the normal sequence file format detection and forces the program to use the specified format.  Valid formats are BAM, SAM, BAM_mapped, SAM_mapped and FASTQ.",
                        "id": "#format",
                        "sbg:altPrefix": "-f",
                        "sbg:toolDefaultValue": "FASTQ",
                        "sbg:category": "Options"
                    },
                    {
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "inputBinding": {
                            "separate": false,
                            "prefix": "--nogroup",
                            "sbg:cmdInclude": true
                        },
                        "label": "Nogroup",
                        "description": "Disable grouping of bases for reads >50bp. All reports will show data for every base in the read.  WARNING: Using this option will cause fastqc to crash and burn if you use it on really long reads, and your plots may end up a ridiculous size. You have been warned.",
                        "id": "#nogroup",
                        "sbg:category": "Options"
                    },
                    {
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "inputBinding": {
                            "separate": false,
                            "prefix": "--nano",
                            "sbg:cmdInclude": true
                        },
                        "label": "Nano",
                        "description": "Files come from naopore sequences and are in fast5 format. In this mode you can pass in directories to process and the program will take in all fast5 files within those directories and produce a single output file from the sequences found in all files.",
                        "id": "#nano",
                        "sbg:category": "Options"
                    },
                    {
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "inputBinding": {
                            "separate": false,
                            "prefix": "--casava",
                            "sbg:cmdInclude": true
                        },
                        "label": "Casava",
                        "description": "Files come from raw casava output. Files in the same sample group (differing only by the group number) will be analysed as a set rather than individually. Sequences with the filter flag set in the header will be excluded from the analysis. Files must have the same names given to them by casava (including being gzipped and ending with .gz) otherwise they won't be grouped together correctly.",
                        "id": "#casava",
                        "sbg:category": "Options"
                    },
                    {
                        "type": [
                            "null",
                            "int"
                        ],
                        "inputBinding": {
                            "valueFrom": {
                                "engine": "#cwl-js-engine",
                                "class": "Expression",
                                "script": "{\n//if \"threads\" is not specified\n//number of threads is determined based on number of inputs\n  if (! $job.inputs.threads){\n    $job.inputs.threads = [].concat($job.inputs.input_fastq).length\n  }\n  return Math.min($job.inputs.threads,7)\n}"
                            },
                            "separate": true,
                            "prefix": "--threads",
                            "sbg:cmdInclude": true
                        },
                        "label": "Threads",
                        "description": "Specifies the number of files which can be processed simultaneously.  Each thread will be allocated 250MB of memory so you shouldn't run more threads than your available memory will cope with, and not more than 6 threads on a 32 bit machine.",
                        "id": "#threads",
                        "sbg:altPrefix": "-t",
                        "sbg:toolDefaultValue": "1",
                        "sbg:category": "Options"
                    },
                    {
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "inputBinding": {
                            "separate": true,
                            "prefix": "--quiet",
                            "sbg:cmdInclude": true
                        },
                        "label": "Quiet",
                        "description": "Supress all progress messages on stdout and only report errors.",
                        "id": "#quiet",
                        "sbg:altPrefix": "-q",
                        "sbg:category": "Options"
                    },
                    {
                        "type": [
                            "null",
                            "int"
                        ],
                        "label": "Number of CPUs.",
                        "description": "Number of CPUs to be allocated per execution of FastQC.",
                        "id": "#cpus_per_job",
                        "sbg:toolDefaultValue": "Determined by the number of input files",
                        "sbg:category": "Execution parameters"
                    },
                    {
                        "type": [
                            "null",
                            "int"
                        ],
                        "label": "Amount of memory allocated per job execution.",
                        "description": "Amount of memory allocated per execution of FastQC job.",
                        "id": "#memory_per_job",
                        "sbg:toolDefaultValue": "Determined by the number of input files",
                        "sbg:category": "Execution parameters"
                    }
                ],
                "sbg:toolkitVersion": "0.11.4",
                "description": "FastQC reads a set of sequence files and produces a quality control (QC) report from each one. These reports consist of a number of different modules, each of which will help identify a different type of potential problem in your data. \n\nSince it's necessary to convert the tool report in order to show them on Seven Bridges platform, it's recommended to use [FastQC Analysis workflow instead](https://igor.sbgenomics.com/public/apps#admin/sbg-public-data/fastqc-analysis/). \n\nFastQC is a tool which takes a FASTQ file and runs a series of tests on it to generate a comprehensive QC report.  This report will tell you if there is anything unusual about your sequence.  Each test is flagged as a pass, warning, or fail depending on how far it departs from what you would expect from a normal large dataset with no significant biases.  It is important to stress that warnings or even failures do not necessarily mean that there is a problem with your data, only that it is unusual.  It is possible that the biological nature of your sample means that you would expect this particular bias in your results.\n\n### Common Issues:\n\nOutput of the tool is ZIP archive. In order to view report on Seven Bridges platform, you can use SBG Html2b64 tool. It is advised to scatter SBG Html2b64 so it would be able to process an array of files. The example can be seen in [FastQC Analysis workflow](https://igor.sbgenomics.com/public/apps#admin/sbg-public-data/fastqc-analysis/) which you can also use instead of this tool.",
                "sbg:contributors": [
                    "bix-demo",
                    "nikola_jovanovic",
                    "mladenlSBG"
                ],
                "sbg:modifiedOn": 1493223877,
                "stdout": "",
                "sbg:toolAuthor": "Babraham Institute",
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revisionNotes": null,
                        "sbg:revision": 0,
                        "sbg:modifiedOn": 1450911593
                    },
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revisionNotes": null,
                        "sbg:revision": 1,
                        "sbg:modifiedOn": 1450911593
                    },
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revisionNotes": null,
                        "sbg:revision": 2,
                        "sbg:modifiedOn": 1450911594
                    },
                    {
                        "sbg:modifiedBy": "mladenlSBG",
                        "sbg:revisionNotes": null,
                        "sbg:revision": 3,
                        "sbg:modifiedOn": 1459870965
                    },
                    {
                        "sbg:modifiedBy": "mladenlSBG",
                        "sbg:revisionNotes": null,
                        "sbg:revision": 4,
                        "sbg:modifiedOn": 1465990120
                    },
                    {
                        "sbg:modifiedBy": "mladenlSBG",
                        "sbg:revisionNotes": "Input categories added.",
                        "sbg:revision": 5,
                        "sbg:modifiedOn": 1476188095
                    },
                    {
                        "sbg:modifiedBy": "mladenlSBG",
                        "sbg:revisionNotes": "FASTQ input changed from single file to array. Added better thread handling. \n\nIMPORTANT NOTICE: If updating this tool in existing workflow, it's necessary to REMOVE SCATTER (uncheck it) from input_fastq or it might break the pipeline.",
                        "sbg:revision": 6,
                        "sbg:modifiedOn": 1476270496
                    },
                    {
                        "sbg:modifiedBy": "mladenlSBG",
                        "sbg:revisionNotes": "FASTQ input changed from single file to array. Added better thread handling.\n\nIMPORTANT NOTICE: If updating this tool in existing workflow, it's necessary to REMOVE SCATTER (uncheck it) from input_fastq or it might break the pipeline.",
                        "sbg:revision": 7,
                        "sbg:modifiedOn": 1476354537
                    },
                    {
                        "sbg:modifiedBy": "mladenlSBG",
                        "sbg:revisionNotes": "IMPORTANT NOTICE: If updating this tool in existing workflow, it's necessary to REMOVE SCATTER (uncheck it) from input_fastq or it might break the pipeline.\"\n\nAdded automatised handling of BAM and SAM files. Also, added security measures for better automated threading handling.",
                        "sbg:revision": 8,
                        "sbg:modifiedOn": 1488882730
                    },
                    {
                        "sbg:modifiedBy": "nikola_jovanovic",
                        "sbg:revisionNotes": "Changed the file types of limits, adapters and contaminants files to be TXT, they have to be in format name[tab]sequence. Format should be similar to the one in the Configuration folder provided with FastQC, txt files.\n\n\"IMPORTANT NOTICE: If updating this tool in existing workflow, it's necessary to REMOVE SCATTER (uncheck it) from input_fastq or it might break the pipeline.\"",
                        "sbg:revision": 9,
                        "sbg:modifiedOn": 1488980183
                    },
                    {
                        "sbg:modifiedBy": "nikola_jovanovic",
                        "sbg:revisionNotes": "* Fixed the JS expression for the CPU and Memory allocation\n* Added cpus_per_job and memory_per_job parameters\n* Removed default version for format, so the tool can handle combinations of file formats",
                        "sbg:revision": 10,
                        "sbg:modifiedOn": 1493223877
                    }
                ],
                "sbg:sbgMaintained": false,
                "sbg:project": "bix-demo/fastqc-0-11-4-demo",
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
                "class": "CommandLineTool",
                "label": "FastQC",
                "sbg:image_url": null,
                "sbg:latestRevision": 10,
                "sbg:revisionNotes": "* Fixed the JS expression for the CPU and Memory allocation\n* Added cpus_per_job and memory_per_job parameters\n* Removed default version for format, so the tool can handle combinations of file formats",
                "sbg:validationErrors": [],
                "sbg:license": "GNU General Public License v3.0 only",
                "sbg:links": [
                    {
                        "id": "http://www.bioinformatics.babraham.ac.uk/projects/fastqc/",
                        "label": "Homepage"
                    },
                    {
                        "id": "http://www.bioinformatics.babraham.ac.uk/projects/fastqc/fastqc_v0.11.4_source.zip",
                        "label": "Source Code"
                    },
                    {
                        "id": "https://wiki.hpcc.msu.edu/display/Bioinfo/FastQC+Tutorial",
                        "label": "Wiki"
                    },
                    {
                        "id": "http://www.bioinformatics.babraham.ac.uk/projects/fastqc/fastqc_v0.11.4.zip",
                        "label": "Download"
                    },
                    {
                        "id": "http://www.bioinformatics.babraham.ac.uk/projects/fastqc",
                        "label": "Publication"
                    }
                ],
                "sbg:cmdPreview": "fastqc  --noextract --outdir .  /path/to/input_fastq-1.fastq  /path/to/input_fastq-2.fastq",
                "baseCommand": [
                    "fastqc"
                ],
                "sbg:projectName": "FastQC 0.11.4 - Demo",
                "sbg:revision": 10,
                "hints": [
                    {
                        "dockerPull": "images.sbgenomics.com/mladenlsbg/fastqc:0.11.4",
                        "class": "DockerRequirement",
                        "dockerImageId": "759c4c8fbafd"
                    },
                    {
                        "value": {
                            "engine": "#cwl-js-engine",
                            "class": "Expression",
                            "script": "{\n  // if cpus_per_job is set, it takes precedence\n  if ($job.inputs.cpus_per_job) {\n    return $job.inputs.cpus_per_job \n  }\n  // if threads parameter is set, the number of CPUs is set based on that parametere\n  else if ($job.inputs.threads) {\n    return $job.inputs.threads\n  }\n  // else the number of CPUs is determined by the number of input files, up to 7 -- default\n  else return Math.min([].concat($job.inputs.input_fastq).length,7)\n}"
                        },
                        "class": "sbg:CPURequirement"
                    },
                    {
                        "value": {
                            "engine": "#cwl-js-engine",
                            "class": "Expression",
                            "script": "{\n  // if memory_per_job is set, it takes precedence\n  if ($job.inputs.memory_per_job){\n    return $job.inputs.memory_per_job\n  }\n  // if threads parameter is set, memory req is set based on the number of threads\n  else if ($job.inputs.threads){\n    return 1024 + 300*$job.inputs.threads\n  }\n  // else the memory req is determined by the number of input files, up to 7 -- default\n  else return (1024 + 300*Math.min([].concat($job.inputs.input_fastq).length,7))\n}\n\n"
                        },
                        "class": "sbg:MemRequirement"
                    }
                ],
                "cwlVersion": "sbg:draft-2",
                "id": "https://api.sbgenomics.com/v2/apps/bix-demo/fastqc-0-11-4-demo/fastqc-0-11-4/10/raw/",
                "outputs": [
                    {
                        "type": [
                            "null",
                            {
                                "items": "File",
                                "name": "report_zip",
                                "type": "array"
                            }
                        ],
                        "outputBinding": {
                            "sbg:inheritMetadataFrom": "#input_fastq",
                            "sbg:metadata": {
                                "__inherit__": "input_fastq"
                            },
                            "glob": "*_fastqc.zip"
                        },
                        "label": "Report zip",
                        "description": "Zip archive of the report.",
                        "id": "#report_zip",
                        "sbg:fileTypes": "ZIP"
                    }
                ],
                "sbg:toolkit": "FastQC",
                "sbg:id": "admin/sbg-public-data/fastqc-0-11-4/18"
            },
            "outputs": [
                {
                    "id": "#FastQC.report_zip"
                }
            ]
        }
    ],
    "sbg:appVersion": [
        "sbg:draft-2"
    ],
    "sbg:modifiedBy": "bixqa",
    "inputs": [
        {
            "sbg:includeInPorts": true,
            "label": "FASTQ Reads",
            "type": [
                {
                    "items": "File",
                    "type": "array"
                }
            ],
            "id": "#FASTQ_Reads",
            "sbg:y": 347.6667008267519,
            "sbg:x": 190.666674176852
        },
        {
            "sbg:x": 189.99996185302743,
            "label": "limits_file",
            "type": [
                "null",
                "File"
            ],
            "id": "#limits_file",
            "sbg:y": 225.0000000000001,
            "sbg:fileTypes": "TXT"
        },
        {
            "sbg:x": 191.66661580403658,
            "label": "contaminants_file",
            "type": [
                "null",
                "File"
            ],
            "id": "#contaminants_file",
            "sbg:y": 471.66671752929716,
            "sbg:fileTypes": "TXT"
        },
        {
            "sbg:x": 193.33330790201833,
            "label": "adapters_file",
            "type": [
                "null",
                "File"
            ],
            "id": "#adapters_file",
            "sbg:y": 598.3333333333336,
            "sbg:fileTypes": "TXT"
        }
    ],
    "sbg:copyOf": "admin/sbg-public-data/fastqc-analysis/8",
    "sbg:toolkitVersion": "1",
    "description": "The FastQC tool, developed by the Babraham Institute, analyzes sequence data from FASTQ, BAM, or SAM files. It produces a set of metrics and charts that help identify technical problems with the data. \n\nUse this pipeline on files you receive from a sequencer or a collaborator to get a general idea of how well the sequencing experiment went. Results from this pipeline can inform if and how you should proceed with your analysis.\n\n###Required inputs\n\n1. FASTQ Reads (ID: *FASTQ_reads*) - one or more FASTQ files. *Note*: In order to process these files efficient, set the number of threads on FastQC app. If it is not set, it will be set automatically based on number of input files, one CPU core per file. If the number of the files is too big (greater than the number of CPU cores on instance) the task will fail. Therefore it's advised that the user should set the \"threads\" argument of FastQC.\n\n###Outputs\n\n1. Report ZIP (ID: *report_zip*)  - ZIP archive containing FastQC html report with dependancies.\n2. FastQC Charts (ID: *b64html*) - Self-contained b64html file, enabling users to see FastQC reports on Seven Bridges platform.\n\n###Common issues\n\n1. In order to process these files efficient, set the number of threads on FastQC app. If it is not set, it will be set automatically based on number of input files, one CPU core per file. If the number of the files is too big (greater than the number of CPU cores on instance) the task will fail. Therefore it's advised that the user should set the \"threads\" argument of FastQC.\n2. If processing large number of big FASTQ files, you might hit the limit of available disk space. Before starting the workflow, check if the total input size is less than available instance disk space. If not, set the different instance, or reduce number of inputs.",
    "sbg:contributors": [
        "bixqa"
    ],
    "sbg:createdOn": 1501518627,
    "sbg:toolAuthor": "Seven Bridges",
    "sbg:canvas_y": 31,
    "sbg:project": "bixqa/qa-load-2017-07-31-18",
    "class": "Workflow",
    "label": "FastQC Analysis",
    "sbg:image_url": "https://brood.sbgenomics.com/static/bixqa/qa-load-2017-07-31-18/fastqc-analysis/8.png",
    "sbg:createdBy": "bixqa",
    "sbg:revisionNotes": "Updated FastQC to revision 10.",
    "sbg:validationErrors": [],
    "sbg:categories": [
        "Quality-Control",
        "FASTQ-Processing"
    ],
    "sbg:license": "Apache License 2.0",
    "sbg:links": [
        {
            "id": "http://www.bioinformatics.babraham.ac.uk/projects/fastqc/",
            "label": "Homepage"
        },
        {
            "id": "http://www.bioinformatics.babraham.ac.uk/projects/fastqc/Help/",
            "label": "Documentation"
        }
    ],
    "sbg:projectName": "qa-load-2017-07-31-18",
    "sbg:revision": 8,
    "hints": [],
    "sbg:canvas_x": 228,
    "cwlVersion": "sbg:draft-2",
    "id": "https://api.sbgenomics.com/v2/apps/bixqa/qa-load-2017-07-31-18/fastqc-analysis/8/raw/",
    "sbg:canvas_zoom": 0.5999999999999996,
    "sbg:toolkit": "SBGTools",
    "sbg:id": "bixqa/qa-load-2017-07-31-18/fastqc-analysis/8",
    "outputs": [
        {
            "sbg:x": 625.6667522986758,
            "type": [
                "null",
                "File"
            ],
            "label": "Report ZIP",
            "sbg:includeInPorts": true,
            "id": "#report_zip",
            "sbg:y": 468.0002322197048,
            "source": [
                "#FastQC.report_zip"
            ],
            "required": false
        },
        {
            "sbg:fileTypes": "HTML, B64HTML",
            "sbg:x": 729.0004155900901,
            "type": [
                "null",
                "File"
            ],
            "label": "FastQC Charts",
            "sbg:includeInPorts": true,
            "id": "#b64html",
            "sbg:y": 271.6668099694836,
            "source": [
                "#SBG_Html2b64_1.b64html"
            ],
            "required": false
        }
    ]
}
