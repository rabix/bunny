{
    "class": "Workflow",
    "sbg:appVersion": [
        "sbg:draft-2"
    ],
    "sbg:createdBy": "bixqa",
    "sbg:canvas_x": -61,
    "sbg:latestRevision": 22,
    "id": "https://api.sbgenomics.com/v2/apps/bixqa/qa-load-2017-07-31-18/samtools-mpileup-parallel/22/raw/",
    "sbg:revision": 22,
    "sbg:canvas_zoom": 1,
    "sbg:canvas_y": -51,
    "sbg:contributors": [
        "bixqa"
    ],
    "sbg:copyOf": "admin/sbg-public-data/samtools-mpileup-parallel/22",
    "requirements": [],
    "description": "The purpose of this workflow is to allow user to execute the SAMtools Mpileup tool in parallel using all of the instance resources. \n\nInstead of analyzing whole BAM at once, this workflow obtains contigs information from FASTA reference index (FAI). This file contains a list of contigs where pileup or BCF should be generated. This is done by using parameter (-I or --positions FILE) of the SAMtools Mpileup tool. Jobs will be then executed per chromosome on each of the available core of the instance, while all small contigs will be executed together in a single job on one core.\n\nAs a result, execution time will be order of magnitude faster compared to the execution of the whole BAM at once. Also, output of the workflow can be an integral file for the whole BAM, or list of files per chromosome which is defined by the parameter \"output_state\". Finally, user can choose format of the output file or list of files by the SAMtools mpileup parameter output_type. Available output formats are PILEUP, VCF and BCF.\n\n######Required inputs:\n\n- Reference file-The faidx-indexed reference file in the FASTA format.\n\n- BAM file-One or a list of input BAM files.\n\n- FAI file-One of these two is required input. FAI file is used to extract intervals that will be used to scatter the SAMtools mpileup job. FAI file can be generated using SAMtools faidx. \n\n- Merging mode-This defines the way this pipeline outputs its results. Options are:\n      - \"Merge\": to produce VCF, PILEUP, or BCF file acquired after merging results of the scattered pileup.\n      - \"Pass nonempty files\"  to pass through all nonempty PILEUP, VF, BCF files without merging.\n      - \"Pass all files\": to output all PILEUP, VF, BCF Separately.\n\n- Output file format -This parameter can be used to change the output format (available choices are VCF/BCF and pileup)\n\n###### Outputs:\nThis pipeline has one output port and the output depends on the selected input parameters (merge mode and output file format). Please refer to these parameters for more details.\n\n###### Common Issues\nBAM files should be sorted by coordinates before using SAMmpileup.",
    "outputs": [
        {
            "sbg:x": 1257,
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#SBG_SAMtools_Merge_Mpileup.output_file"
            ],
            "label": "output_pileup",
            "id": "#output_file",
            "required": false,
            "sbg:y": 447,
            "sbg:includeInPorts": true
        }
    ],
    "sbg:createdOn": 1501518694,
    "sbg:categories": [
        "SAM/BAM-Processing"
    ],
    "sbg:image_url": "https://brood.sbgenomics.com/static/bixqa/qa-load-2017-07-31-18/samtools-mpileup-parallel/22.png",
    "sbg:projectName": "qa-load-2017-07-31-18",
    "sbg:toolkit": "SAMtools",
    "steps": [
        {
            "sbg:x": 924,
            "outputs": [
                {
                    "id": "#SAMtools_Mpileup.output_pileup_vcf_or_bcf_file"
                }
            ],
            "id": "#SAMtools_Mpileup",
            "scatter": "#SAMtools_Mpileup.chr_pos_list_file",
            "sbg:y": 361,
            "run": {
                "sbg:cmdPreview": "/opt/samtools-1.3/samtools mpileup    input_bam_file1.bam  input_bam_file2.bam   | awk '{if($4 != 0) print $0}'  > input_bam_file1.pileup",
                "sbg:createdBy": "marouf",
                "sbg:latestRevision": 19,
                "stdout": "",
                "sbg:revision": 19,
                "x": 924,
                "temporaryFailCodes": [],
                "sbg:modifiedOn": 1482334695,
                "class": "CommandLineTool",
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
                "description": "SAMtools Mpileup generates BCF or PILEUP for one or multiple BAM files. Alignment records are grouped by sample identifiers in @RG header lines. If sample identifiers are absent, each input file is regarded as one sample.\n\nIn the pileup format (without -uor-g), each line represents a genomic position consisting of chromosome name, coordinate, reference base, read bases, read qualities, and alignment mapping qualities. Information on match, mismatch, indel, strand, mapping quality, and the start and end of a read are all encoded at the read base column. In this column, a dot stands for a match to the reference base on the forward strand, a comma for a match on the reverse strand, a '>' or '<' for a reference skip, 'ACGTN' for a mismatch on the forward strand, and 'acgtn' for a mismatch on the reverse strand. A pattern `\\\\+[0-9]+[ACGTNacgtn]+' indicates there is an insertion between this reference position and the next reference position. The length of the insertion is given by the integer in the pattern followed by the inserted sequence. Similarly, a pattern '-[0-9]+[ACGTNacgtn]+' represents a deletion from the reference. The deleted bases will be presented as `*' in the following lines. Also at the read base column, a symbol '^', marks the start of a read. The ASCII of the character following '^' minus 33 gives the mapping quality. The symbol '$' marks the end of a read segment.\n\n#### Common Issue:\n- Please use the public pileup parallel pipeline for large input files.\n- Please sort BAM files by coordinates before using mpileup.",
                "y": 361,
                "outputs": [
                    {
                        "description": "Output PILEUP, VCF, or BCF file.",
                        "type": [
                            "null",
                            "File"
                        ],
                        "outputBinding": {
                            "glob": "{*.pileup,*.bcf,*.vcf}",
                            "streamable": false,
                            "sbg:metadata": {
                                "ScatteredUsing": {
                                    "class": "Expression",
                                    "script": "{\n\tif ($job.inputs.chr_pos_list_file)\n    {\n\t    len =  $job.inputs.chr_pos_list_file.path.split('/').length\n        \n        return $job.inputs.chr_pos_list_file.path.split('/')[len-1].split('.')[0]\n    }\n  \telse\n    {\n\t    return \n    }\n}",
                                    "engine": "#cwl-js-engine"
                                }
                            },
                            "sbg:inheritMetadataFrom": "#input_bam_files"
                        },
                        "label": "Output PILEUP, VCF, or BCF file",
                        "id": "#output_pileup_vcf_or_bcf_file",
                        "sbg:fileTypes": "PILEUP,BCF,VCF"
                    }
                ],
                "sbg:job": {
                    "allocatedResources": {
                        "cpu": 1,
                        "mem": 1000
                    },
                    "inputs": {
                        "filter_zero_coverage_lines": true,
                        "region_pileup_generation": "region_pileup_generation",
                        "required_flags": 0,
                        "recalc_BAQ_fly": true,
                        "parameter_for_adjusting_mapq": 11,
                        "max_idepth": 11,
                        "gap_open_sequencing_error_probability": 11,
                        "output_uncompressed_bcf_or_vcf": true,
                        "ignore_overlaps": true,
                        "output_base_positions_on_reads": true,
                        "skip_bases_with_baq_smaller_than_defined": 11,
                        "gap_extension_seq_error_probability": 11,
                        "output_format": "PILEUP",
                        "homopolymer_err_coeficient": 11,
                        "mapq_threshold": 11,
                        "assume_the_quality_Illumina_encoding": true,
                        "more_info_to_output": "more_info_to_output-string-value",
                        "max_per_bam_depth": 11,
                        "ignore_rg_tags": true,
                        "comma_separated_list_of_platforms_for_indels": "all",
                        "exclude_read_groups_list_file": {
                            "class": "File",
                            "path": "exclude_read_groups_list_file.txt",
                            "size": 0,
                            "secondaryFiles": []
                        },
                        "min_gapped_reads_for_indel": 11,
                        "no_indel_calling": true,
                        "per_sample_mF": true,
                        "disable_baq_computation": true,
                        "input_bam_files": [
                            {
                                "class": "File",
                                "path": "input_bam_file1.bam",
                                "size": 0,
                                "secondaryFiles": []
                            },
                            {
                                "path": "input_bam_file2.bam"
                            }
                        ],
                        "minimum_fraction_of_gapped_reads": 11,
                        "count_anomalous_read_pairs": true,
                        "filter_flags": "filter_flags-string-value",
                        "reference_fasta_file": {
                            "path": "input/input.fasta",
                            "secondaryFiles": [
                                {
                                    "path": ".fai"
                                }
                            ]
                        },
                        "output_mapping_quality": true
                    }
                },
                "sbg:contributors": [
                    "milan.domazet.sudo",
                    "marouf"
                ],
                "sbg:license": "The MIT License",
                "sbg:image_url": null,
                "sbg:toolkit": "SAMtools",
                "arguments": [
                    {
                        "separate": true,
                        "valueFrom": {
                            "class": "Expression",
                            "script": "{\n\n if($job.inputs.output_format)\n {\n    if($job.inputs.output_format == \"PILEUP\")\n        return\n    else if($job.inputs.output_format == \"VCF\")\n    \treturn \"-v\"\n    else if($job.inputs.output_format == \"BCF\")\n    \treturn \"-g\"\n    else\n      return\n  \n \n }\n\n\n\n}",
                            "engine": "#cwl-js-engine"
                        }
                    },
                    {
                        "position": 1000,
                        "separate": true,
                        "valueFrom": {
                            "class": "Expression",
                            "script": "{\n\n // Check if input files are delivered in an array or not\n if($job.inputs.input_bam_files.constructor == Array)\n {\n   // Select the first entry of the array\n\u2002\u2002\u2002\u2002filepath = \"/\"+$job.inputs.input_bam_files[0].path \n\u2002\u2002} \n  \n  else\n    \n  {\n    // Use the file name directly\n\u2002\u2002\u2002\u2002filepath = \"/\"+$job.inputs.input_bam_files.path\n \u2002}\n   \n \n filename = filepath.split(\"/\").pop()\n new_filename = filename.substr(0,filename.lastIndexOf(\".\"))\n    \n \n if ($job.inputs.chr_pos_list_file)\n   new_filename = new_filename+'_'+ $job.inputs.chr_pos_list_file.path.split('/')[$job.inputs.chr_pos_list_file.path.split('/').length-1].split('.')[0]\n\n \n extension = '.pileup'\n  \n if ( (typeof $job.inputs.output_format !== \"undefined\" && $job.inputs.output_format === \"BCF\") )\n   extension = '.bcf'\n             \n else if ((typeof $job.inputs.output_format !== \"undefined\" && $job.inputs.output_format === \"VCF\") )  \n   extension = '.vcf' \n  \n if($job.inputs.filter_zero_coverage_lines && extension== '.pileup')   \n \treturn  \" | awk '{if($4 != 0) print $0}'  > \"+  new_filename + extension\n else\n   return  \" > \"+  new_filename + extension\n}",
                            "engine": "#cwl-js-engine"
                        }
                    }
                ],
                "hints": [
                    {
                        "class": "DockerRequirement",
                        "dockerImageId": "2fb927277493",
                        "dockerPull": "images.sbgenomics.com/marouf/samtools:1.3"
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
                "sbg:createdOn": 1458812970,
                "sbg:categories": [
                    "SAM/BAM-Processing"
                ],
                "sbg:project": "marouf/samtools-1-3-demo",
                "sbg:validationErrors": [],
                "baseCommand": [
                    "/opt/samtools-1.3/samtools",
                    "mpileup"
                ],
                "label": "SAMtools Mpileup",
                "cwlVersion": "sbg:draft-2",
                "sbg:revisionNotes": "Added inherit metadata from input.",
                "sbg:modifiedBy": "milan.domazet.sudo",
                "sbg:toolkitVersion": "v1.3",
                "successCodes": [],
                "sbg:links": [
                    {
                        "label": "Homepage",
                        "id": "http://samtools.sourceforge.net/"
                    },
                    {
                        "label": "Source code",
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
                "sbg:revisionsInfo": [
                    {
                        "sbg:revisionNotes": null,
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 0,
                        "sbg:modifiedOn": 1458812970
                    },
                    {
                        "sbg:revisionNotes": null,
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 1,
                        "sbg:modifiedOn": 1458825811
                    },
                    {
                        "sbg:revisionNotes": null,
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 2,
                        "sbg:modifiedOn": 1462805320
                    },
                    {
                        "sbg:revisionNotes": null,
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 3,
                        "sbg:modifiedOn": 1468578873
                    },
                    {
                        "sbg:revisionNotes": null,
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 4,
                        "sbg:modifiedOn": 1468580666
                    },
                    {
                        "sbg:revisionNotes": null,
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 5,
                        "sbg:modifiedOn": 1468586077
                    },
                    {
                        "sbg:revisionNotes": null,
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 6,
                        "sbg:modifiedOn": 1468924014
                    },
                    {
                        "sbg:revisionNotes": null,
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 7,
                        "sbg:modifiedOn": 1473160925
                    },
                    {
                        "sbg:revisionNotes": "Peer review",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 8,
                        "sbg:modifiedOn": 1473433741
                    },
                    {
                        "sbg:revisionNotes": null,
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 9,
                        "sbg:modifiedOn": 1473434123
                    },
                    {
                        "sbg:revisionNotes": null,
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 10,
                        "sbg:modifiedOn": 1473771444
                    },
                    {
                        "sbg:revisionNotes": "change positional argument for BCF output",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 11,
                        "sbg:modifiedOn": 1473779051
                    },
                    {
                        "sbg:revisionNotes": "Delete default value inserted mistakenly for some parameters.",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 12,
                        "sbg:modifiedOn": 1473849264
                    },
                    {
                        "sbg:revisionNotes": null,
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 13,
                        "sbg:modifiedOn": 1473931426
                    },
                    {
                        "sbg:revisionNotes": "add new common issue",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 14,
                        "sbg:modifiedOn": 1474901461
                    },
                    {
                        "sbg:revisionNotes": "Add filter to pileup files",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 15,
                        "sbg:modifiedOn": 1478526836
                    },
                    {
                        "sbg:revisionNotes": "change the test case",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 16,
                        "sbg:modifiedOn": 1478526922
                    },
                    {
                        "sbg:revisionNotes": "update description",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 17,
                        "sbg:modifiedOn": 1480075403
                    },
                    {
                        "sbg:revisionNotes": "output is not required field anymore",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 18,
                        "sbg:modifiedOn": 1480440302
                    },
                    {
                        "sbg:revisionNotes": "Added inherit metadata from input.",
                        "sbg:modifiedBy": "milan.domazet.sudo",
                        "sbg:revision": 19,
                        "sbg:modifiedOn": 1482334695
                    }
                ],
                "stdin": "",
                "id": "marouf/samtools-1-3-demo/mpileup-1-3/19",
                "sbg:sbgMaintained": false,
                "sbg:id": "admin/sbg-public-data/mpileup-1-3/19",
                "sbg:toolAuthor": "Heng Li, Sanger Institute",
                "inputs": [
                    {
                        "description": "Minimum base quality for a base to be considered.",
                        "type": [
                            "null",
                            "int"
                        ],
                        "inputBinding": {
                            "separator": " ",
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-Q"
                        },
                        "sbg:category": "Input options",
                        "label": "Skip bases with baseQ/BAQ smaller than",
                        "id": "#skip_bases_with_baq_smaller_than_defined",
                        "sbg:toolDefaultValue": "13"
                    },
                    {
                        "description": "Required flags: skip reads with mask bits unset.",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Input options",
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "--rf"
                        },
                        "label": "Required flags",
                        "id": "#required_flags"
                    },
                    {
                        "description": "Region in which pileup is generated.",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Input options",
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-r"
                        },
                        "label": "Region in which pileup is generated",
                        "id": "#region_pileup_generation"
                    },
                    {
                        "description": "Faidx indexed reference sequence (FASTA) file.",
                        "type": [
                            "null",
                            "File"
                        ],
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-f",
                            "secondaryFiles": [
                                ".fai"
                            ]
                        },
                        "sbg:category": "File input",
                        "label": "Reference FASTA file",
                        "id": "#reference_fasta_file",
                        "required": false,
                        "sbg:fileTypes": "FASTA,FA,GZ",
                        "schema": [
                            "null",
                            "File"
                        ]
                    },
                    {
                        "description": "Recalculate BAQ on the fly, ignore existing BQ tags.",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:category": "Input options",
                        "inputBinding": {
                            "separator": " ",
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-E"
                        },
                        "label": "Recalculate BAQ on the fly",
                        "id": "#recalc_BAQ_fly"
                    },
                    {
                        "description": "Apply -m and -F thresholds per sample to increase sensitivity of calling. By default both options are applied to reads pooled from all samples.",
                        "sbg:stageInput": null,
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-p"
                        },
                        "sbg:category": "configuration",
                        "label": "Apply -m and -F thresholds per sample",
                        "id": "#per_sample_mF"
                    },
                    {
                        "description": "Coefficient for downgrading mapping quality for reads containing excessive mismatches. Given a read with a phred-scaled probability q of being generated from the mapped position, the new mapping quality is about sqrt((INT-q)/INT)*INT. A zero value disables this functionality; if enabled, the recommended value for BWA is 50.",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Input options",
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-C"
                        },
                        "label": "Parameter for adjusting mapQ",
                        "id": "#parameter_for_adjusting_mapq"
                    },
                    {
                        "description": "Similar to -g except that the output is uncompressed BCF, which is preferred for piping.",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:category": "Output options",
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-u"
                        },
                        "label": "Generate uncompress BCF/VCF output",
                        "id": "#output_uncompressed_bcf_or_vcf"
                    },
                    {
                        "description": "Output mapping quality (disabled by -g/-u).",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:category": "Output options",
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-s"
                        },
                        "label": "Output mapping quality",
                        "id": "#output_mapping_quality"
                    },
                    {
                        "label": "Output file format",
                        "id": "#output_format",
                        "description": "Output file format.",
                        "type": [
                            {
                                "type": "enum",
                                "name": "output_format",
                                "symbols": [
                                    "PILEUP",
                                    "BCF",
                                    "VCF"
                                ]
                            }
                        ],
                        "sbg:category": "configuration"
                    },
                    {
                        "description": "Output base positions on reads (disabled by -g/-u).",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:category": "Output options",
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-O"
                        },
                        "label": "Output base positions on reads",
                        "id": "#output_base_positions_on_reads"
                    },
                    {
                        "description": "Do not perform indel calling.",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:category": "SNP/INDEL genotype likelihoods options",
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-I"
                        },
                        "label": "Do not perform indel calling",
                        "id": "#no_indel_calling"
                    },
                    {
                        "description": "Comma-separated list of FORMAT and INFO tags (DP,AD,ADF,ADR,SP,INFO/AD,INFO/ADF,INFO/ADR []\") to output.",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "configuration",
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-t",
                            "itemSeparator": null
                        },
                        "label": "Comma-separated list of FORMAT and INFO tags to output",
                        "id": "#more_info_to_output"
                    },
                    {
                        "description": "Minimum fraction of gapped reads for candidates.",
                        "type": [
                            "null",
                            "float"
                        ],
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-F"
                        },
                        "sbg:category": "SNP/INDEL genotype likelihoods options",
                        "label": "Minimum fraction of gapped reads for candidates",
                        "id": "#minimum_fraction_of_gapped_reads",
                        "sbg:toolDefaultValue": "0.002"
                    },
                    {
                        "description": "Minimum gapped reads for indel candidates.",
                        "type": [
                            "null",
                            "int"
                        ],
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-m"
                        },
                        "sbg:category": "SNP/INDEL genotype likelihoods options",
                        "label": "Minimum gapped reads for indel candidates",
                        "id": "#min_gapped_reads_for_indel",
                        "sbg:toolDefaultValue": "1"
                    },
                    {
                        "description": "At a position, read maximally INT reads per input BAM.",
                        "type": [
                            "null",
                            "int"
                        ],
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-d"
                        },
                        "sbg:category": "Input options",
                        "label": "Max per-BAM depth",
                        "id": "#max_per_bam_depth",
                        "sbg:toolDefaultValue": "250"
                    },
                    {
                        "description": "Skip INDEL calling if the average per-sample depth is above.",
                        "type": [
                            "null",
                            "int"
                        ],
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-L"
                        },
                        "sbg:category": "configuration",
                        "label": "Skip INDEL calling if the average per-sample depth is above",
                        "id": "#max_idepth",
                        "sbg:toolDefaultValue": "250",
                        "schema": [
                            "null",
                            "int"
                        ]
                    },
                    {
                        "description": "Minimum mapping quality for an alignment to be used.",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Input options",
                        "inputBinding": {
                            "separator": " ",
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-q"
                        },
                        "label": "Skip alignments with mapQ  smaller than",
                        "id": "#mapq_threshold"
                    },
                    {
                        "description": "List of input BAM files.",
                        "sbg:stageInput": "link",
                        "type": [
                            {
                                "items": "File",
                                "type": "array",
                                "name": "input_bam_files"
                            }
                        ],
                        "inputBinding": {
                            "position": 1,
                            "separate": true,
                            "streamable": false,
                            "sbg:cmdInclude": true,
                            "separator": " ",
                            "itemSeparator": null
                        },
                        "sbg:category": "Input options",
                        "label": "List of input BAM files",
                        "id": "#input_bam_files",
                        "required": true,
                        "sbg:fileTypes": "BAM",
                        "schema": [
                            "File"
                        ]
                    },
                    {
                        "description": "Ignore rg tags.",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:category": "Input options",
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-R"
                        },
                        "label": "Ignore RG tags",
                        "id": "#ignore_rg_tags"
                    },
                    {
                        "description": "Disable read-pair overlap detection.",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:category": "configuration",
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-x"
                        },
                        "label": "Disable read-pair overlap detection",
                        "id": "#ignore_overlaps"
                    },
                    {
                        "description": "Coefficient for modeling homopolymer errors. Given an l-long homopolymer run, the sequencing error of an indel of size s is modeled as INT*s/l.",
                        "type": [
                            "null",
                            "int"
                        ],
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-h"
                        },
                        "sbg:category": "SNP/INDEL genotype likelihoods options",
                        "label": "Coefficient for homopolymer errors",
                        "id": "#homopolymer_err_coeficient",
                        "sbg:toolDefaultValue": "100"
                    },
                    {
                        "description": "Phred-scaled gap open sequencing error probability. Reducing INT leads to more indel calls.",
                        "type": [
                            "null",
                            "int"
                        ],
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-o"
                        },
                        "sbg:category": "SNP/INDEL genotype likelihoods options",
                        "label": "Phred-scaled gap open sequencing error probability",
                        "id": "#gap_open_sequencing_error_probability",
                        "sbg:toolDefaultValue": "40"
                    },
                    {
                        "description": "Phred-scaled gap extension sequencing error probability. Reducing INT leads to longer indels.",
                        "type": [
                            "null",
                            "int"
                        ],
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-e"
                        },
                        "sbg:category": "SNP/INDEL genotype likelihoods options",
                        "label": "Phred-scaled gap extension seq error probability",
                        "id": "#gap_extension_seq_error_probability",
                        "sbg:toolDefaultValue": "20"
                    },
                    {
                        "description": "Filter zero coverage lines from pileup files. This option is valid only when output pileup files.",
                        "sbg:stageInput": null,
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:category": "output configuration",
                        "label": "Filter zero coverage lines.",
                        "id": "#filter_zero_coverage_lines"
                    },
                    {
                        "description": "Filter flags: skip reads with mask bits set [UNMAP,SECONDARY,QCFAIL,DUP].",
                        "sbg:stageInput": null,
                        "type": [
                            "null",
                            "string"
                        ],
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "--ff"
                        },
                        "sbg:category": "Input options",
                        "label": "Filter flags",
                        "id": "#filter_flags"
                    },
                    {
                        "description": "Exclude read groups listed in file.",
                        "type": [
                            "null",
                            "File"
                        ],
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-G"
                        },
                        "sbg:category": "File input",
                        "label": "Exclude read groups listed in file",
                        "id": "#exclude_read_groups_list_file",
                        "required": false,
                        "sbg:fileTypes": "TXT"
                    },
                    {
                        "description": "Disable probabilistic realignment for the computation of base alignment quality (BAQ). BAQ is the Phred-scaled probability of a read base being misaligned. Applying this option greatly helps to reduce false SNPs caused by misalignments.",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:category": "Input options",
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-B"
                        },
                        "label": "Disable BAQ computation",
                        "id": "#disable_baq_computation"
                    },
                    {
                        "description": "Do not skip anomalous read pairs in variant calling.",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:category": "Input options",
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-A"
                        },
                        "label": "Count anomalous read pairs",
                        "id": "#count_anomalous_read_pairs"
                    },
                    {
                        "description": "Comma dilimited list of platforms (determined by @RG-PL) from which indel candidates are obtained. It is recommended to collect indel candidates from sequencing technologies that have low indel error rate such as ILLUMINA.",
                        "type": [
                            "null",
                            "string"
                        ],
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-P"
                        },
                        "sbg:category": "SNP/INDEL genotype likelihoods options",
                        "label": "Comma separated list of platforms for indels",
                        "id": "#comma_separated_list_of_platforms_for_indels",
                        "sbg:toolDefaultValue": "\"all\""
                    },
                    {
                        "description": "BED or position list file containing a list of regions or sites where pileup or BCF should be generated.",
                        "type": [
                            "null",
                            "File"
                        ],
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-l"
                        },
                        "sbg:category": "File input",
                        "label": "List of positions (chr pos) or regions in BED file",
                        "id": "#chr_pos_list_file",
                        "required": false,
                        "sbg:fileTypes": "BED"
                    },
                    {
                        "description": "Assume the quality is in the Illumina-1.3+ encoding.",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:category": "Input options",
                        "inputBinding": {
                            "position": 0,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "-6"
                        },
                        "label": "Assume the quality is in the Illumina-1.3+ encoding",
                        "id": "#assume_the_quality_Illumina_encoding"
                    }
                ]
            },
            "inputs": [
                {
                    "source": [
                        "#skip_bases_with_baq_smaller_than_defined"
                    ],
                    "id": "#SAMtools_Mpileup.skip_bases_with_baq_smaller_than_defined"
                },
                {
                    "source": [
                        "#required_flags"
                    ],
                    "id": "#SAMtools_Mpileup.required_flags"
                },
                {
                    "source": [
                        "#region_pileup_generation"
                    ],
                    "id": "#SAMtools_Mpileup.region_pileup_generation"
                },
                {
                    "source": [
                        "#reference_fasta"
                    ],
                    "id": "#SAMtools_Mpileup.reference_fasta_file"
                },
                {
                    "source": [
                        "#recalc_BAQ_fly"
                    ],
                    "id": "#SAMtools_Mpileup.recalc_BAQ_fly"
                },
                {
                    "source": [
                        "#per_sample_mF"
                    ],
                    "id": "#SAMtools_Mpileup.per_sample_mF"
                },
                {
                    "source": [
                        "#parameter_for_adjusting_mapq"
                    ],
                    "id": "#SAMtools_Mpileup.parameter_for_adjusting_mapq"
                },
                {
                    "source": [
                        "#output_uncompressed_bcf_or_vcf"
                    ],
                    "id": "#SAMtools_Mpileup.output_uncompressed_bcf_or_vcf"
                },
                {
                    "source": [
                        "#output_mapping_quality"
                    ],
                    "id": "#SAMtools_Mpileup.output_mapping_quality"
                },
                {
                    "source": [
                        "#output_format"
                    ],
                    "id": "#SAMtools_Mpileup.output_format"
                },
                {
                    "source": [
                        "#output_base_positions_on_reads"
                    ],
                    "id": "#SAMtools_Mpileup.output_base_positions_on_reads"
                },
                {
                    "source": [
                        "#no_indel_calling"
                    ],
                    "id": "#SAMtools_Mpileup.no_indel_calling"
                },
                {
                    "source": [
                        "#more_info_to_output"
                    ],
                    "id": "#SAMtools_Mpileup.more_info_to_output"
                },
                {
                    "source": [
                        "#minimum_fraction_of_gapped_reads"
                    ],
                    "id": "#SAMtools_Mpileup.minimum_fraction_of_gapped_reads"
                },
                {
                    "source": [
                        "#min_gapped_reads_for_indel"
                    ],
                    "id": "#SAMtools_Mpileup.min_gapped_reads_for_indel"
                },
                {
                    "source": [
                        "#max_per_bam_depth"
                    ],
                    "id": "#SAMtools_Mpileup.max_per_bam_depth"
                },
                {
                    "source": [
                        "#max_idepth"
                    ],
                    "id": "#SAMtools_Mpileup.max_idepth"
                },
                {
                    "source": [
                        "#mapq_threshold"
                    ],
                    "id": "#SAMtools_Mpileup.mapq_threshold"
                },
                {
                    "source": [
                        "#bams"
                    ],
                    "id": "#SAMtools_Mpileup.input_bam_files"
                },
                {
                    "source": [
                        "#ignore_rg_tags"
                    ],
                    "id": "#SAMtools_Mpileup.ignore_rg_tags"
                },
                {
                    "source": [
                        "#ignore_overlaps"
                    ],
                    "id": "#SAMtools_Mpileup.ignore_overlaps"
                },
                {
                    "source": [
                        "#homopolymer_err_coeficient"
                    ],
                    "id": "#SAMtools_Mpileup.homopolymer_err_coeficient"
                },
                {
                    "source": [
                        "#gap_open_sequencing_error_probability"
                    ],
                    "id": "#SAMtools_Mpileup.gap_open_sequencing_error_probability"
                },
                {
                    "source": [
                        "#gap_extension_seq_error_probability"
                    ],
                    "id": "#SAMtools_Mpileup.gap_extension_seq_error_probability"
                },
                {
                    "source": [
                        "#filter_zero_coverage_lines"
                    ],
                    "id": "#SAMtools_Mpileup.filter_zero_coverage_lines"
                },
                {
                    "source": [
                        "#filter_flags"
                    ],
                    "id": "#SAMtools_Mpileup.filter_flags"
                },
                {
                    "id": "#SAMtools_Mpileup.exclude_read_groups_list_file"
                },
                {
                    "source": [
                        "#disable_baq_computation"
                    ],
                    "id": "#SAMtools_Mpileup.disable_baq_computation"
                },
                {
                    "source": [
                        "#count_anomalous_read_pairs"
                    ],
                    "id": "#SAMtools_Mpileup.count_anomalous_read_pairs"
                },
                {
                    "source": [
                        "#comma_separated_list_of_platforms_for_indels"
                    ],
                    "id": "#SAMtools_Mpileup.comma_separated_list_of_platforms_for_indels"
                },
                {
                    "source": [
                        "#SBG_Prepare_Intervals.intervals"
                    ],
                    "id": "#SAMtools_Mpileup.chr_pos_list_file"
                },
                {
                    "source": [
                        "#assume_the_quality_Illumina_encoding"
                    ],
                    "id": "#SAMtools_Mpileup.assume_the_quality_Illumina_encoding"
                }
            ]
        },
        {
            "sbg:x": 782,
            "outputs": [
                {
                    "id": "#SBG_Prepare_Intervals.names"
                },
                {
                    "id": "#SBG_Prepare_Intervals.intervals"
                }
            ],
            "id": "#SBG_Prepare_Intervals",
            "sbg:y": 494,
            "run": {
                "sbg:cmdPreview": "python sbg_prepare_intervals.py  --format \"chr start end\" --mode 3",
                "sbg:createdBy": "vladimirk",
                "sbg:latestRevision": 4,
                "stdout": "",
                "sbg:revision": 4,
                "x": 782,
                "temporaryFailCodes": [],
                "sbg:modifiedOn": 1478525360,
                "class": "CommandLineTool",
                "requirements": [
                    {
                        "class": "CreateFileRequirement",
                        "fileDef": [
                            {
                                "filename": "sbg_prepare_intervals.py",
                                "fileContent": "\"\"\"\nUsage:\n    sbg_prepare_intervals.py [options] [--fastq FILE --bed FILE --mode INT --format STR --others STR]\n\nDescription:\n    Purpose of this tool is to split BED file into files based on the selected mode.\n    If bed file is not provided fai(fasta index) file is converted to bed.\n\nOptions:\n\n    -h, --help            Show this message.\n\n    -v, -V, --version     Tool version.\n\n    -b, -B, --bed FILE    Path to input bed file.\n\n    --fai FILE            Path to input fai file.\n\n    --format STR          Output file format.\n\n    --mode INT            Select input mode.\n\n\"\"\"\n\n\nfrom docopt import docopt\nimport os\nimport shutil\nimport glob\n\ndefault_extension = '.bed'  # for output files\n\n\n\ndef create_file(contents, contig_name, extension=default_extension):\n    \"\"\"function for creating a file for all intervals in a contig\"\"\"\n\n    new_file = open(\"Intervals/\" + contig_name + extension, \"w\")\n    new_file.write(contents)\n    new_file.close()\n\n\ndef add_to_file(line, name, extension=default_extension):\n    \"\"\"function for adding a line to a file\"\"\"\n\n    new_file = open(\"Intervals/\" + name + extension, \"a\")\n    if lformat == formats[1]:\n        sep = line.split(\"\\t\")\n        line = sep[0] + \":\" + sep[1] + \"-\" + sep[2]\n    new_file.write(line)\n    new_file.close()\n\n\ndef fai2bed(fai):\n    \"\"\"function to create a bed file from fai file\"\"\"\n\n    region_thr = 10000000  # threshold used to determine starting point accounting for telomeres in chromosomes\n    if not fai.rfind(\".fasta.fai\") == -1:\n        basename = fai[0:fai.rfind(\".fasta.fai\")]\n    else:\n        basename = fai[0:fai.rfind(\".\")]\n    with open(fai, \"r\") as ins:\n        new_array = []\n        for line in ins:\n            len_reg = int(line.split()[1])\n            cutoff = 0 if (len_reg < region_thr) else 0  # sd\\\\telomeres or start with 1\n            new_line = line.split()[0] + '\\t' + str(cutoff) + '\\t' + str(len_reg + cutoff)\n            new_array.append(new_line)\n    new_file = open(basename + \".bed\", \"w\")\n    new_file.write(\"\\n\".join(new_array))\n    return basename + \".bed\"\n\ndef chr_intervals(no_of_chrms = 23):\n    \"\"\"returns all possible designations for chromosome intervals\"\"\"\n    \n    chrms = []\n    for i in range(1, no_of_chrms):\n        chrms.append(\"chr\" + str(i))\n        chrms.append(str(i))\n    chrms.extend([\"x\", \"y\", \"chrx\", \"chry\"])\n    return chrms\n\n\ndef mode_1(orig_file):\n    \"\"\"mode 1: every line is a new file\"\"\"\n\n    with open(orig_file, \"r\") as ins:\n        prev = \"\"\n        counter = 0\n        names = []\n        for line in ins:\n            if line.split()[0] == prev:\n                counter += 1\n            else:\n                counter = 0\n            suffix = \"\" if (counter == 0) else \"_\" + str(counter)\n            create_file(line, line.split()[0] + suffix)\n            names.append(line.split()[0] + suffix)\n            prev = line.split()[0]\n\n        create_file(str(names), \"names\", extension=\".txt\")\n\ndef mode_2(orig_file, others_name):\n    \"\"\"mode 2: separate file is created for each chromosome, and one file is created for other intervals\"\"\"\n\n    chrms = chr_intervals()\n    names = []\n\n    with open(orig_file, 'r') as ins:\n        for line in ins:\n            name = line.split()[0]\n            if name.lower() in chrms:\n                name = name.lower()\n            else:\n                name = others_name\n            try:\n                add_to_file(line, name)\n                if not name in names:\n                    names.append(name)\n            except:\n                raise Exception(\"Couldn't create or write in the file in mode 2\")\n\n        create_file(str(names), \"names\", extension = \".txt\")\n\n\ndef mode_3(orig_file, extension=default_extension):\n    \"\"\"mode 3: input file is staged to output\"\"\"\n\n    orig_name = orig_file.split(\"/\")[len(orig_file.split(\"/\")) - 1]\n    output_file = r\"./Intervals/\" + orig_name[0:orig_name.rfind('.')] + extension\n\n    shutil.copyfile(orig_file, output_file)\n\n    names = [orig_name[0:orig_name.rfind('.')]]\n    create_file(str(names), \"names\", extension=\".txt\")\n\n\ndef mode_4(orig_file, others_name):\n    \"\"\"mode 4: every interval in chromosomes is in a separate file. Other intervals are in a single file\"\"\"\n\n    chrms = chr_intervals()\n    names = []\n\n    with open(orig_file, \"r\") as ins:\n        counter = {}\n        for line in ins:\n            name = line.split()[0].lower()\n            if name in chrms:\n                if name in counter:\n                    counter[name] += 1\n                else:\n                    counter[name] = 0\n                suffix = \"\" if (counter[name] == 0) else \"_\" + str(counter[name])\n                create_file(line, name + suffix)\n                names.append(name + suffix)\n                prev = name\n            else:\n                name = others_name\n                if not name in names:\n                    names.append(name)\n                try:\n                    add_to_file(line, name)\n                except:\n                    raise Exception(\"Couldn't create or write in the file in mode 4\")\n\n        create_file(str(names), \"names\", extension=\".txt\")\n\n\ndef prepare_intervals():\n    # reading input files and split mode from command line\n    args = docopt(__doc__, version='1.0')\n\n    bed_file = args['--bed']\n    fai_file = args['--fai']\n    split_mode = int(args['--mode'])\n\n    \n    # define file name for non-chromosomal contigs\n    others_name = 'others' \n\n    global formats, lformat\n    formats = [\"chr start end\", \"chr:start-end\"]\n    lformat = args['--format']\n    if lformat == None:\n        lformat = formats[0]\n    if not lformat in formats:\n        raise Exception('Unsuported interval format')\n\n    if not os.path.exists(r\"./Intervals\"):\n        os.mkdir(r\"./Intervals\")\n    else:\n        files = glob.glob(r\"./Intervals/*\")\n        for f in files:\n            os.remove(f)\n\n    # create variable input_file taking bed_file as priority\n    if bed_file:\n        input_file = bed_file\n    elif fai_file:\n        input_file = fai2bed(fai_file)\n    else:\n        raise Exception('No input files are provided')\n\n    # calling adequate split mode function\n    if split_mode == 1:\n        mode_1(input_file)\n    elif split_mode == 2:\n        mode_2(input_file, others_name)\n    elif split_mode == 3:\n        if bed_file:\n            mode_3(input_file)\n        else:\n            raise Exception('Bed file is required for mode 3')\n    elif split_mode == 4:\n        mode_4(input_file, others_name)\n    else:\n        raise Exception('Split mode value is not set')\n\n\nif __name__ == '__main__':\n    prepare_intervals()"
                            }
                        ]
                    },
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
                "description": "Depending on selected Split Mode value, output files are generated in accordance with description below:\n\n1. Whole Genome - The tool creates one interval file per line of the input BED(FAI) file.\nEach interval file contains a single line (one of the lines of BED(FAI) input file).\n\n2. Whole Genome with reduced number of jobs - For each contig(chromosome) a single file\nis created containing all the intervals corresponding to it .\nAll the intervals (lines) other than (chr1, chr2 ... chrY or 1, 2 ... Y) are saved as\n(\"others.bed\").\n\n3. Whole Exome - BED file is required for execution of this mode. If mode  3 is applied input is passed to the output.\n\n4. Whole Genome Hybrid - For each chromosome a single file is created for each interval.\nAll the intervals (lines) other than (chr1, chr2 ... chrY or 1, 2 ... Y) are saved as\n(\"others.bed\").",
                "y": 494,
                "outputs": [
                    {
                        "description": "File containing the names of created files.",
                        "type": [
                            "null",
                            "string"
                        ],
                        "outputBinding": {
                            "loadContents": true,
                            "outputEval": {
                                "class": "Expression",
                                "script": "{   \n content =  $self[0].contents.replace(/\\0/g, '')\n content = content.replace('[','')\n content = content.replace(']','')\n content = content.replace(/\\'/g, \"\")\n content = content.replace(/\\s/g, '')\n content_arr = content.split(\",\")\n\n return content_arr\n \n\n} ",
                                "engine": "#cwl-js-engine"
                            },
                            "glob": "Intervals/names.txt"
                        },
                        "label": "Output file names",
                        "id": "#names",
                        "sbg:fileTypes": "TXT"
                    },
                    {
                        "description": "Array of BED files genereted as per selected Split Mode.",
                        "type": [
                            "null",
                            {
                                "items": "File",
                                "type": "array",
                                "name": "intervals"
                            }
                        ],
                        "outputBinding": {
                            "sbg:metadata": {
                                "sbg_scatter": "true"
                            },
                            "glob": "Intervals/*.bed"
                        },
                        "label": "Intervals",
                        "id": "#intervals",
                        "sbg:fileTypes": "BED"
                    }
                ],
                "sbg:job": {
                    "allocatedResources": {
                        "cpu": 1,
                        "mem": 1000
                    },
                    "inputs": {
                        "format": "chr start end",
                        "split_mode": null,
                        "fai_file": {
                            "class": "File",
                            "path": "/path/to/fai_file.ext",
                            "size": 0,
                            "secondaryFiles": []
                        },
                        "bed_file": {
                            "class": "File",
                            "path": "/path/to/bed_file.ext",
                            "size": 0,
                            "secondaryFiles": []
                        }
                    }
                },
                "sbg:contributors": [
                    "bix-demo",
                    "medjo",
                    "bogdang",
                    "vladimirk"
                ],
                "sbg:license": "Apache License 2.0",
                "sbg:image_url": null,
                "sbg:toolkit": "SBGTools",
                "arguments": [
                    {
                        "separate": true,
                        "valueFrom": {
                            "class": "Expression",
                            "script": "{\t\n  if (typeof($job.inputs.format) !== \"undefined\")\n  \treturn \"--format \" + \"\\\"\" + $job.inputs.format + \"\\\"\"\n}",
                            "engine": "#cwl-js-engine"
                        }
                    }
                ],
                "hints": [
                    {
                        "class": "sbg:MemRequirement",
                        "value": 1000
                    },
                    {
                        "class": "DockerRequirement",
                        "dockerImageId": "",
                        "dockerPull": "images.sbgenomics.com/bogdang/sbg_prepare_intervals:1.0"
                    },
                    {
                        "class": "sbg:CPURequirement",
                        "value": 1
                    }
                ],
                "sbg:createdOn": 1473083821,
                "sbg:categories": [
                    "Converters"
                ],
                "sbg:project": "bix-demo/sbgtools-demo",
                "sbg:validationErrors": [],
                "baseCommand": [
                    "python",
                    "sbg_prepare_intervals.py"
                ],
                "label": "SBG Prepare Intervals",
                "cwlVersion": "sbg:draft-2",
                "sbg:revisionNotes": "Fixed Toolkit name.",
                "sbg:modifiedBy": "bix-demo",
                "sbg:toolkitVersion": "1.0",
                "successCodes": [],
                "sbg:revisionsInfo": [
                    {
                        "sbg:revisionNotes": "Copy of medjo/sbg-prepare-intervals/sbg-prepare-intervals/75",
                        "sbg:modifiedBy": "vladimirk",
                        "sbg:revision": 0,
                        "sbg:modifiedOn": 1473083821
                    },
                    {
                        "sbg:revisionNotes": "Copy of medjo/sbg-prepare-intervals/sbg-prepare-intervals/76",
                        "sbg:modifiedBy": "bogdang",
                        "sbg:revision": 1,
                        "sbg:modifiedOn": 1473084447
                    },
                    {
                        "sbg:revisionNotes": null,
                        "sbg:modifiedBy": "medjo",
                        "sbg:revision": 2,
                        "sbg:modifiedOn": 1473928444
                    },
                    {
                        "sbg:revisionNotes": "split_mode set to required",
                        "sbg:modifiedBy": "medjo",
                        "sbg:revision": 3,
                        "sbg:modifiedOn": 1474970272
                    },
                    {
                        "sbg:revisionNotes": "Fixed Toolkit name.",
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revision": 4,
                        "sbg:modifiedOn": 1478525360
                    }
                ],
                "stdin": "",
                "id": "bix-demo/sbgtools-demo/sbg-prepare-intervals/4",
                "sbg:sbgMaintained": false,
                "sbg:id": "admin/sbg-public-data/sbg-prepare-intervals/82",
                "sbg:toolAuthor": "Seven Bridges Genomics",
                "inputs": [
                    {
                        "description": "Depending on selected Split Mode value, output files are generated in accordance with description below:  1. Whole Genome - The tool creates one interval file per line of the input BED(FAI) file. Each interval file contains a single line (one of the lines of BED(FAI) input file).  2. Whole Genome with reduced number of jobs - For each contig(chromosome) a single file is created containing all the intervals corresponding to it . All the intervals (lines) other than (chr1, chr2 ... chrY or 1, 2 ... Y) are saved as (\"others.bed\").  3. Whole Exome - BED file is required for execution of this mode. If mode  3 is applied input is passed to the output.  4. Whole Exome Parallel - BED file is required for execution of this mode. For each contig(chromosome) a single file is created containing all the intervals corresponding to it. All the intervals (lines) other than (chr1, chr2 ... chrY or 1, 2 ... Y) are saved as (\"others.bed\"). If split mode is not selected, the tool will output original BED file.",
                        "type": [
                            {
                                "type": "enum",
                                "name": "split_mode",
                                "symbols": [
                                    "File per interval",
                                    "File per chr with alt contig in a single file",
                                    "Output original BED",
                                    "File per interval with alt contig in a single file"
                                ]
                            }
                        ],
                        "sbg:category": "Input",
                        "inputBinding": {
                            "position": 3,
                            "sbg:cmdInclude": true,
                            "valueFrom": {
                                "class": "Expression",
                                "script": "{\n  mode = $job.inputs.split_mode\n  switch (mode) \n  {\n    case \"File per interval\": \n      return 1\n    case \"File per chr with alt contig in a single file\": \n      return 2\n    case \"Output original BED\": \n      return 3\n    case \"File per interval with alt contig in a single file\": \n      return 4  \n  }\n  return 3\n}",
                                "engine": "#cwl-js-engine"
                            },
                            "prefix": "--mode",
                            "separate": true
                        },
                        "label": "Split mode",
                        "id": "#split_mode"
                    },
                    {
                        "label": "Interval format",
                        "id": "#format",
                        "description": "Format of the intervals in the generated files.",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "name": "format",
                                "symbols": [
                                    "chr start end",
                                    "chr:start-end"
                                ]
                            }
                        ],
                        "sbg:category": "Input"
                    },
                    {
                        "description": "FAI file is converted to BED format if BED file is not provided.",
                        "type": [
                            "null",
                            "File"
                        ],
                        "inputBinding": {
                            "position": 2,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "--fai"
                        },
                        "sbg:category": "File Input",
                        "label": "Input FAI file",
                        "id": "#fai_file",
                        "required": false,
                        "sbg:fileTypes": "FAI"
                    },
                    {
                        "description": "Input BED file containing intervals. Required for modes 3 and 4.",
                        "sbg:stageInput": "link",
                        "type": [
                            "null",
                            "File"
                        ],
                        "inputBinding": {
                            "position": 1,
                            "sbg:cmdInclude": true,
                            "separate": true,
                            "prefix": "--bed"
                        },
                        "sbg:category": "File Input",
                        "label": "Input BED file",
                        "id": "#bed_file",
                        "required": false,
                        "sbg:fileTypes": "BED"
                    }
                ]
            },
            "inputs": [
                {
                    "id": "#SBG_Prepare_Intervals.split_mode",
                    "default": "File per chr with alt contig in a single file"
                },
                {
                    "id": "#SBG_Prepare_Intervals.format",
                    "default": "chr start end"
                },
                {
                    "source": [
                        "#fai_file"
                    ],
                    "id": "#SBG_Prepare_Intervals.fai_file"
                },
                {
                    "id": "#SBG_Prepare_Intervals.bed_file"
                }
            ]
        },
        {
            "sbg:x": 1109,
            "outputs": [
                {
                    "id": "#SBG_SAMtools_Merge_Mpileup.output_file"
                }
            ],
            "id": "#SBG_SAMtools_Merge_Mpileup",
            "sbg:y": 447,
            "run": {
                "sbg:cmdPreview": "",
                "sbg:createdBy": "bix-demo",
                "sbg:latestRevision": 12,
                "sbg:revision": 12,
                "temporaryFailCodes": [],
                "sbg:contributors": [
                    "milan.domazet.sudo",
                    "bix-demo",
                    "marouf"
                ],
                "class": "CommandLineTool",
                "requirements": [
                    {
                        "class": "CreateFileRequirement",
                        "fileDef": [
                            {
                                "filename": "merge.py",
                                "fileContent": "\"\"\"\nUsage: merge.py     --file-list FILE --output-format FRMT  \n\nDescription:\n  Main function of this tool is to provide option to the user weather to merge list of files from SAMtools Mpileup\n  tool or to pass list of files for each contig to the output. If --Merge option is selected, regardless of the type,\n  files will be merged following the contig order from the BAM header.\n\nOptions:\n\n    --help                              This message.\n\n    --version                           Tool version.\n    \n    --output-format FRMT                Output_format.\n\n    --file-list FILE                    List of contigs in BCF or PILEUP format produced by SAMtools Mpileup tool.\n\n\"\"\"\n\nimport docopt\nfrom subprocess import Popen\n\nargs = docopt.docopt(__doc__, version=\"1.0\") \n\nBCFTOOLS_ROOT = \"bcftools\"\n\nsorted_file_list = list()\nfile_list = args['--file-list'].split(',')\n\noutput_format =  args['--output-format']\n\n\nfor fp in file_list:\n    sorted_file_list.append(fp)\n\n\n\nif output_format ==\"pileup\":\n    with open(sorted_file_list[0].split('/')[-1].replace('.pileup', '.merged.pileup'), 'w')  as rr:\n        cmd = ['cat'] + sorted_file_list\n        p = Popen(cmd, stdout=rr)\n        p.wait()\nelif output_format ==\"bcf\":\n    with open(sorted_file_list[0].split('/')[-1].replace('.bcf', '.merged.bcf'), 'w')  as rr:\n        cmd = [BCFTOOLS_ROOT, 'concat'] + sorted_file_list\n        p = Popen(cmd, stdout=rr)\n        p.wait()\nelif output_format ==\"vcf\":\n    with open(sorted_file_list[0].split('/')[-1].replace('.vcf', '.merged.vcf'), 'w')  as rr:\n        cmd = [BCFTOOLS_ROOT, 'concat'] + sorted_file_list\n        p = Popen(cmd, stdout=rr)\n        p.wait()"
                            },
                            {
                                "filename": "delete_empty.py",
                                "fileContent": "\"\"\"\nUsage: delete_empty.py     --file-list FILE  \n\nDescription:\n  Main function of this tool is to remove empty pileup files.\n\nOptions:\n\n    --help                              This message.\n\n    --version                           Tool version.\n\n    --file-list FILE                    List of contigs in BCF or PILEUP format produced by SAMtools Mpileup tool.\n\n\"\"\"\n\nimport os\nimport docopt\nfrom subprocess import Popen\n\nargs = docopt.docopt(__doc__, version=\"1.0\") \n\n\nsorted_file_list = list()\nfile_list = args['--file-list'].split(',')\n\nfor fp in file_list:\n    os.remove(fp)"
                            }
                        ]
                    },
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
                "description": "Main function of this tool is to provide option to the user whether to merge list of files from SAMtools Mpileup tool or to pass list of files for each contig to the output. \n\nIf MERGE option is selected, regardless of the type, files will be merged following the contig order from the reference file index file or BED file. This order is provided using input contig_order_names.  So, this tool will receive list of pileup, vcf or BC files and list of files names of BED files used in creating input files. Pairing and finding the correct order will be done using metadata field  ScatteredUsing.\n\n####Common issue:\nThis tools is built specially for mpileup parallel and Varscan pipelines, so please don't use it outside these pipeline unless you are sure how to use it. Contact our support team for information.",
                "outputs": [
                    {
                        "description": "File obtained by merging files form #file_list.",
                        "type": [
                            "null",
                            {
                                "items": "File",
                                "type": "array"
                            }
                        ],
                        "outputBinding": {
                            "outputEval": {
                                "class": "Expression",
                                "script": "{\n  \n  var inputs = {};\n\n  to_ret = \"\"\n\n  for ( fn in $job.inputs.file_list)\n  {\n    if($job.inputs.file_list[fn].size!==0)\n    {\n\n      name = $job.inputs.file_list[fn].path.split(\"/\").pop()  \n       \n\n\t if(\"metadata\" in $job.inputs.file_list[fn])\n     { \n     \n      inputs[name] = $job.inputs.file_list[fn].metadata\n      \n     }\n\n    }\n  }\n             \n  content = $self\n\n  if($job.inputs.output_state.includes(\"Merge\"))\n  {  \n    \n    // set metadata from any input to this tool\n    name =  content[0][\"name\"]\n    \n    if (typeof name !== 'undefined')\n    {\n      metadata = inputs[name]\n      \n\t if(typeof metadata !== 'undefined')\n     {\n      if (\"ScatteredUsing\" in metadata)\n\t\tdelete metadata[\"ScatteredUsing\"]\n        \n      for ( var fp in content )\n       {\n\n          content[fp][\"metadata\"] = metadata\n\n       }\n     }\n    }\n  \n  }\n   else\n   {\n     for ( var fp in content )\n     {\n\n       name =  content[fp][\"name\"]\n       \n       if (name in inputs)\n       {\n         metadata = inputs[name]\n\n         content[fp][\"metadata\"] = metadata\n       }  \n      \n     }\n\n     return content\n   }\n\n  return content\n}\n\n\n\n\n\n\n\n",
                                "engine": "#cwl-js-engine"
                            },
                            "glob": {
                                "class": "Expression",
                                "script": "{\n  \n  \n   if($job.inputs.output_state.includes(\"Merge\"))\n   {\n    \n    format = $job.inputs.file_list[0].path.split(\"/\").pop()\n    format = format.split('.').pop()\n\n  \treturn \"*.merged.\"+format.toLowerCase()\n   }\n  else\n  {\n    return \"{*.pileup,*.vcf,*.bcf}\"\n  }\n \n}",
                                "engine": "#cwl-js-engine"
                            }
                        },
                        "label": "output_file",
                        "id": "#output_file",
                        "sbg:fileTypes": "PILEUP,BCF,VCF"
                    }
                ],
                "id": "https://api.sbgenomics.com/v2/apps/bix-demo/sbgtools-demo/sbg-samtools-mpileup-merge-out/12/raw/",
                "sbg:job": {
                    "allocatedResources": {
                        "cpu": 1,
                        "mem": 1000
                    },
                    "inputs": {
                        "output_state": "Pass all files",
                        "contig_order_names": [
                            "1",
                            "2",
                            "3"
                        ],
                        "file_list": [
                            {
                                "class": "File",
                                "metadata": {
                                    "ScatteredUsing": "3"
                                },
                                "path": "/ovde/1994060146_RNASeq_R.Aligned.toTranscriptome.out_22.vcf",
                                "size": 0,
                                "secondaryFiles": []
                            },
                            {
                                "class": "File",
                                "metadata": {
                                    "ScatteredUsing": "1",
                                    "SID": "111"
                                },
                                "path": "1994060146_RNASeq_R.Aligned.toTranscriptome.out_11.vcf",
                                "size": 1,
                                "secondaryFiles": []
                            },
                            {
                                "class": "File",
                                "path": "file.txt",
                                "size": 1,
                                "secondaryFiles": []
                            }
                        ]
                    }
                },
                "sbg:createdOn": 1450911287,
                "sbg:license": "Apache License 2.0",
                "stdin": "",
                "sbg:toolkit": "SBGTools",
                "arguments": [
                    {
                        "position": 3,
                        "valueFrom": {
                            "class": "Expression",
                            "script": "{\n\n  if($job.inputs.output_state.includes(\"Merge\"))\n  {\n    to_ret = \"\"\n    for(f in $job.inputs.contig_order_names)\n    {\n\n      for ( fn in $job.inputs.file_list)\n      {\n        if($job.inputs.file_list[fn].metadata)\n        {\n\n\n          if($job.inputs.contig_order_names[f] == $job.inputs.file_list[fn].metadata.ScatteredUsing)\n          {\n            if(to_ret == \"\")\n              to_ret =  $job.inputs.file_list[fn].path\n            else\n    \t\t  to_ret =  to_ret + \",\"+ $job.inputs.file_list[fn].path\n\n          }\n        }\n      }\n\n    }\n    \n    format = $job.inputs.file_list[0].path.split(\"/\").pop()\n    format = format.split('.').pop()\n    \n    \n    if(to_ret==\"\")\n    \treturn \n    else\n      \treturn \"python merge.py --output-format \" + format.toLowerCase()+ \" --file-list \"+ to_ret\n  \n  }\n  else if($job.inputs.output_state == \"Pass nonempty files\")\n  {\n    to_ret = \"\"\n          \n    for ( fn in $job.inputs.file_list)\n      {\n        if($job.inputs.file_list[fn].size==0)\n        {\n\n          if(to_ret == \"\")\n            to_ret =  $job.inputs.file_list[fn].path\n          else\n            to_ret =  to_ret + \",\"+ $job.inputs.file_list[fn].path\n\n        }\n      }\n    \n    if(to_ret==\"\")\n    \treturn \n    else\n  \t\treturn \"python delete_empty.py --file-list \"+ to_ret\n          \n  }\n\n\n\n          \n}",
                            "engine": "#cwl-js-engine"
                        },
                        "prefix": "",
                        "separate": true
                    }
                ],
                "hints": [
                    {
                        "class": "sbg:CPURequirement",
                        "value": 1
                    },
                    {
                        "class": "sbg:MemRequirement",
                        "value": 1000
                    },
                    {
                        "class": "DockerRequirement",
                        "dockerImageId": "",
                        "dockerPull": "images.sbgenomics.com/marouf/sbg-pileup-merge-out:0.1"
                    }
                ],
                "sbg:categories": [
                    "Text-Processing",
                    "SAM/BAM-Processing"
                ],
                "sbg:project": "bix-demo/sbgtools-demo",
                "sbg:validationErrors": [],
                "baseCommand": [
                    ""
                ],
                "label": "SBG SAMtools Merge Mpileup",
                "cwlVersion": "sbg:draft-2",
                "sbg:revisionNotes": "fix metadata issue when there is not metadata at input files",
                "sbg:modifiedBy": "marouf",
                "sbg:toolkitVersion": "0.2",
                "successCodes": [],
                "sbg:modifiedOn": 1482421824,
                "sbg:revisionsInfo": [
                    {
                        "sbg:revisionNotes": null,
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revision": 0,
                        "sbg:modifiedOn": 1450911287
                    },
                    {
                        "sbg:revisionNotes": null,
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:revision": 1,
                        "sbg:modifiedOn": 1450911287
                    },
                    {
                        "sbg:revisionNotes": "import from dev project",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 2,
                        "sbg:modifiedOn": 1473929938
                    },
                    {
                        "sbg:revisionNotes": "add some verification to avoid deleting null in case there are not empty files to delete.",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 3,
                        "sbg:modifiedOn": 1474019719
                    },
                    {
                        "sbg:revisionNotes": "Update the merge.py script and fix bug with the output file name",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 4,
                        "sbg:modifiedOn": 1474973977
                    },
                    {
                        "sbg:revisionNotes": "change the python code in merge.py",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 5,
                        "sbg:modifiedOn": 1475052403
                    },
                    {
                        "sbg:revisionNotes": "change the merge.py",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 6,
                        "sbg:modifiedOn": 1475058095
                    },
                    {
                        "sbg:revisionNotes": "add . to the output file name",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 7,
                        "sbg:modifiedOn": 1475083435
                    },
                    {
                        "sbg:revisionNotes": "update parameters ID, and label",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 8,
                        "sbg:modifiedOn": 1475237770
                    },
                    {
                        "sbg:revisionNotes": "link instead of copy inputs",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 9,
                        "sbg:modifiedOn": 1476440477
                    },
                    {
                        "sbg:revisionNotes": "Added inherit metadata from input.",
                        "sbg:modifiedBy": "milan.domazet.sudo",
                        "sbg:revision": 10,
                        "sbg:modifiedOn": 1482334679
                    },
                    {
                        "sbg:revisionNotes": "fix metadata issue",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 11,
                        "sbg:modifiedOn": 1482417569
                    },
                    {
                        "sbg:revisionNotes": "fix metadata issue when there is not metadata at input files",
                        "sbg:modifiedBy": "marouf",
                        "sbg:revision": 12,
                        "sbg:modifiedOn": 1482421824
                    }
                ],
                "sbg:image_url": null,
                "stdout": "",
                "sbg:sbgMaintained": false,
                "sbg:id": "admin/sbg-public-data/sbg-samtools-mpileup-merge-out/12",
                "sbg:toolAuthor": "Mohamed Marouf, Seven Bridges Genomics, <mohamed.marouf@sbgenomics.com>",
                "inputs": [
                    {
                        "description": "Input files for merging.",
                        "sbg:stageInput": "link",
                        "type": [
                            {
                                "items": "File",
                                "type": "array",
                                "name": "file_list"
                            }
                        ],
                        "label": "Input file list",
                        "id": "#file_list",
                        "sbg:fileTypes": "PILEUP,VCF,BCF"
                    },
                    {
                        "label": "merging_order",
                        "id": "#contig_order_names",
                        "description": "Ordered names of contigs that are used for merging of file list loaded via file_list input.",
                        "type": [
                            {
                                "items": "string",
                                "type": "array",
                                "name": "contig_order_names"
                            }
                        ],
                        "sbg:includeInPorts": true
                    },
                    {
                        "label": "Output state",
                        "id": "#output_state",
                        "description": "Mode to tell how to merge mpileup files or to pass them through.",
                        "type": [
                            {
                                "type": "enum",
                                "name": "output_state",
                                "symbols": [
                                    "Merge",
                                    "Pass nonempty files",
                                    "Pass all files"
                                ]
                            }
                        ]
                    }
                ]
            },
            "inputs": [
                {
                    "source": [
                        "#SAMtools_Mpileup.output_pileup_vcf_or_bcf_file"
                    ],
                    "id": "#SBG_SAMtools_Merge_Mpileup.file_list"
                },
                {
                    "source": [
                        "#SBG_Prepare_Intervals.names"
                    ],
                    "id": "#SBG_SAMtools_Merge_Mpileup.contig_order_names"
                },
                {
                    "source": [
                        "#output_state"
                    ],
                    "id": "#SBG_SAMtools_Merge_Mpileup.output_state"
                }
            ]
        }
    ],
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
            "id": "http://sourceforge.net/projects/samtools/files/samtools/0.1.18/"
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
    "sbg:project": "bixqa/qa-load-2017-07-31-18",
    "sbg:validationErrors": [],
    "label": "SAMtools Mpileup parallel",
    "cwlVersion": "sbg:draft-2",
    "sbg:revisionNotes": "fix metadata issue when there is no metadata at inputs",
    "sbg:modifiedBy": "bixqa",
    "sbg:toolkitVersion": "1.3",
    "hints": [],
    "sbg:modifiedOn": 1501518694,
    "sbg:revisionsInfo": [
        {
            "sbg:revisionNotes": null,
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 0,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": null,
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 1,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": null,
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 2,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": null,
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 3,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": null,
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 4,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": null,
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 5,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "Removed MergedOutput",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 6,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "update the merge mpileup tool",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 7,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "Change the description",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 8,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "update merge_pileup tool",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 9,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "Update the merge pileup and change the description",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 10,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "change parameters settings",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 11,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "provide better description and remove bed file",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 12,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "more description",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 13,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "change merge pileup",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 14,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "change description",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 15,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "Exposed mapq_threshold.",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 16,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "Exposed mapq_threshold.",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 17,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "Add filter to mpileu tool",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 18,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "expose filter",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 19,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "Updated all tools (mostly inherit metadata from input)",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 20,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "fix metadata",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 21,
            "sbg:modifiedOn": 1501518694
        },
        {
            "sbg:revisionNotes": "fix metadata issue when there is no metadata at inputs",
            "sbg:modifiedBy": "bixqa",
            "sbg:revision": 22,
            "sbg:modifiedOn": 1501518694
        }
    ],
    "sbg:license": "The MIT License",
    "sbg:sbgMaintained": false,
    "sbg:id": "bixqa/qa-load-2017-07-31-18/samtools-mpileup-parallel/22",
    "sbg:toolAuthor": "Heng Li, Sanger Institute",
    "inputs": [
        {
            "sbg:x": 627,
            "type": [
                {
                    "items": "File",
                    "type": "array"
                }
            ],
            "sbg:fileTypes": "BAM",
            "label": "bams",
            "id": "#bams",
            "sbg:y": 348,
            "sbg:includeInPorts": true
        },
        {
            "sbg:x": 624,
            "type": [
                "File"
            ],
            "sbg:fileTypes": "FASTA,FA,GZ",
            "label": "reference_fasta",
            "id": "#reference_fasta",
            "sbg:y": 221,
            "sbg:suggestedValue": {
                "class": "File",
                "path": "5772b6d8507c1752674486e6",
                "name": "human_g1k_v37_decoy.fasta"
            },
            "sbg:includeInPorts": true
        },
        {
            "sbg:x": 629,
            "type": [
                "null",
                "File"
            ],
            "sbg:fileTypes": "FAI",
            "label": "fai_file",
            "id": "#fai_file",
            "sbg:y": 469,
            "sbg:suggestedValue": {
                "class": "File",
                "path": "578cf949507c17681a3117e2",
                "name": "human_g1k_v37_decoy.fasta.fai"
            },
            "sbg:includeInPorts": true
        },
        {
            "description": "Minimum base quality for a base to be considered.",
            "type": [
                "null",
                "int"
            ],
            "sbg:toolDefaultValue": "13",
            "sbg:category": "Input options",
            "label": "Skip bases with baseQ/BAQ smaller than",
            "id": "#skip_bases_with_baq_smaller_than_defined",
            "required": false,
            "sbg:includeInPorts": false
        },
        {
            "description": "Required flags: skip reads with mask bits unset.",
            "type": [
                "null",
                "int"
            ],
            "sbg:category": "Input options",
            "label": "Required flags",
            "id": "#required_flags",
            "required": false,
            "sbg:includeInPorts": false
        },
        {
            "description": "Region in which pileup is generated.",
            "type": [
                "null",
                "string"
            ],
            "sbg:category": "Input options",
            "label": "Region in which pileup is generated",
            "id": "#region_pileup_generation",
            "required": false,
            "sbg:includeInPorts": false
        },
        {
            "description": "Recalculate BAQ on the fly, ignore existing BQ tags.",
            "type": [
                "null",
                "boolean"
            ],
            "sbg:category": "Input options",
            "label": "Recalculate BAQ on the fly",
            "id": "#recalc_BAQ_fly",
            "required": false,
            "sbg:includeInPorts": false
        },
        {
            "description": "Apply -m and -F thresholds per sample to increase sensitivity of calling. By default both options are applied to reads pooled from all samples.",
            "sbg:stageInput": null,
            "type": [
                "null",
                "boolean"
            ],
            "sbg:category": "configuration",
            "label": "Apply -m and -F thresholds per sample",
            "id": "#per_sample_mF"
        },
        {
            "description": "Coefficient for downgrading mapping quality for reads containing excessive mismatches. Given a read with a phred-scaled probability q of being generated from the mapped position, the new mapping quality is about sqrt((INT-q)/INT)*INT. A zero value disables this functionality; if enabled, the recommended value for BWA is 50.",
            "type": [
                "null",
                "int"
            ],
            "sbg:toolDefaultValue": "0",
            "sbg:category": "Input options",
            "label": "Parameter for adjusting mapQ",
            "id": "#parameter_for_adjusting_mapq",
            "required": false,
            "sbg:includeInPorts": false
        },
        {
            "label": "Generate uncompress BCF/VCF output",
            "id": "#output_uncompressed_bcf_or_vcf",
            "description": "Similar to -g except that the output is uncompressed BCF, which is preferred for piping.",
            "type": [
                "null",
                "boolean"
            ],
            "sbg:category": "Output options"
        },
        {
            "label": "Output mapping quality",
            "id": "#output_mapping_quality",
            "description": "Output mapping quality (disabled by -g/-u).",
            "type": [
                "null",
                "boolean"
            ],
            "sbg:category": "Output options"
        },
        {
            "label": "Output file format",
            "id": "#output_format",
            "description": "Output file format.",
            "type": [
                {
                    "type": "enum",
                    "name": "output_format",
                    "symbols": [
                        "PILEUP",
                        "BCF",
                        "VCF"
                    ]
                }
            ],
            "sbg:category": "configuration"
        },
        {
            "label": "Output base positions on reads",
            "id": "#output_base_positions_on_reads",
            "description": "Output base positions on reads (disabled by -g/-u).",
            "type": [
                "null",
                "boolean"
            ],
            "sbg:category": "Output options"
        },
        {
            "label": "Do not perform indel calling",
            "id": "#no_indel_calling",
            "description": "Do not perform indel calling.",
            "type": [
                "null",
                "boolean"
            ],
            "sbg:category": "SNP/INDEL genotype likelihoods options"
        },
        {
            "label": "Comma-separated list of FORMAT and INFO tags to output",
            "id": "#more_info_to_output",
            "description": "Comma-separated list of FORMAT and INFO tags (DP,AD,ADF,ADR,SP,INFO/AD,INFO/ADF,INFO/ADR []\") to output.",
            "type": [
                "null",
                "string"
            ],
            "sbg:category": "configuration"
        },
        {
            "description": "Minimum fraction of gapped reads for candidates.",
            "type": [
                "null",
                "float"
            ],
            "sbg:category": "SNP/INDEL genotype likelihoods options",
            "label": "Minimum fraction of gapped reads for candidates",
            "id": "#minimum_fraction_of_gapped_reads",
            "sbg:toolDefaultValue": "0.002"
        },
        {
            "description": "Minimum gapped reads for indel candidates.",
            "type": [
                "null",
                "int"
            ],
            "sbg:category": "SNP/INDEL genotype likelihoods options",
            "label": "Minimum gapped reads for indel candidates",
            "id": "#min_gapped_reads_for_indel",
            "sbg:toolDefaultValue": "1"
        },
        {
            "description": "At a position, read maximally INT reads per input BAM.",
            "type": [
                "null",
                "int"
            ],
            "sbg:category": "Input options",
            "label": "Max per-BAM depth",
            "id": "#max_per_bam_depth",
            "sbg:toolDefaultValue": "250"
        },
        {
            "description": "Skip INDEL calling if the average per-sample depth is above.",
            "type": [
                "null",
                "int"
            ],
            "schema": [
                "null",
                "int"
            ],
            "sbg:category": "configuration",
            "label": "Skip INDEL calling if the average per-sample depth is above",
            "id": "#max_idepth",
            "sbg:toolDefaultValue": "250"
        },
        {
            "label": "Skip alignments with mapQ  smaller than",
            "id": "#mapq_threshold",
            "description": "Minimum mapping quality for an alignment to be used.",
            "type": [
                "null",
                "int"
            ],
            "sbg:category": "Input options"
        },
        {
            "label": "Ignore RG tags",
            "id": "#ignore_rg_tags",
            "description": "Ignore rg tags.",
            "type": [
                "null",
                "boolean"
            ],
            "sbg:category": "Input options"
        },
        {
            "label": "Disable read-pair overlap detection",
            "id": "#ignore_overlaps",
            "description": "Disable read-pair overlap detection.",
            "type": [
                "null",
                "boolean"
            ],
            "sbg:category": "configuration"
        },
        {
            "description": "Coefficient for modeling homopolymer errors. Given an l-long homopolymer run, the sequencing error of an indel of size s is modeled as INT*s/l.",
            "type": [
                "null",
                "int"
            ],
            "sbg:category": "SNP/INDEL genotype likelihoods options",
            "label": "Coefficient for homopolymer errors",
            "id": "#homopolymer_err_coeficient",
            "sbg:toolDefaultValue": "100"
        },
        {
            "description": "Phred-scaled gap open sequencing error probability. Reducing INT leads to more indel calls.",
            "type": [
                "null",
                "int"
            ],
            "sbg:category": "SNP/INDEL genotype likelihoods options",
            "label": "Phred-scaled gap open sequencing error probability",
            "id": "#gap_open_sequencing_error_probability",
            "sbg:toolDefaultValue": "40"
        },
        {
            "description": "Phred-scaled gap extension sequencing error probability. Reducing INT leads to longer indels.",
            "type": [
                "null",
                "int"
            ],
            "sbg:category": "SNP/INDEL genotype likelihoods options",
            "label": "Phred-scaled gap extension seq error probability",
            "id": "#gap_extension_seq_error_probability",
            "sbg:toolDefaultValue": "20"
        },
        {
            "description": "Filter zero coverage lines from pileup files. This option is valid only when output pileup files.",
            "sbg:stageInput": null,
            "type": [
                "null",
                "boolean"
            ],
            "sbg:category": "output configuration",
            "label": "Filter zero coverage lines.",
            "id": "#filter_zero_coverage_lines"
        },
        {
            "description": "Filter flags: skip reads with mask bits set [UNMAP,SECONDARY,QCFAIL,DUP].",
            "sbg:stageInput": null,
            "type": [
                "null",
                "string"
            ],
            "sbg:category": "Input options",
            "label": "Filter flags",
            "id": "#filter_flags"
        },
        {
            "label": "Disable BAQ computation",
            "id": "#disable_baq_computation",
            "description": "Disable probabilistic realignment for the computation of base alignment quality (BAQ). BAQ is the Phred-scaled probability of a read base being misaligned. Applying this option greatly helps to reduce false SNPs caused by misalignments.",
            "type": [
                "null",
                "boolean"
            ],
            "sbg:category": "Input options"
        },
        {
            "label": "Count anomalous read pairs",
            "id": "#count_anomalous_read_pairs",
            "description": "Do not skip anomalous read pairs in variant calling.",
            "type": [
                "null",
                "boolean"
            ],
            "sbg:category": "Input options"
        },
        {
            "description": "Comma dilimited list of platforms (determined by @RG-PL) from which indel candidates are obtained. It is recommended to collect indel candidates from sequencing technologies that have low indel error rate such as ILLUMINA.",
            "type": [
                "null",
                "string"
            ],
            "sbg:category": "SNP/INDEL genotype likelihoods options",
            "label": "Comma separated list of platforms for indels",
            "id": "#comma_separated_list_of_platforms_for_indels",
            "sbg:toolDefaultValue": "\"all\""
        },
        {
            "label": "Assume the quality is in the Illumina-1.3+ encoding",
            "id": "#assume_the_quality_Illumina_encoding",
            "description": "Assume the quality is in the Illumina-1.3+ encoding.",
            "type": [
                "null",
                "boolean"
            ],
            "sbg:category": "Input options"
        },
        {
            "label": "Output state",
            "id": "#output_state",
            "description": "Mode to tell how to merge mpileup files or to pass them through.",
            "type": [
                {
                    "type": "enum",
                    "name": "output_state",
                    "symbols": [
                        "Merge",
                        "Pass nonempty files",
                        "Pass all files"
                    ]
                }
            ]
        }
    ]
}