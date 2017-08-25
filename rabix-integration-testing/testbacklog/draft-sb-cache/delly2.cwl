{
    "sbg:createdBy": "bixqa",
    "sbg:categories": [
        "Variant-Calling"
    ],
    "sbg:image_url": "https://brood.sbgenomics.com/static/bixqa/qa-load-2017-07-31-18/delly2-workflow-0-7-1/0.png",
    "sbg:copyOf": "admin/sbg-public-data/delly2-workflow-0-7-1/0",
    "steps": [
        {
            "sbg:x": 715.5555119749948,
            "id": "#Delly2_1",
            "inputs": [
                {
                    "source": [
                        "#Picard_SortSam.sorted_bam"
                    ],
                    "id": "#Delly2_1.bam"
                },
                {
                    "source": [
                        "#SAMtools_Index_FASTA.output_fasta_file"
                    ],
                    "id": "#Delly2_1.genome"
                },
                {
                    "source": [
                        "#type"
                    ],
                    "id": "#Delly2_1.type"
                },
                {
                    "source": [
                        "#exclude"
                    ],
                    "id": "#Delly2_1.exclude"
                },
                {
                    "source": [
                        "#vcfgeno"
                    ],
                    "id": "#Delly2_1.vcfgeno"
                },
                {
                    "source": [
                        "#exclude_preset"
                    ],
                    "id": "#Delly2_1.exclude_preset"
                },
                {
                    "source": [
                        "#map_qual"
                    ],
                    "id": "#Delly2_1.map_qual"
                },
                {
                    "source": [
                        "#mad_cutoff"
                    ],
                    "id": "#Delly2_1.mad_cutoff"
                },
                {
                    "source": [
                        "#min_flank"
                    ],
                    "id": "#Delly2_1.min_flank"
                },
                {
                    "source": [
                        "#flanking"
                    ],
                    "id": "#Delly2_1.flanking"
                },
                {
                    "source": [
                        "#noindels"
                    ],
                    "id": "#Delly2_1.noindels"
                },
                {
                    "source": [
                        "#indel_size"
                    ],
                    "id": "#Delly2_1.indel_size"
                },
                {
                    "source": [
                        "#geno_qual"
                    ],
                    "id": "#Delly2_1.geno_qual"
                }
            ],
            "sbg:y": 204.44441394452753,
            "outputs": [
                {
                    "id": "#Delly2_1.output"
                }
            ],
            "run": {
                "sbg:createdBy": "bix-demo",
                "sbg:categories": [
                    "Variant-Calling"
                ],
                "successCodes": [],
                "sbg:validationErrors": [],
                "outputs": [
                    {
                        "sbg:fileTypes": "VCF",
                        "description": "SV output file.",
                        "id": "#output",
                        "type": [
                            "null",
                            "File"
                        ],
                        "outputBinding": {
                            "sbg:metadata": {},
                            "sbg:inheritMetadataFrom": "#bam",
                            "glob": {
                                "class": "Expression",
                                "script": "{\n\tfile = $job.inputs.bam.path\n    file_split = file.split('.')\n    basename = file_split\n    if (file_split.length > 1) {\n    \tbasename = file_split.slice(0, file_split.length-1)\n    }\n  \tretval = basename.concat('vcf')\n    return retval.join('.').replace(/^.*[\\\\\\/]/, '')\n}\n",
                                "engine": "#cwl-js-engine"
                            }
                        },
                        "label": "SV output"
                    }
                ],
                "requirements": [
                    {
                        "class": "ExpressionEngineRequirement",
                        "requirements": [
                            {
                                "class": "DockerRequirement",
                                "dockerPull": "rabix/js-engine"
                            }
                        ],
                        "id": "#cwl-js-engine"
                    }
                ],
                "sbg:license": "GNU General Public License v3.0 only",
                "hints": [
                    {
                        "class": "DockerRequirement",
                        "dockerPull": "images.sbgenomics.com/tziotas/delly:0.7.1--GIT4NOV2015",
                        "dockerImageId": ""
                    },
                    {
                        "class": "sbg:CPURequirement",
                        "value": 1
                    },
                    {
                        "class": "sbg:MemRequirement",
                        "value": 7000
                    }
                ],
                "sbg:toolkitVersion": "0.7.1",
                "sbg:modifiedOn": 1450911484,
                "id": "https://api.sbgenomics.com/bix-demo/delly-2-demo/delly2-0-7-1/2/raw/",
                "temporaryFailCodes": [],
                "class": "CommandLineTool",
                "sbg:project": "bix-demo/delly-2-demo",
                "sbg:toolkit": "Delly2",
                "stdin": "",
                "sbg:createdOn": 1450911483,
                "sbg:links": [
                    {
                        "id": "https://github.com/tobiasrausch/delly",
                        "label": "Home Page"
                    },
                    {
                        "id": "http://bioinformatics.oxfordjournals.org/content/28/18/i333.abstract",
                        "label": "Publications"
                    },
                    {
                        "id": "https://github.com/tobiasrausch/delly/blob/master/README.md",
                        "label": "Manual"
                    }
                ],
                "sbg:cmdPreview": "/opt/delly/delly -t DEL -q 3 -s 9 -f 90 -m 13 -n  -i 500 -u 5 -o sample1.vcf -x exclude.ext -v genotype.ext -g reference.fa sample1.bam sample1.bam reference.fa",
                "sbg:modifiedBy": "bix-demo",
                "sbg:id": "admin/sbg-public-data/delly2-0-7-1/2",
                "baseCommand": [
                    "/opt/delly/delly"
                ],
                "sbg:toolAuthor": "Tobias Rausch",
                "label": "Delly2",
                "cwlVersion": "sbg:draft-2",
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1450911483,
                        "sbg:revision": 0,
                        "sbg:modifiedBy": "bix-demo"
                    },
                    {
                        "sbg:modifiedOn": 1450911484,
                        "sbg:revision": 1,
                        "sbg:modifiedBy": "bix-demo"
                    },
                    {
                        "sbg:modifiedOn": 1450911484,
                        "sbg:revision": 2,
                        "sbg:modifiedBy": "bix-demo"
                    }
                ],
                "description": "Delly is a tool for predicting structural variants, such as deletions, duplications, translocations, and inversions. It integrates short insert paired-ends, long-range mate-pairs, and split-read alignments to accurately delineate genomic rearrangements at single-nucleotide resolution.",
                "stdout": "",
                "sbg:contributors": [
                    "bix-demo"
                ],
                "sbg:sbgMaintained": false,
                "sbg:revision": 2,
                "inputs": [
                    {
                        "inputBinding": {
                            "sbg:cmdInclude": true,
                            "position": 100,
                            "separate": false,
                            "itemSeparator": null
                        },
                        "type": [
                            "File"
                        ],
                        "description": "Samples in BAM file format, one BAM per sample.",
                        "id": "#bam",
                        "label": "Bams"
                    },
                    {
                        "inputBinding": {
                            "prefix": "-g",
                            "sbg:cmdInclude": true,
                            "position": 99,
                            "separate": true
                        },
                        "type": [
                            "File"
                        ],
                        "description": "Genome FASTA file.",
                        "id": "#genome",
                        "label": "Genome"
                    },
                    {
                        "inputBinding": {
                            "prefix": "-t",
                            "sbg:cmdInclude": true,
                            "position": 0,
                            "separate": true
                        },
                        "type": [
                            "null",
                            {
                                "name": "type",
                                "symbols": [
                                    "DEL",
                                    "DUP",
                                    "INV",
                                    "TRA",
                                    "INS"
                                ],
                                "type": "enum"
                            }
                        ],
                        "description": "Choose structural variant analysis that will be performed.",
                        "id": "#type",
                        "label": "Type"
                    },
                    {
                        "type": [
                            "null",
                            "File"
                        ],
                        "description": "A TSV file with telomere and centromere regions that can be excluded in order to save time. This file normally has the following columns: CHROMOSOME, START, END, COMMENT(telomere/centromere).",
                        "id": "#exclude",
                        "label": "Exclude"
                    },
                    {
                        "inputBinding": {
                            "prefix": "-v",
                            "sbg:cmdInclude": true,
                            "position": 98,
                            "separate": true
                        },
                        "type": [
                            "null",
                            "File"
                        ],
                        "description": "A VCF file for genotyping only.",
                        "id": "#vcfgeno",
                        "label": "Genotype"
                    },
                    {
                        "type": [
                            "null",
                            {
                                "name": "exclude_preset",
                                "symbols": [
                                    "None",
                                    "Human HG19",
                                    "Human HG38",
                                    "Mouse MM10"
                                ],
                                "type": "enum"
                            }
                        ],
                        "description": "Ready templates for telomere and centromere regions that can be excluded in order to save time. If an external exclude file is supplied this option will be ignored.",
                        "id": "#exclude_preset",
                        "label": "Exclude preset"
                    },
                    {
                        "inputBinding": {
                            "prefix": "-q",
                            "sbg:cmdInclude": true,
                            "position": 1,
                            "valueFrom": {
                                "class": "Expression",
                                "script": "{\n  if (!$job.inputs.map_qual)\n  {\n    return 1\n  }\n  else\n  {\n    return $job.inputs.map_qual\n  }\n}\n",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": true
                        },
                        "type": [
                            "null",
                            "int"
                        ],
                        "description": "Minimum paired-end mapping quality.",
                        "id": "#map_qual",
                        "label": "Min mapping quality"
                    },
                    {
                        "inputBinding": {
                            "prefix": "-s",
                            "sbg:cmdInclude": true,
                            "position": 2,
                            "valueFrom": {
                                "class": "Expression",
                                "script": "{\n  if (!$job.inputs.mad_cutoff)\n  {\n    return 9\n  }\n  else\n  {\n    return $job.inputs.mad_cutoff\n  }\n}\n",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": true
                        },
                        "type": [
                            "null",
                            "int"
                        ],
                        "description": "Insert size cutoff, median+s*MAD (deletions only).",
                        "id": "#mad_cutoff",
                        "label": "MAD Cutoff"
                    },
                    {
                        "inputBinding": {
                            "prefix": "-m",
                            "sbg:cmdInclude": true,
                            "position": 4,
                            "valueFrom": {
                                "class": "Expression",
                                "script": "{\n  if (!$job.inputs.min_flank)\n  {\n    return 13\n  }\n  else\n  {\n    return $job.inputs.min_flank\n  }\n}\n",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": true
                        },
                        "type": [
                            "null",
                            "int"
                        ],
                        "description": "Minimum flanking sequence size.",
                        "id": "#min_flank",
                        "label": "Min flanking size"
                    },
                    {
                        "inputBinding": {
                            "prefix": "-f",
                            "sbg:cmdInclude": true,
                            "position": 3,
                            "valueFrom": {
                                "class": "Expression",
                                "script": "{\n  if (!$job.inputs.flanking)\n  {\n    return 90\n  }\n  else\n  {\n    return $job.inputs.flanking\n  }\n}\n",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": true
                        },
                        "type": [
                            "null",
                            "int"
                        ],
                        "description": "Quality of the consensus alignment.",
                        "id": "#flanking",
                        "label": "Flanking"
                    },
                    {
                        "inputBinding": {
                            "prefix": "-n",
                            "sbg:cmdInclude": true,
                            "position": 5,
                            "separate": true
                        },
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "description": "No small InDel calling",
                        "id": "#noindels",
                        "label": "No InDels"
                    },
                    {
                        "inputBinding": {
                            "prefix": "-i",
                            "sbg:cmdInclude": true,
                            "position": 6,
                            "valueFrom": {
                                "class": "Expression",
                                "script": "{\n  if (!$job.inputs.indel_size)\n  {\n    return 500\n  }\n  else\n  {\n    return $job.inputs.indel_size\n  }\n}\n",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": true
                        },
                        "type": [
                            "null",
                            "int"
                        ],
                        "description": "Max small InDel size",
                        "id": "#indel_size",
                        "label": "InDel size"
                    },
                    {
                        "inputBinding": {
                            "prefix": "-u",
                            "sbg:cmdInclude": true,
                            "position": 7,
                            "valueFrom": {
                                "class": "Expression",
                                "script": "{\n  if (!$job.inputs.geno_qual)\n  {\n    return 5\n  }\n  else\n  {\n    return $job.inputs.geno_qual\n  }\n}\n",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": true
                        },
                        "type": [
                            "null",
                            "int"
                        ],
                        "description": "Min mapping quality for genotyping.",
                        "id": "#geno_qual",
                        "label": "Genotyping quality"
                    }
                ],
                "sbg:latestRevision": 2,
                "arguments": [
                    {
                        "prefix": "",
                        "position": 97,
                        "valueFrom": {
                            "class": "Expression",
                            "script": "{\n\tif ($job.inputs.exclude) {\n \t \treturn '-x '.concat($job.inputs.exclude.path)\n\t}\n\telse if ($job.inputs.exclude_preset) {\n  \t\tpreset = $job.inputs.exclude_preset\n \t \tif (preset == 'Human HG19') {\n   \t\t\treturn '-x /opt/delly/git/delly/excludeTemplates/human.hg19.excl.tsv'\n  \t\t}\n  \t\tif (preset == 'Human HG38') {\n    \t\treturn '-x /opt/delly/git/delly/excludeTemplates/human.hg38.excl.tsv'\n  \t\t}\n  \t\tif (preset == 'Mouse MM10') {\n    \t\treturn '-x /opt/delly/git/delly/excludeTemplates/mouse.mm10.excl.tsv'\n  \t\t}\n\t}\n \treturn ''\n}",
                            "engine": "#cwl-js-engine"
                        },
                        "separate": false
                    },
                    {
                        "prefix": "-o",
                        "position": 96,
                        "valueFrom": {
                            "class": "Expression",
                            "script": "{\n\tfile = $job.inputs.bam.path\n    file_split = file.split('.')\n    basename = file_split\n    if (file_split.length > 1) {\n    \tbasename = file_split.slice(0, file_split.length-1)\n    }\n  \tretval = basename.concat('vcf')\n    return retval.join('.').replace(/^.*[\\\\\\/]/, '')\n}\n",
                            "engine": "#cwl-js-engine"
                        },
                        "separate": true
                    }
                ],
                "sbg:job": {
                    "allocatedResources": {
                        "cpu": 1,
                        "mem": 7000
                    },
                    "inputs": {
                        "noindels": true,
                        "mad_cutoff": 0,
                        "geno_qual": 0,
                        "map_qual": 3,
                        "flanking": 0,
                        "indel_size": 0,
                        "type": "DEL",
                        "bam": {
                            "path": "sample1.bam"
                        },
                        "exclude": {
                            "class": "File",
                            "secondaryFiles": [],
                            "path": "exclude.ext",
                            "size": 0
                        },
                        "min_flank": 0,
                        "vcfgeno": {
                            "class": "File",
                            "secondaryFiles": [],
                            "path": "genotype.ext",
                            "size": 0
                        },
                        "exclude_preset": "None",
                        "genome": {
                            "class": "File",
                            "secondaryFiles": [],
                            "path": "reference.fa",
                            "size": 0
                        }
                    }
                }
            }
        },
        {
            "sbg:x": 301.111085214733,
            "id": "#SAMtools_Index_FASTA",
            "inputs": [
                {
                    "source": [
                        "#genome"
                    ],
                    "id": "#SAMtools_Index_FASTA.input_fasta_file"
                }
            ],
            "sbg:y": 191.11111050476262,
            "outputs": [
                {
                    "id": "#SAMtools_Index_FASTA.output_fasta_file"
                }
            ],
            "run": {
                "sbg:createdBy": "bix-demo",
                "sbg:categories": [
                    "FASTA-Processing",
                    "Indexing"
                ],
                "successCodes": [],
                "sbg:validationErrors": [],
                "outputs": [
                    {
                        "sbg:fileTypes": "FASTA",
                        "description": "Output FASTA file with index (FAI) file.",
                        "id": "#output_fasta_file",
                        "type": [
                            "File"
                        ],
                        "outputBinding": {
                            "sbg:metadata": {},
                            "sbg:inheritMetadataFrom": "#input_fasta_file",
                            "secondaryFiles": [
                                ".fai"
                            ],
                            "glob": {
                                "class": "Expression",
                                "script": "$job.inputs.input_fasta_file.path",
                                "engine": "#cwl-js-engine"
                            }
                        },
                        "label": "Output FASTA file"
                    }
                ],
                "requirements": [
                    {
                        "class": "ExpressionEngineRequirement",
                        "requirements": [
                            {
                                "class": "DockerRequirement",
                                "dockerPull": "rabix/js-engine"
                            }
                        ],
                        "id": "#cwl-js-engine"
                    }
                ],
                "sbg:license": "The MIT License",
                "hints": [
                    {
                        "class": "DockerRequirement",
                        "dockerPull": "images.sbgenomics.com/markop/samtools:0.1.18",
                        "dockerImageId": "3764b29ff462"
                    },
                    {
                        "class": "sbg:CPURequirement",
                        "value": 1
                    },
                    {
                        "class": "sbg:MemRequirement",
                        "value": 1000
                    }
                ],
                "sbg:toolkitVersion": "0.1.18",
                "sbg:modifiedOn": 1450911628,
                "id": "https://api.sbgenomics.com/bix-demo/samtools-0-1-18-demo/samtools-index-fasta-0-1-18/1/raw/",
                "temporaryFailCodes": [],
                "class": "CommandLineTool",
                "sbg:project": "bix-demo/samtools-0-1-18-demo",
                "sbg:toolkit": "SAMtools",
                "stdin": "",
                "sbg:createdOn": 1450911628,
                "sbg:links": [
                    {
                        "id": "http://samtools.sourceforge.net/",
                        "label": "Homepage"
                    },
                    {
                        "id": "https://github.com/samtools/samtools",
                        "label": "Source Code"
                    },
                    {
                        "id": "http://sourceforge.net/p/samtools/wiki/Home/",
                        "label": "Wiki"
                    },
                    {
                        "id": "http://sourceforge.net/projects/samtools/files/",
                        "label": "Download"
                    },
                    {
                        "id": "http://www.ncbi.nlm.nih.gov/pubmed/19505943",
                        "label": "Publication"
                    },
                    {
                        "id": "http://www.htslib.org/doc/samtools.html",
                        "label": "Documentation"
                    }
                ],
                "sbg:cmdPreview": "/opt/samtools-0.1.18/samtools faidx  fasta.fa fasta.fa",
                "sbg:modifiedBy": "bix-demo",
                "sbg:id": "admin/sbg-public-data/samtools-index-fasta-0-1-18/1",
                "baseCommand": [
                    "/opt/samtools-0.1.18/samtools",
                    "faidx"
                ],
                "sbg:toolAuthor": "Heng Li, Sanger Institute",
                "label": "SAMtools Index FASTA",
                "cwlVersion": "sbg:draft-2",
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1450911628,
                        "sbg:revision": 0,
                        "sbg:modifiedBy": "bix-demo"
                    },
                    {
                        "sbg:modifiedOn": 1450911628,
                        "sbg:revision": 1,
                        "sbg:modifiedBy": "bix-demo"
                    }
                ],
                "description": "SAMtools Index FASTA indexes the reference sequence in a FASTA format or extracts subsequence from the indexed reference sequence. If no region is specified, faidx will index the file and create <ref.fasta>.fai on the disk. If regions are specified, the subsequences will be retrieved and printed to stdout in the FASTA format. The input file can be compressed in a RAZF format.",
                "stdout": "",
                "sbg:contributors": [
                    "bix-demo"
                ],
                "sbg:sbgMaintained": false,
                "sbg:revision": 1,
                "inputs": [
                    {
                        "sbg:fileTypes": "FASTA",
                        "description": "FASTA input file.",
                        "id": "#input_fasta_file",
                        "inputBinding": {
                            "sbg:cmdInclude": true,
                            "position": 1,
                            "separate": true
                        },
                        "type": [
                            "File"
                        ],
                        "label": "FASTA input file"
                    }
                ],
                "sbg:latestRevision": 1,
                "arguments": [],
                "sbg:job": {
                    "allocatedResources": {
                        "cpu": 1,
                        "mem": 1000
                    },
                    "inputs": {
                        "input_fasta_file": {
                            "class": "File",
                            "secondaryFiles": [],
                            "path": "fasta.fa",
                            "size": 0
                        }
                    }
                }
            }
        },
        {
            "sbg:x": 299.9999615351367,
            "id": "#Picard_SortSam",
            "inputs": [
                {
                    "id": "#Picard_SortSam.output_type",
                    "default": "BAM"
                },
                {
                    "id": "#Picard_SortSam.sort_order",
                    "default": "Coordinate"
                },
                {
                    "id": "#Picard_SortSam.create_index",
                    "default": "True"
                },
                {
                    "id": "#Picard_SortSam.quiet"
                },
                {
                    "id": "#Picard_SortSam.validation_stringency",
                    "default": "SILENT"
                },
                {
                    "id": "#Picard_SortSam.compression_level"
                },
                {
                    "id": "#Picard_SortSam.max_records_in_ram"
                },
                {
                    "source": [
                        "#input_bam"
                    ],
                    "id": "#Picard_SortSam.input_bam"
                },
                {
                    "id": "#Picard_SortSam.memory_per_job"
                }
            ],
            "sbg:y": 468.8888697153258,
            "outputs": [
                {
                    "id": "#Picard_SortSam.sorted_bam"
                }
            ],
            "run": {
                "sbg:createdBy": "bix-demo",
                "sbg:categories": [
                    "SAM/BAM-Processing"
                ],
                "successCodes": [],
                "sbg:validationErrors": [],
                "outputs": [
                    {
                        "sbg:fileTypes": "BAM, SAM",
                        "description": "Sorted BAM or SAM file.",
                        "id": "#sorted_bam",
                        "type": [
                            "null",
                            "File"
                        ],
                        "outputBinding": {
                            "sbg:metadata": {
                                "__inherit__": "input_bam"
                            },
                            "sbg:inheritMetadataFrom": "#input_bam",
                            "secondaryFiles": [
                                "^.bai",
                                ".bai"
                            ],
                            "glob": "*.sorted.?am"
                        },
                        "label": "Sorted BAM/SAM"
                    }
                ],
                "requirements": [
                    {
                        "class": "ExpressionEngineRequirement",
                        "engineCommand": "cwl-engine.js",
                        "requirements": [
                            {
                                "class": "DockerRequirement",
                                "dockerPull": "rabix/js-engine"
                            }
                        ],
                        "id": "#cwl-js-engine"
                    }
                ],
                "sbg:license": "MIT License, Apache 2.0 Licence",
                "hints": [
                    {
                        "class": "DockerRequirement",
                        "dockerPull": "images.sbgenomics.com/mladenlsbg/picard:1.140",
                        "dockerImageId": "eab0e70b6629"
                    },
                    {
                        "class": "sbg:CPURequirement",
                        "value": 1
                    },
                    {
                        "class": "sbg:MemRequirement",
                        "value": {
                            "class": "Expression",
                            "script": "{\n  if($job.inputs.memory_per_job){\n  \treturn $job.inputs.memory_per_job\n  }\n  \treturn 2048\n}",
                            "engine": "#cwl-js-engine"
                        }
                    }
                ],
                "sbg:toolkitVersion": "1.140",
                "sbg:modifiedOn": 1450911170,
                "id": "https://api.sbgenomics.com/bix-demo/picard-1-140-demo/picard-sortsam-1-140/2/raw/",
                "temporaryFailCodes": [],
                "class": "CommandLineTool",
                "sbg:project": "bix-demo/picard-1-140-demo",
                "sbg:toolkit": "Picard",
                "stdin": "",
                "sbg:createdOn": 1450911168,
                "sbg:links": [
                    {
                        "id": "http://broadinstitute.github.io/picard/index.html",
                        "label": "Homepage"
                    },
                    {
                        "id": "https://github.com/broadinstitute/picard/releases/tag/1.138",
                        "label": "Source Code"
                    },
                    {
                        "id": "http://broadinstitute.github.io/picard/",
                        "label": "Wiki"
                    },
                    {
                        "id": "https://github.com/broadinstitute/picard/zipball/master",
                        "label": "Download"
                    },
                    {
                        "id": "http://broadinstitute.github.io/picard/",
                        "label": "Publication"
                    }
                ],
                "sbg:cmdPreview": "java -Xmx2048M -jar /opt/picard-tools-1.140/picard.jar SortSam OUTPUT=example.tested.sorted.bam INPUT=/root/dir/example.tested.bam SORT_ORDER=coordinate   INPUT=/root/dir/example.tested.bam SORT_ORDER=coordinate  /root/dir/example.tested.bam",
                "sbg:modifiedBy": "bix-demo",
                "sbg:id": "admin/sbg-public-data/picard-sortsam-1-140/2",
                "baseCommand": [
                    "java",
                    {
                        "class": "Expression",
                        "script": "{   \n  if($job.inputs.memory_per_job){\n    return '-Xmx'.concat($job.inputs.memory_per_job, 'M')\n  }   \n  \treturn '-Xmx2048M'\n}",
                        "engine": "#cwl-js-engine"
                    },
                    "-jar",
                    "/opt/picard-tools-1.140/picard.jar",
                    "SortSam"
                ],
                "sbg:toolAuthor": "Broad Institute",
                "label": "Picard SortSam",
                "cwlVersion": "sbg:draft-2",
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedOn": 1450911168,
                        "sbg:revision": 0,
                        "sbg:modifiedBy": "bix-demo"
                    },
                    {
                        "sbg:modifiedOn": 1450911169,
                        "sbg:revision": 1,
                        "sbg:modifiedBy": "bix-demo"
                    },
                    {
                        "sbg:modifiedOn": 1450911170,
                        "sbg:revision": 2,
                        "sbg:modifiedBy": "bix-demo"
                    }
                ],
                "description": "Picard SortSam sorts the input SAM or BAM. Input and output formats are determined by the file extension.",
                "stdout": "",
                "sbg:contributors": [
                    "bix-demo"
                ],
                "sbg:sbgMaintained": false,
                "sbg:revision": 2,
                "inputs": [
                    {
                        "sbg:category": "Other input types",
                        "description": "Since Picard tools can output both SAM and BAM files, user can choose the format of the output file.",
                        "id": "#output_type",
                        "type": [
                            "null",
                            {
                                "name": "output_type",
                                "symbols": [
                                    "BAM",
                                    "SAM",
                                    "SAME AS INPUT"
                                ],
                                "type": "enum"
                            }
                        ],
                        "sbg:toolDefaultValue": "SAME AS INPUT",
                        "label": "Output format"
                    },
                    {
                        "sbg:category": "Other input types",
                        "description": "Sort order of the output file. Possible values: {unsorted, queryname, coordinate}.",
                        "id": "#sort_order",
                        "inputBinding": {
                            "prefix": "SORT_ORDER=",
                            "sbg:cmdInclude": true,
                            "position": 3,
                            "valueFrom": {
                                "class": "Expression",
                                "script": "{\n  p = $job.inputs.sort_order.toLowerCase()\n  return p\n}",
                                "engine": "#cwl-js-engine"
                            },
                            "separate": false
                        },
                        "type": [
                            {
                                "name": "sort_order",
                                "symbols": [
                                    "Unsorted",
                                    "Queryname",
                                    "Coordinate"
                                ],
                                "type": "enum"
                            }
                        ],
                        "sbg:toolDefaultValue": "Coordinate",
                        "label": "Sort order",
                        "sbg:altPrefix": "SO"
                    },
                    {
                        "sbg:category": "Other input types",
                        "description": "This parameter indicates whether to create a BAM index when writing a coordinate-sorted BAM file. This option can be set to 'null' to clear the default value. Possible values: {true, false}.",
                        "id": "#create_index",
                        "inputBinding": {
                            "prefix": "CREATE_INDEX=",
                            "sbg:cmdInclude": true,
                            "position": 5,
                            "separate": false
                        },
                        "type": [
                            "null",
                            {
                                "name": "create_index",
                                "symbols": [
                                    "True",
                                    "False"
                                ],
                                "type": "enum"
                            }
                        ],
                        "sbg:toolDefaultValue": "False",
                        "label": "Create index"
                    },
                    {
                        "sbg:category": "Other input types",
                        "description": "This parameter indicates whether to suppress job-summary info on System.err. This option can be set to 'null' to clear the default value. Possible values: {true, false}.",
                        "id": "#quiet",
                        "inputBinding": {
                            "prefix": "QUIET=",
                            "sbg:cmdInclude": true,
                            "separate": false
                        },
                        "type": [
                            "null",
                            {
                                "name": "quiet",
                                "symbols": [
                                    "True",
                                    "False"
                                ],
                                "type": "enum"
                            }
                        ],
                        "sbg:toolDefaultValue": "False",
                        "label": "Quiet"
                    },
                    {
                        "sbg:category": "Other input types",
                        "description": "Validation stringency for all SAM files read by this program. Setting stringency to SILENT can improve performance when processing a BAM file in which variable-length data (read, qualities, tags) do not otherwise need to be decoded. This option can be set to 'null' to clear the default value. Possible values: {STRICT, LENIENT, SILENT}.",
                        "id": "#validation_stringency",
                        "inputBinding": {
                            "prefix": "VALIDATION_STRINGENCY=",
                            "sbg:cmdInclude": true,
                            "separate": false,
                            "valueFrom": {
                                "class": "Expression",
                                "script": "{\n  if ($job.inputs.validation_stringency)\n  {\n    return $job.inputs.validation_stringency\n  }\n  else\n  {\n    return \"SILENT\"\n  }\n}",
                                "engine": "#cwl-js-engine"
                            }
                        },
                        "type": [
                            "null",
                            {
                                "name": "validation_stringency",
                                "symbols": [
                                    "STRICT",
                                    "LENIENT",
                                    "SILENT"
                                ],
                                "type": "enum"
                            }
                        ],
                        "sbg:toolDefaultValue": "SILENT",
                        "label": "Validation stringency"
                    },
                    {
                        "sbg:category": "Other input types",
                        "description": "Compression level for all compressed files created (e.g. BAM and GELI). This option can be set to 'null' to clear the default value.",
                        "id": "#compression_level",
                        "inputBinding": {
                            "prefix": "COMPRESSION_LEVEL=",
                            "sbg:cmdInclude": true,
                            "separate": false
                        },
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:toolDefaultValue": "5",
                        "label": "Compression level"
                    },
                    {
                        "sbg:category": "Other input types",
                        "description": "When writing SAM files that need to be sorted, this parameter will specify the number of records stored in RAM before spilling to disk. Increasing this number reduces the number of file handles needed to sort a SAM file, and increases the amount of RAM needed. This option can be set to 'null' to clear the default value.",
                        "id": "#max_records_in_ram",
                        "inputBinding": {
                            "prefix": "MAX_RECORDS_IN_RAM=",
                            "sbg:cmdInclude": true,
                            "separate": false
                        },
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:toolDefaultValue": "500000",
                        "label": "Max records in RAM"
                    },
                    {
                        "sbg:category": "File inputs",
                        "sbg:fileTypes": "BAM, SAM",
                        "description": "The BAM or SAM file to sort.",
                        "id": "#input_bam",
                        "inputBinding": {
                            "prefix": "INPUT=",
                            "sbg:cmdInclude": true,
                            "position": 1,
                            "separate": false
                        },
                        "type": [
                            "File"
                        ],
                        "label": "Input BAM",
                        "sbg:altPrefix": "I"
                    },
                    {
                        "type": [
                            "null",
                            "int"
                        ],
                        "description": "Amount of RAM memory to be used per job. Defaults to 2048 MB for single threaded jobs.",
                        "id": "#memory_per_job",
                        "label": "Memory per job",
                        "sbg:toolDefaultValue": "2048"
                    }
                ],
                "sbg:latestRevision": 2,
                "arguments": [
                    {
                        "prefix": "OUTPUT=",
                        "position": 0,
                        "valueFrom": {
                            "class": "Expression",
                            "script": "{\n  filename = $job.inputs.input_bam.path\n  ext = $job.inputs.output_type\n\nif (ext === \"BAM\")\n{\n    return filename.split('.').slice(0, -1).concat(\"sorted.bam\").join(\".\").replace(/^.*[\\\\\\/]/, '')\n    }\n\nelse if (ext === \"SAM\")\n{\n    return filename.split('.').slice(0, -1).concat(\"sorted.sam\").join('.').replace(/^.*[\\\\\\/]/, '')\n}\n\nelse \n{\n\treturn filename.split('.').slice(0, -1).concat(\"sorted.\"+filename.split('.').slice(-1)[0]).join(\".\").replace(/^.*[\\\\\\/]/, '')\n}\n}",
                            "engine": "#cwl-js-engine"
                        },
                        "separate": false
                    },
                    {
                        "position": 1000,
                        "valueFrom": {
                            "class": "Expression",
                            "script": "{\n  filename = $job.inputs.input_bam.path\n  \n  /* figuring out output file type */\n  ext = $job.inputs.output_type\n  if (ext === \"BAM\")\n  {\n    out_extension = \"BAM\"\n  }\n  else if (ext === \"SAM\")\n  {\n    out_extension = \"SAM\"\n  }\n  else \n  {\n\tout_extension = filename.split('.').slice(-1)[0].toUpperCase()\n  }  \n  \n  /* if exist moving .bai in bam.bai */\n  if ($job.inputs.create_index === 'True' && $job.inputs.sort_order === 'Coordinate' && out_extension == \"BAM\")\n  {\n    \n    old_name = filename.split('.').slice(0, -1).concat('sorted.bai').join('.').replace(/^.*[\\\\\\/]/, '')\n    new_name = filename.split('.').slice(0, -1).concat('sorted.bam.bai').join('.').replace(/^.*[\\\\\\/]/, '')\n    return \"; mv \" + \" \" + old_name + \" \" + new_name\n  }\n\n}",
                            "engine": "#cwl-js-engine"
                        },
                        "separate": true
                    }
                ],
                "sbg:job": {
                    "allocatedResources": {
                        "cpu": 1,
                        "mem": 2048
                    },
                    "inputs": {
                        "memory_per_job": 2048,
                        "output_type": null,
                        "create_index": null,
                        "sort_order": "Coordinate",
                        "input_bam": {
                            "path": "/root/dir/example.tested.bam"
                        }
                    }
                }
            }
        }
    ],
    "sbg:validationErrors": [],
    "outputs": [
        {
            "source": [
                "#Delly2_1.output"
            ],
            "sbg:x": 981.1111362834023,
            "id": "#output",
            "sbg:y": 204.44445224455805,
            "sbg:includeInPorts": true,
            "label": "output",
            "type": [
                "null",
                "File"
            ]
        }
    ],
    "requirements": [],
    "sbg:license": "GNU General Public License v3.0 only",
    "hints": [],
    "sbg:projectName": "qa-load-2017-07-31-18",
    "sbg:toolkitVersion": "0.7.1",
    "sbg:modifiedOn": 1501518622,
    "id": "https://api.sbgenomics.com/v2/apps/bixqa/qa-load-2017-07-31-18/delly2-workflow-0-7-1/0/raw/",
    "sbg:appVersion": [
        "sbg:draft-2"
    ],
    "sbg:project": "bixqa/qa-load-2017-07-31-18",
    "sbg:canvas_x": 9,
    "sbg:createdOn": 1501518622,
    "class": "Workflow",
    "sbg:canvas_zoom": 0.8999999999999999,
    "sbg:links": [
        {
            "id": "https://github.com/tobiasrausch/delly",
            "label": "Home Page"
        },
        {
            "id": "http://bioinformatics.oxfordjournals.org/content/28/18/i333.abstract",
            "label": "Publication"
        },
        {
            "id": "https://github.com/tobiasrausch/delly/blob/master/README.md",
            "label": "Manual"
        }
    ],
    "sbg:revisionNotes": null,
    "sbg:modifiedBy": "bixqa",
    "sbg:id": "bixqa/qa-load-2017-07-31-18/delly2-workflow-0-7-1/0",
    "sbg:toolAuthor": "Tobias Rausch",
    "sbg:toolkit": "Delly2",
    "sbg:canvas_y": 21,
    "cwlVersion": "sbg:draft-2",
    "sbg:revisionsInfo": [
        {
            "sbg:revision": 0,
            "sbg:modifiedOn": 1501518622,
            "sbg:revisionNotes": null,
            "sbg:modifiedBy": "bixqa"
        }
    ],
    "description": "Delly is a tool for predicting structural variants, i.e. deletions, duplications, translocations and inversions. It integrates short insert paired-ends, long-range mate-pairs and split-read alignments to accurately delineate genomic rearrangements at single-nucleotide resolution.",
    "sbg:contributors": [
        "bixqa"
    ],
    "sbg:sbgMaintained": false,
    "sbg:revision": 0,
    "inputs": [
        {
            "type": [
                "null",
                {
                    "name": "exclude_preset",
                    "symbols": [
                        "None",
                        "Human HG19",
                        "Human HG38",
                        "Mouse MM10"
                    ],
                    "type": "enum"
                }
            ],
            "description": "Ready templates for telomere and centromere regions that can be excluded in order to save time. If an external exclude file is supplied this option will be ignored.",
            "id": "#exclude_preset",
            "label": "Exclude preset"
        },
        {
            "inputBinding": {
                "prefix": "-f",
                "sbg:cmdInclude": true,
                "position": 3,
                "valueFrom": {
                    "class": "Expression",
                    "script": "{\n  if (!$job.inputs.flanking)\n  {\n    return 90\n  }\n  else\n  {\n    return $job.inputs.flanking\n  }\n}\n",
                    "engine": "#cwl-js-engine"
                },
                "separate": true
            },
            "type": [
                "null",
                "int"
            ],
            "description": "Quality of the consensus alignment.",
            "id": "#flanking",
            "label": "Flanking"
        },
        {
            "inputBinding": {
                "prefix": "-u",
                "sbg:cmdInclude": true,
                "position": 7,
                "valueFrom": {
                    "class": "Expression",
                    "script": "{\n  if (!$job.inputs.geno_qual)\n  {\n    return 5\n  }\n  else\n  {\n    return $job.inputs.geno_qual\n  }\n}\n",
                    "engine": "#cwl-js-engine"
                },
                "separate": true
            },
            "type": [
                "null",
                "int"
            ],
            "description": "Min mapping quality for genotyping.",
            "id": "#geno_qual",
            "label": "Genotyping quality"
        },
        {
            "inputBinding": {
                "prefix": "-i",
                "sbg:cmdInclude": true,
                "position": 6,
                "valueFrom": {
                    "class": "Expression",
                    "script": "{\n  if (!$job.inputs.indel_size)\n  {\n    return 500\n  }\n  else\n  {\n    return $job.inputs.indel_size\n  }\n}\n",
                    "engine": "#cwl-js-engine"
                },
                "separate": true
            },
            "type": [
                "null",
                "int"
            ],
            "description": "Max small InDel size",
            "id": "#indel_size",
            "label": "InDel size"
        },
        {
            "inputBinding": {
                "prefix": "-s",
                "sbg:cmdInclude": true,
                "position": 2,
                "valueFrom": {
                    "class": "Expression",
                    "script": "{\n  if (!$job.inputs.mad_cutoff)\n  {\n    return 9\n  }\n  else\n  {\n    return $job.inputs.mad_cutoff\n  }\n}\n",
                    "engine": "#cwl-js-engine"
                },
                "separate": true
            },
            "type": [
                "null",
                "int"
            ],
            "description": "Insert size cutoff, median+s*MAD (deletions only).",
            "id": "#mad_cutoff",
            "label": "MAD Cutoff"
        },
        {
            "inputBinding": {
                "prefix": "-q",
                "sbg:cmdInclude": true,
                "position": 1,
                "valueFrom": {
                    "class": "Expression",
                    "script": "{\n  if (!$job.inputs.map_qual)\n  {\n    return 1\n  }\n  else\n  {\n    return $job.inputs.map_qual\n  }\n}\n",
                    "engine": "#cwl-js-engine"
                },
                "separate": true
            },
            "type": [
                "null",
                "int"
            ],
            "description": "Minimum paired-end mapping quality.",
            "id": "#map_qual",
            "label": "Min mapping quality"
        },
        {
            "inputBinding": {
                "prefix": "-m",
                "sbg:cmdInclude": true,
                "position": 4,
                "valueFrom": {
                    "class": "Expression",
                    "script": "{\n  if (!$job.inputs.min_flank)\n  {\n    return 13\n  }\n  else\n  {\n    return $job.inputs.min_flank\n  }\n}\n",
                    "engine": "#cwl-js-engine"
                },
                "separate": true
            },
            "type": [
                "null",
                "int"
            ],
            "description": "Minimum flanking sequence size.",
            "id": "#min_flank",
            "label": "Min flanking size"
        },
        {
            "inputBinding": {
                "prefix": "-n",
                "sbg:cmdInclude": true,
                "position": 5,
                "separate": true
            },
            "type": [
                "null",
                "boolean"
            ],
            "description": "No small InDel calling",
            "id": "#noindels",
            "label": "No InDels"
        },
        {
            "description": "Choose structural variant analysis that will be performed.",
            "id": "#type",
            "sbg:suggestedValue": "DEL",
            "inputBinding": {
                "prefix": "-t",
                "sbg:cmdInclude": true,
                "position": 0,
                "separate": true
            },
            "type": [
                "null",
                {
                    "name": "type",
                    "symbols": [
                        "DEL",
                        "DUP",
                        "INV",
                        "TRA",
                        "INS"
                    ],
                    "type": "enum"
                }
            ],
            "label": "Type"
        },
        {
            "type": [
                "null",
                "File"
            ],
            "sbg:x": 82,
            "id": "#exclude",
            "sbg:y": 332,
            "label": "exclude"
        },
        {
            "type": [
                "File"
            ],
            "sbg:x": 82.22223264199684,
            "id": "#input_bam",
            "sbg:y": 468.8888998855773,
            "label": "#input_bam"
        },
        {
            "type": [
                "File"
            ],
            "sbg:x": 78.88889991501199,
            "id": "#genome",
            "sbg:y": 191.11111023396614,
            "label": "genome"
        },
        {
            "type": [
                "null",
                "File"
            ],
            "sbg:x": 76.66667497305197,
            "id": "#vcfgeno",
            "sbg:y": 50.00000267852961,
            "label": "vcfgeno"
        }
    ],
    "sbg:latestRevision": 0,
    "label": "Delly2 Workflow"
}