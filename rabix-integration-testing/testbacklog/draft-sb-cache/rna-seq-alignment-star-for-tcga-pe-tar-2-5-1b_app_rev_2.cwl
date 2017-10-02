{
    "sbg:contributors": [
        "bixqa"
    ],
    "id": "https://api.sbgenomics.com/v2/apps/bixqa/qa-load-2017-07-31-18/rna-seq-alignment-star-for-tcga-pe-tar-2-5-1b/2/raw/",
    "sbg:copyOf": "admin/sbg-public-data/rna-seq-alignment-star-for-tcga-pe-tar-2-5-1b/2",
    "requirements": [],
    "outputs": [
        {
            "sbg:fileTypes": "FASTQ",
            "id": "#unmapped_reads",
            "required": false,
            "sbg:y": 159.58330912391222,
            "sbg:includeInPorts": true,
            "sbg:x": 766.2497863074045,
            "label": "unmapped_reads",
            "type": [
                "null",
                {
                    "type": "array",
                    "items": "File"
                }
            ],
            "source": [
                "#STAR_1.unmapped_reads"
            ]
        },
        {
            "sbg:fileTypes": "BAM",
            "id": "#transcriptome_aligned_reads",
            "required": false,
            "sbg:y": 86.58332158128358,
            "sbg:includeInPorts": true,
            "sbg:x": 1118.9998003244302,
            "label": "transcriptome_aligned_reads",
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#STAR_1.transcriptome_aligned_reads"
            ]
        },
        {
            "sbg:fileTypes": "TAB",
            "id": "#splice_junctions",
            "required": false,
            "sbg:y": 167.49997603893155,
            "sbg:includeInPorts": true,
            "sbg:x": 1282.3330177465928,
            "label": "splice_junctions",
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#STAR_1.splice_junctions"
            ]
        },
        {
            "sbg:fileTypes": "TAB",
            "id": "#reads_per_gene",
            "required": false,
            "sbg:y": 245.74996398885858,
            "sbg:includeInPorts": true,
            "sbg:x": 1394.416355699286,
            "label": "reads_per_gene",
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#STAR_1.reads_per_gene"
            ]
        },
        {
            "sbg:fileTypes": "OUT",
            "id": "#log_files",
            "required": false,
            "sbg:y": 322.9999517997081,
            "sbg:includeInPorts": true,
            "sbg:x": 1505.0830268959055,
            "label": "log_files",
            "type": [
                "null",
                {
                    "type": "array",
                    "items": "File"
                }
            ],
            "source": [
                "#STAR_1.log_files"
            ]
        },
        {
            "sbg:fileTypes": "JUNCTION",
            "id": "#chimeric_junctions",
            "required": false,
            "sbg:y": 446.7499567170913,
            "sbg:includeInPorts": true,
            "sbg:x": 1278.7498061756194,
            "label": "chimeric_junctions",
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#STAR_1.chimeric_junctions"
            ]
        },
        {
            "sbg:fileTypes": "TAR",
            "id": "#intermediate_genome",
            "required": false,
            "sbg:y": 386.0832876066342,
            "sbg:includeInPorts": true,
            "sbg:x": 1408.9164783457816,
            "label": "intermediate_genome",
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#STAR_1.intermediate_genome"
            ]
        },
        {
            "sbg:fileTypes": "SAM",
            "id": "#chimeric_alignments",
            "required": false,
            "sbg:y": 503.2499285439613,
            "sbg:includeInPorts": true,
            "sbg:x": 1147.5831347604494,
            "label": "chimeric_alignments",
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#STAR_1.chimeric_alignments"
            ]
        },
        {
            "sbg:fileTypes": "BAM, SAM",
            "id": "#sorted_bam",
            "required": false,
            "sbg:y": 557.2498435974195,
            "sbg:includeInPorts": true,
            "sbg:x": 934.2498227655963,
            "label": "sorted_bam",
            "type": [
                "null",
                "File"
            ],
            "source": [
                "#Picard_SortSam.sorted_bam"
            ]
        }
    ],
    "sbg:toolkitVersion": "2.5.1b",
    "sbg:links": [
        {
            "label": "Homepage",
            "id": "https://github.com/alexdobin/STAR"
        },
        {
            "label": "Releases",
            "id": "https://github.com/alexdobin/STAR/releases"
        },
        {
            "label": "Manual",
            "id": "https://github.com/alexdobin/STAR/blob/master/doc/STARmanual.pdf"
        },
        {
            "label": "Support",
            "id": "https://groups.google.com/forum/#!forum/rna-star"
        },
        {
            "label": "Publication",
            "id": "http://www.ncbi.nlm.nih.gov/pubmed/23104886"
        }
    ],
    "sbg:revisionNotes": "Update STAR Genome Generate.",
    "sbg:image_url": "https://brood.sbgenomics.com/static/bixqa/qa-load-2017-07-31-18/rna-seq-alignment-star-for-tcga-pe-tar-2-5-1b/2.png",
    "sbg:createdOn": 1501518670,
    "sbg:validationErrors": [],
    "sbg:appVersion": [
        "sbg:draft-2"
    ],
    "sbg:sbgMaintained": false,
    "sbg:id": "bixqa/qa-load-2017-07-31-18/rna-seq-alignment-star-for-tcga-pe-tar-2-5-1b/2",
    "steps": [
        {
            "id": "#SBG_Unpack_FASTQs",
            "outputs": [
                {
                    "id": "#SBG_Unpack_FASTQs.output_fastq_files"
                }
            ],
            "sbg:y": 323.3333868715514,
            "sbg:x": 203.33336763911876,
            "run": {
                "sbg:contributors": [
                    "bix-demo",
                    "markop"
                ],
                "id": "bix-demo/sbgtools-demo/sbg-unpack-fastqs-1-0/4",
                "stdin": "",
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
                "outputs": [
                    {
                        "sbg:fileTypes": "FASTQ",
                        "id": "#output_fastq_files",
                        "label": "Output FASTQ files",
                        "outputBinding": {
                            "glob": "decompressed_files/*.fastq",
                            "sbg:inheritMetadataFrom": "#input_archive_file",
                            "sbg:metadata": {
                                "paired_end": {
                                    "script": "{\n  filepath = $self.path\n  filename = filepath.split(\"/\").pop();\n  if (filename.lastIndexOf(\".fastq\") !== 0)\n  \tp = filename[filename.lastIndexOf(\".fastq\") - 1 ]\n  if ((p == 1) || (p == 2))\n    return p\n  else\n    return \"\"\n}",
                                    "engine": "#cwl-js-engine",
                                    "class": "Expression"
                                }
                            }
                        },
                        "description": "Output FASTQ files.",
                        "type": [
                            {
                                "type": "array",
                                "items": "File",
                                "name": "output_fastq_files"
                            }
                        ]
                    }
                ],
                "sbg:toolkitVersion": "v1.0",
                "hints": [
                    {
                        "class": "DockerRequirement",
                        "dockerPull": "images.sbgenomics.com/markop/sbg-unpack-fastqs:1.0",
                        "dockerImageId": "df9e1c169beb"
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
                "sbg:revisionNotes": "Changed paired-end metadata for single end reads.",
                "stdout": "out.txt",
                "sbg:createdOn": 1450911291,
                "sbg:image_url": null,
                "sbg:cmdPreview": "/opt/sbg_unpack_fastqs.py --input_archive_file input_file.tar > out.txt",
                "sbg:id": "admin/sbg-public-data/sbg-unpack-fastqs-1-0/4",
                "sbg:latestRevision": 4,
                "y": 323.3333868715514,
                "cwlVersion": "sbg:draft-2",
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "Other"
                ],
                "sbg:homepage": "https://igor.sbgenomics.com/",
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 1000,
                        "cpu": 1
                    },
                    "inputs": {
                        "input_archive_file": {
                            "secondaryFiles": [],
                            "size": 0,
                            "path": "input_file.tar",
                            "class": "File"
                        }
                    }
                },
                "label": "SBG Unpack FASTQs",
                "sbg:modifiedBy": "markop",
                "sbg:toolAuthor": "Marko Petkovic, Seven Bridges Genomics",
                "baseCommand": [
                    "/opt/sbg_unpack_fastqs.py"
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:modifiedOn": 1450911291,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 0
                    },
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:modifiedOn": 1450911292,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 1
                    },
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:modifiedOn": 1450911292,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 2
                    },
                    {
                        "sbg:modifiedBy": "markop",
                        "sbg:modifiedOn": 1468593347,
                        "sbg:revisionNotes": "Changed description.",
                        "sbg:revision": 3
                    },
                    {
                        "sbg:modifiedBy": "markop",
                        "sbg:modifiedOn": 1468593614,
                        "sbg:revisionNotes": "Changed paired-end metadata for single end reads.",
                        "sbg:revision": 4
                    }
                ],
                "sbg:createdBy": "bix-demo",
                "x": 203.33336763911876,
                "sbg:modifiedOn": 1468593614,
                "sbg:sbgMaintained": false,
                "class": "CommandLineTool",
                "successCodes": [],
                "sbg:project": "bix-demo/sbgtools-demo",
                "sbg:license": "Apache License 2.0",
                "arguments": [],
                "description": "**SBG Unpack FASTQs** performs the extraction of the input archive, containing FASTQ files. \nThis tool also sets the \"paired_end\" metadata field. It assumes that FASTQ file names are formatted in this manner:\nfirst pair reads FASTQ file        -  *1.fastq\nsecond pair reads FASTQ file  -  * 2.fastq. \nwhere * represents any string.\n**This tool is designed to be used for paired-end metadata with above mentioned name formatting only.**\nSupported formats are:\n1. TAR\n2. TAR.GZ (TGZ)\n3. TAR.BZ2 (TBZ2)\n4. GZ\n5. BZ2\n6. ZIP",
                "sbg:revision": 4,
                "sbg:toolkit": "SBGTools",
                "inputs": [
                    {
                        "sbg:fileTypes": "TAR, TAR.GZ, TGZ, TAR.BZ2, TBZ2,  GZ, BZ2, ZIP",
                        "id": "#input_archive_file",
                        "required": true,
                        "inputBinding": {
                            "prefix": "--input_archive_file",
                            "position": 0,
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Input archive file",
                        "type": [
                            "File"
                        ],
                        "sbg:category": "",
                        "description": "The input archive file, containing FASTQ files, to be unpacked."
                    }
                ]
            },
            "inputs": [
                {
                    "id": "#SBG_Unpack_FASTQs.input_archive_file",
                    "source": [
                        "#input_archive_file"
                    ]
                }
            ]
        },
        {
            "scatter": "#SBG_FASTQ_Quality_Detector.fastq",
            "id": "#SBG_FASTQ_Quality_Detector",
            "outputs": [
                {
                    "id": "#SBG_FASTQ_Quality_Detector.result"
                }
            ],
            "sbg:y": 322.6742276278409,
            "sbg:x": 375.33330743963063,
            "run": {
                "sbg:contributors": [
                    "vladimirk",
                    "milan.domazet.sudo",
                    "bix-demo"
                ],
                "id": "bix-demo/sbgtools-demo/sbg-fastq-quality-detector/8",
                "stdin": "",
                "requirements": [
                    {
                        "fileDef": [
                            {
                                "fileContent": "\"\"\"\nUsage:\n    sbg_fastq_sniff.py --fastq FILE\n\nOptions:\n    -h, --help          Show this message.\n\n    -f, --fastq FILE    Input FASTQ file.\n\n\"\"\"\n\nfrom docopt import docopt\nimport os\nimport gzip\nimport itertools as it\nimport shutil\n\nfrom sdkcwl import *\n\nargs = docopt(__doc__, version='1.0')\n\n\nclass myGzipFile(gzip.GzipFile):\n    def __enter__(self, *args, **kwargs):\n        if self.fileobj is None:\n            raise Exception(\"I/O operation on closed GzipFile object\")\n        return self\n\n    def __exit__(self, *args, **kwargs):\n        self.close()\n\n\ndef extremes(a, b):\n    if a is False:\n        return b, b\n    return min(a[0], b), max(a[1], b)\n\n\ndef walk_qualities(f, sample_size=1000):\n    for i in xrange(sample_size * 4):\n        try:\n            line = f.next()\n        except StopIteration:\n            return\n        if i % 4 == 3:\n            yield line.rstrip(\"\\n\\r\")\n\n\ndef sniff(path):\n    with open(path, 'rb') as f:\n        gz = f.read(2) == '\\x1f\\x8b'\n    opn = myGzipFile if gz else open\n    with opn(path) as f:\n        return get_scale(*map(ord, reduce(extremes, it.chain(*walk_qualities(f)), False)))\n\n\ndef get_scale(ord_min, ord_max):\n    options = {\n        'illumina13': (64, 105),\n        'illumina15': (66, 105),\n        'sanger': (33, 126),\n        'solexa': (59, 105),\n    }\n    fits = [(k, v) for k, v in options.iteritems() if v[0] <= ord_min and v[1] >= ord_max]\n    if not fits:\n        message = 'Quality scale for range (%s, %s) not found.' % (ord_min, ord_max)\n        raise Exception(message)\n        # Return narrowest range\n    return reduce(lambda a, b: a if a[1][1] - a[1][0] < b[1][1] - b[1][0] else b, fits)[0]\n\ncwl_input(args, '--fastq', 'fastq')\ncwl_output('result')\n\nfq = self.inputs.fastq\n\nquality_scale = sniff(fq)\n\noutput_file = fq[fq.rfind('/')+1:]\nshutil.copyfile(fq, output_file)\n\nself.outputs.result = output_file\nself.outputs.result.meta = fq.make_metadata(quality_scale=quality_scale)\n\ncwl_finish()",
                                "filename": "sbg_fastq_quality_scale_detector.py"
                            },
                            {
                                "fileContent": "import json\nimport os\n\ndef _get_meta(fpath, reload_job=False):\n    def get_files(o):\n        if isinstance(o, dict) and o.get('class') == 'File':\n            return [o]\n        if isinstance(o, dict):\n            return sum(map(get_files, o.itervalues()), [])\n        if isinstance(o, list):\n            return sum(map(get_files, o), [])\n        return []\n\n    files = getattr(_get_meta, '_files', None)\n    if reload_job or files is None:\n        with open('job.json') as fp:\n            job = json.load(fp)\n        files = {f['path']: f for f in get_files(job['inputs'])}\n    return files[fpath].get('metadata', {})\n\n\nclass _DotDict(dict):\n    def _map(self, attr):\n        key_map = {\n            \"file_type\": \"file_extension\",\n            \"seq_tech\": \"platform\",\n            \"sample\": \"sample_id\",\n            \"library\": \"library_id\",\n            \"platform_unit\": \"platform_unit_id\",\n            \"chunk\": \"file_segment_number\",\n            \"qual_scale\": \"quality_scale\"\n        }\n        if attr in key_map:\n            attr = key_map[attr]\n        return attr\n\n    def __init__(self, *args, **kwargs):\n        super(_DotDict, self).__init__(*args, **kwargs)\n        for arg in args:\n            if isinstance(arg, dict) or isinstance(arg, _DotDict):\n                for k, v in arg.iteritems():\n                    nk = self._map(k)\n                    if k != nk and k in self:\n                        self.pop(k)\n                    self[nk] = v\n        if kwargs:\n            for k, v in kwargs.iteritems():\n                self[self._map(k)] = v\n\n    def __getattr__(self, attr):\n        return self.get(self._map(attr))\n\n    def __setattr__(self, key, value):\n        self.__setitem__(self._map(key), value)\n\n    def __setitem__(self, key, value):\n        super(_DotDict, self).__setitem__(self._map(key), value)\n        self.__dict__.update({self._map(key): value})\n\n    def __delattr__(self, item):\n        self.__delitem__(self._map(item))\n\n    def __delitem__(self, key):\n        super(_DotDict, self).__delitem__(self._map(key))\n        del self.__dict__[self._map(key)]\n\nclass _OldInput(str):\n\n    _meta = None\n\n    @property\n    def meta(self):\n        if self._meta is None:\n            self._meta = _DotDict(_get_meta(self))\n        return self._meta\n    \n    def make_metadata(self, **kwargs):\n        new_meta = _DotDict(self.meta)\n        if kwargs is not None:\n            for key, value in kwargs.iteritems():\n                new_meta[key] = value\n        return new_meta\n\n\nclass _OldOutputBucket(_DotDict):\n    \n    def __setitem__(self, key, value):\n        if isinstance(value, list):\n            super(_OldOutputBucket, self).__setitem__(key, _OldOutputList(value))\n        else:\n            super(_DotDict, self).__setitem__(key, _OldOutput(value))\n\nclass _OldOutput(str):\n\n    _meta = None\n\n    @property\n    def meta(self):\n        if self._meta is None:\n            self._meta = _DotDict()\n        return self._meta\n\n    @meta.setter\n    def meta(self, value):\n        self._meta = _DotDict(value)\n\n\nclass _OldOutputList(list):\n\n    def add_file(self, name):\n        new_file = _OldOutput(name)\n        self.append(new_file)\n        return new_file\ndef cwl_input(_args, _new, _old, list=False):\n    _new = _args[_new]\n    if isinstance(_new, str):\n        _new = [_new]\n    if list==False and len(_new) > 1:\n        raise Exception('Number of items provided to a non-list type input.')\n    if len(_new) == 1 and list == False:\n        self.inputs[_old] =  _OldInput(_new[0])\n    else:\n        self.inputs[_old] = map(_OldInput, _new)\n\ndef cwl_param(_args, _new, _old):\n    self.params[_old] = _args[_new]\n\ndef cwl_output(_old, list=False):\n    if list is False:\n        self.outputs[_old] = _OldOutput()\n    else:\n        self.outputs[_old] = _OldOutputList()\n\n\ndef cwl_finish():\n    if not self['outputs']:\n        return\n    if 'cwl_secondary' not in self:\n        self['cwl_secondary'] = {}\n    data = {}\n    for output in self['outputs']:\n        o = self['outputs'][output]\n        sf = self['cwl_secondary'].get(output, None)\n        if isinstance(o, _OldOutputList):\n            file_data = []\n            for f in o:\n                f_dict = {'name': os.path.split(f)[1], \\\n                          'class': 'File', \\\n                          'metadata': f.meta, \\\n                          'path': os.path.join(os.getcwd(), f)}\n                if sf:\n                    f_dict['secondaryFiles'] = [{'path': os.path.join(os.getcwd(), x), \"class\": \"File\"} for x in sf]\n                file_data.append(f_dict)\n        else:\n            file_data = {\n                'name': os.path.split(o)[1],\n                'class': 'File',\n                'metadata': o.meta,\n                'path': os.path.join(os.getcwd(), o)\n            }\n            if sf:\n                file_data['secondaryFiles'] = [{'path': os.path.join(os.getcwd(), x), \"class\": \"File\"} for x in sf]\n        data[output] = file_data\n    with open('cwl.output.json', 'w') as w:\n        json.dump(data, w)\n\ndef cwl_set_secondary(output, secondary_files):\n    if 'cwl_secondary' not in self:\n        self['cwl_secondary'] = {}\n    if not isinstance(secondary_files, list):\n        secondary_files = [secondary_files]\n    self['cwl_secondary'][output] = secondary_files\n\n################################################################################\n\nglobal self\nself = _DotDict(globals())\nif 'inputs' not in self:\n    self['inputs'] = _DotDict()\nif 'outputs' not in self:\n    self['outputs'] =  _OldOutputBucket()\nif 'params' not in self:\n    self['params'] = _DotDict()",
                                "filename": "sdkcwl.py"
                            }
                        ],
                        "class": "CreateFileRequirement"
                    }
                ],
                "temporaryFailCodes": [],
                "hints": [
                    {
                        "class": "DockerRequirement",
                        "dockerPull": "images.sbgenomics.com/tziotas/sbg_fastq_quality_scale_detector:1.0",
                        "dockerImageId": ""
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
                "sbg:revisionNotes": "Reverted Sanger scale to (33,126), until we find a better range.",
                "stdout": "",
                "sbg:createdOn": 1450911312,
                "sbg:image_url": null,
                "sbg:cmdPreview": "python sbg_fastq_quality_scale_detector.py --fastq /path/to/fastq.ext",
                "sbg:id": "admin/sbg-public-data/sbg-fastq-quality-detector/12",
                "sbg:latestRevision": 8,
                "y": 322.6742276278409,
                "cwlVersion": "sbg:draft-2",
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "FASTQ-Processing"
                ],
                "sbg:license": "Apache License 2.0",
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 1000,
                        "cpu": 1
                    },
                    "inputs": {
                        "fastq": {
                            "secondaryFiles": [],
                            "size": 0,
                            "path": "/path/to/fastq.ext",
                            "class": "File"
                        }
                    }
                },
                "label": "SBG FASTQ Quality Detector",
                "sbg:modifiedBy": "milan.domazet.sudo",
                "sbg:toolAuthor": "Seven Bridges Genomics",
                "baseCommand": [
                    "python",
                    "sbg_fastq_quality_scale_detector.py"
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:modifiedOn": 1450911312,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 0
                    },
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:modifiedOn": 1450911313,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 1
                    },
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:modifiedOn": 1450911313,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 2
                    },
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:modifiedOn": 1450911314,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 3
                    },
                    {
                        "sbg:modifiedBy": "milan.domazet.sudo",
                        "sbg:modifiedOn": 1472045214,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 4
                    },
                    {
                        "sbg:modifiedBy": "milan.domazet.sudo",
                        "sbg:modifiedOn": 1473774177,
                        "sbg:revisionNotes": "Changed input type to FASTQ and FQ.",
                        "sbg:revision": 5
                    },
                    {
                        "sbg:modifiedBy": "vladimirk",
                        "sbg:modifiedOn": 1473775728,
                        "sbg:revisionNotes": "Added Category.",
                        "sbg:revision": 6
                    },
                    {
                        "sbg:modifiedBy": "milan.domazet.sudo",
                        "sbg:modifiedOn": 1475083591,
                        "sbg:revisionNotes": "Fixed Sanger range and moved script to crate files.",
                        "sbg:revision": 7
                    },
                    {
                        "sbg:modifiedBy": "milan.domazet.sudo",
                        "sbg:modifiedOn": 1475246089,
                        "sbg:revisionNotes": "Reverted Sanger scale to (33,126), until we find a better range.",
                        "sbg:revision": 8
                    }
                ],
                "sbg:createdBy": "bix-demo",
                "x": 375.33330743963063,
                "outputs": [
                    {
                        "sbg:fileTypes": "FASTQ",
                        "id": "#result",
                        "label": "Result",
                        "outputBinding": {
                            "glob": "*.fastq",
                            "sbg:inheritMetadataFrom": "#fastq"
                        },
                        "description": "Source FASTQ file with updated metadata.",
                        "type": [
                            "null",
                            "File"
                        ]
                    }
                ],
                "sbg:sbgMaintained": false,
                "class": "CommandLineTool",
                "successCodes": [],
                "sbg:project": "bix-demo/sbgtools-demo",
                "sbg:modifiedOn": 1475246089,
                "arguments": [],
                "description": "FASTQ Quality Scale Detector detects which quality encoding scheme was used in your reads and automatically enters the proper value in the \"Quality Scale\" metadata field.",
                "sbg:revision": 8,
                "sbg:toolkit": "SBGTools",
                "inputs": [
                    {
                        "sbg:fileTypes": "FASTQ,FQ",
                        "id": "#fastq",
                        "required": true,
                        "inputBinding": {
                            "prefix": "--fastq",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Fastq",
                        "type": [
                            "File"
                        ],
                        "sbg:category": "Input",
                        "description": "FASTQ file."
                    }
                ]
            },
            "inputs": [
                {
                    "id": "#SBG_FASTQ_Quality_Detector.fastq",
                    "source": [
                        "#SBG_Unpack_FASTQs.output_fastq_files"
                    ]
                }
            ]
        },
        {
            "id": "#Picard_SortSam",
            "outputs": [
                {
                    "id": "#Picard_SortSam.sorted_bam"
                }
            ],
            "sbg:y": 470.91659385958945,
            "sbg:x": 773.083180715633,
            "run": {
                "sbg:contributors": [
                    "bix-demo",
                    "mladenlSBG"
                ],
                "id": "bix-demo/picard-1-140-demo/picard-sortsam-1-140/3",
                "stdin": "",
                "requirements": [
                    {
                        "id": "#cwl-js-engine",
                        "class": "ExpressionEngineRequirement",
                        "requirements": [
                            {
                                "dockerPull": "rabix/js-engine",
                                "class": "DockerRequirement"
                            }
                        ],
                        "engineCommand": "cwl-engine.js"
                    }
                ],
                "temporaryFailCodes": [],
                "outputs": [
                    {
                        "sbg:fileTypes": "BAM, SAM",
                        "id": "#sorted_bam",
                        "label": "Sorted BAM/SAM",
                        "outputBinding": {
                            "glob": "*.sorted.?am",
                            "secondaryFiles": [
                                "^.bai",
                                ".bai"
                            ],
                            "sbg:metadata": {
                                "__inherit__": "input_bam"
                            },
                            "sbg:inheritMetadataFrom": "#input_bam"
                        },
                        "description": "Sorted BAM or SAM file.",
                        "type": [
                            "null",
                            "File"
                        ]
                    }
                ],
                "sbg:toolkitVersion": "1.140",
                "sbg:links": [
                    {
                        "label": "Homepage",
                        "id": "http://broadinstitute.github.io/picard/index.html"
                    },
                    {
                        "label": "Source Code",
                        "id": "https://github.com/broadinstitute/picard/releases/tag/1.138"
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
                "hints": [
                    {
                        "class": "DockerRequirement",
                        "dockerPull": "images.sbgenomics.com/mladenlsbg/picard:1.140",
                        "dockerImageId": "eab0e70b6629"
                    },
                    {
                        "value": 1,
                        "class": "sbg:CPURequirement"
                    },
                    {
                        "value": {
                            "script": "{\n  if($job.inputs.memory_per_job){\n  \treturn $job.inputs.memory_per_job\n  }\n  \treturn 2048\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "class": "sbg:MemRequirement"
                    }
                ],
                "sbg:revisionNotes": "Modified \"sort_order\" default value.",
                "stdout": "",
                "sbg:createdOn": 1450911168,
                "sbg:image_url": null,
                "sbg:cmdPreview": "java -Xmx2048M -jar /opt/picard-tools-1.140/picard.jar SortSam OUTPUT=example.tested.sorted.bam INPUT=/root/dir/example.tested.bam SORT_ORDER=coordinate",
                "sbg:sbgMaintained": false,
                "sbg:id": "admin/sbg-public-data/picard-sortsam-1-140/3",
                "y": 470.91659385958945,
                "cwlVersion": "sbg:draft-2",
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "SAM/BAM-Processing"
                ],
                "sbg:license": "MIT License, Apache 2.0 Licence",
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 2048,
                        "cpu": 1
                    },
                    "inputs": {
                        "sort_order": "Coordinate",
                        "input_bam": {
                            "path": "/root/dir/example.tested.bam"
                        },
                        "create_index": null,
                        "output_type": null,
                        "memory_per_job": 2048
                    }
                },
                "label": "Picard SortSam",
                "sbg:modifiedBy": "mladenlSBG",
                "sbg:toolAuthor": "Broad Institute",
                "baseCommand": [
                    "java",
                    {
                        "script": "{   \n  if($job.inputs.memory_per_job){\n    return '-Xmx'.concat($job.inputs.memory_per_job, 'M')\n  }   \n  \treturn '-Xmx2048M'\n}",
                        "engine": "#cwl-js-engine",
                        "class": "Expression"
                    },
                    "-jar",
                    "/opt/picard-tools-1.140/picard.jar",
                    "SortSam"
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:modifiedOn": 1450911168,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 0
                    },
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:modifiedOn": 1450911169,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 1
                    },
                    {
                        "sbg:modifiedBy": "bix-demo",
                        "sbg:modifiedOn": 1450911170,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 2
                    },
                    {
                        "sbg:modifiedBy": "mladenlSBG",
                        "sbg:modifiedOn": 1476869720,
                        "sbg:revisionNotes": "Modified \"sort_order\" default value.",
                        "sbg:revision": 3
                    }
                ],
                "sbg:createdBy": "bix-demo",
                "x": 773.083180715633,
                "sbg:modifiedOn": 1476869720,
                "sbg:latestRevision": 3,
                "class": "CommandLineTool",
                "successCodes": [],
                "sbg:project": "bix-demo/picard-1-140-demo",
                "arguments": [
                    {
                        "prefix": "OUTPUT=",
                        "position": 0,
                        "valueFrom": {
                            "script": "{\n  filename = $job.inputs.input_bam.path\n  ext = $job.inputs.output_type\n\nif (ext === \"BAM\")\n{\n    return filename.split('.').slice(0, -1).concat(\"sorted.bam\").join(\".\").replace(/^.*[\\\\\\/]/, '')\n    }\n\nelse if (ext === \"SAM\")\n{\n    return filename.split('.').slice(0, -1).concat(\"sorted.sam\").join('.').replace(/^.*[\\\\\\/]/, '')\n}\n\nelse \n{\n\treturn filename.split('.').slice(0, -1).concat(\"sorted.\"+filename.split('.').slice(-1)[0]).join(\".\").replace(/^.*[\\\\\\/]/, '')\n}\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "separate": false
                    },
                    {
                        "position": 1000,
                        "valueFrom": {
                            "script": "{\n  filename = $job.inputs.input_bam.path\n  \n  /* figuring out output file type */\n  ext = $job.inputs.output_type\n  if (ext === \"BAM\")\n  {\n    out_extension = \"BAM\"\n  }\n  else if (ext === \"SAM\")\n  {\n    out_extension = \"SAM\"\n  }\n  else \n  {\n\tout_extension = filename.split('.').slice(-1)[0].toUpperCase()\n  }  \n  \n  /* if exist moving .bai in bam.bai */\n  if ($job.inputs.create_index === 'True' && $job.inputs.sort_order === 'Coordinate' && out_extension == \"BAM\")\n  {\n    \n    old_name = filename.split('.').slice(0, -1).concat('sorted.bai').join('.').replace(/^.*[\\\\\\/]/, '')\n    new_name = filename.split('.').slice(0, -1).concat('sorted.bam.bai').join('.').replace(/^.*[\\\\\\/]/, '')\n    return \"; mv \" + \" \" + old_name + \" \" + new_name\n  }\n\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "separate": true
                    }
                ],
                "description": "Picard SortSam sorts the input SAM or BAM. Input and output formats are determined by the file extension.",
                "sbg:revision": 3,
                "sbg:toolkit": "Picard",
                "inputs": [
                    {
                        "id": "#validation_stringency",
                        "sbg:toolDefaultValue": "SILENT",
                        "inputBinding": {
                            "prefix": "VALIDATION_STRINGENCY=",
                            "valueFrom": {
                                "script": "{\n  if ($job.inputs.validation_stringency)\n  {\n    return $job.inputs.validation_stringency\n  }\n  else\n  {\n    return \"SILENT\"\n  }\n}",
                                "engine": "#cwl-js-engine",
                                "class": "Expression"
                            },
                            "separate": false,
                            "sbg:cmdInclude": true
                        },
                        "label": "Validation stringency",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "STRICT",
                                    "LENIENT",
                                    "SILENT"
                                ],
                                "name": "validation_stringency"
                            }
                        ],
                        "sbg:category": "Other input types",
                        "description": "Validation stringency for all SAM files read by this program. Setting stringency to SILENT can improve performance when processing a BAM file in which variable-length data (read, qualities, tags) do not otherwise need to be decoded. This option can be set to 'null' to clear the default value. Possible values: {STRICT, LENIENT, SILENT}."
                    },
                    {
                        "id": "#sort_order",
                        "sbg:category": "Other input types",
                        "inputBinding": {
                            "prefix": "SORT_ORDER=",
                            "position": 3,
                            "valueFrom": {
                                "script": "{\n  p = $job.inputs.sort_order.toLowerCase()\n  return p\n}",
                                "engine": "#cwl-js-engine",
                                "class": "Expression"
                            },
                            "separate": false,
                            "sbg:cmdInclude": true
                        },
                        "label": "Sort order",
                        "type": [
                            {
                                "type": "enum",
                                "symbols": [
                                    "Unsorted",
                                    "Queryname",
                                    "Coordinate"
                                ],
                                "name": "sort_order"
                            }
                        ],
                        "sbg:altPrefix": "SO",
                        "description": "Sort order of the output file. Possible values: {unsorted, queryname, coordinate}."
                    },
                    {
                        "id": "#quiet",
                        "sbg:toolDefaultValue": "False",
                        "inputBinding": {
                            "prefix": "QUIET=",
                            "separate": false,
                            "sbg:cmdInclude": true
                        },
                        "label": "Quiet",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "True",
                                    "False"
                                ],
                                "name": "quiet"
                            }
                        ],
                        "sbg:category": "Other input types",
                        "description": "This parameter indicates whether to suppress job-summary info on System.err. This option can be set to 'null' to clear the default value. Possible values: {true, false}."
                    },
                    {
                        "id": "#output_type",
                        "sbg:toolDefaultValue": "SAME AS INPUT",
                        "label": "Output format",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "BAM",
                                    "SAM",
                                    "SAME AS INPUT"
                                ],
                                "name": "output_type"
                            }
                        ],
                        "sbg:category": "Other input types",
                        "description": "Since Picard tools can output both SAM and BAM files, user can choose the format of the output file."
                    },
                    {
                        "label": "Memory per job",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:toolDefaultValue": "2048",
                        "description": "Amount of RAM memory to be used per job. Defaults to 2048 MB for single threaded jobs.",
                        "id": "#memory_per_job"
                    },
                    {
                        "id": "#max_records_in_ram",
                        "sbg:toolDefaultValue": "500000",
                        "inputBinding": {
                            "prefix": "MAX_RECORDS_IN_RAM=",
                            "separate": false,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max records in RAM",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Other input types",
                        "description": "When writing SAM files that need to be sorted, this parameter will specify the number of records stored in RAM before spilling to disk. Increasing this number reduces the number of file handles needed to sort a SAM file, and increases the amount of RAM needed. This option can be set to 'null' to clear the default value."
                    },
                    {
                        "sbg:fileTypes": "BAM, SAM",
                        "label": "Input BAM",
                        "required": true,
                        "inputBinding": {
                            "prefix": "INPUT=",
                            "position": 1,
                            "separate": false,
                            "sbg:cmdInclude": true
                        },
                        "id": "#input_bam",
                        "sbg:altPrefix": "I",
                        "type": [
                            "File"
                        ],
                        "sbg:category": "File inputs",
                        "description": "The BAM or SAM file to sort."
                    },
                    {
                        "id": "#create_index",
                        "sbg:toolDefaultValue": "False",
                        "inputBinding": {
                            "prefix": "CREATE_INDEX=",
                            "position": 5,
                            "separate": false,
                            "sbg:cmdInclude": true
                        },
                        "label": "Create index",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "True",
                                    "False"
                                ],
                                "name": "create_index"
                            }
                        ],
                        "sbg:category": "Other input types",
                        "description": "This parameter indicates whether to create a BAM index when writing a coordinate-sorted BAM file. This option can be set to 'null' to clear the default value. Possible values: {true, false}."
                    },
                    {
                        "id": "#compression_level",
                        "sbg:toolDefaultValue": "5",
                        "inputBinding": {
                            "prefix": "COMPRESSION_LEVEL=",
                            "separate": false,
                            "sbg:cmdInclude": true
                        },
                        "label": "Compression level",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Other input types",
                        "description": "Compression level for all compressed files created (e.g. BAM and GELI). This option can be set to 'null' to clear the default value."
                    }
                ]
            },
            "inputs": [
                {
                    "default": "SILENT",
                    "id": "#Picard_SortSam.validation_stringency"
                },
                {
                    "default": "Coordinate",
                    "id": "#Picard_SortSam.sort_order"
                },
                {
                    "id": "#Picard_SortSam.quiet"
                },
                {
                    "id": "#Picard_SortSam.output_type"
                },
                {
                    "id": "#Picard_SortSam.memory_per_job"
                },
                {
                    "id": "#Picard_SortSam.max_records_in_ram"
                },
                {
                    "id": "#Picard_SortSam.input_bam",
                    "source": [
                        "#STAR_1.aligned_reads"
                    ]
                },
                {
                    "default": "True",
                    "id": "#Picard_SortSam.create_index"
                },
                {
                    "id": "#Picard_SortSam.compression_level"
                }
            ]
        },
        {
            "id": "#STAR_1",
            "outputs": [
                {
                    "id": "#STAR_1.unmapped_reads"
                },
                {
                    "id": "#STAR_1.transcriptome_aligned_reads"
                },
                {
                    "id": "#STAR_1.splice_junctions"
                },
                {
                    "id": "#STAR_1.reads_per_gene"
                },
                {
                    "id": "#STAR_1.log_files"
                },
                {
                    "id": "#STAR_1.intermediate_genome"
                },
                {
                    "id": "#STAR_1.chimeric_junctions"
                },
                {
                    "id": "#STAR_1.chimeric_alignments"
                },
                {
                    "id": "#STAR_1.aligned_reads"
                }
            ],
            "sbg:y": 322.3530543040269,
            "sbg:x": 583.5295573831879,
            "run": {
                "sbg:contributors": [
                    "ana_d",
                    "uros_sipetic",
                    "dusan_randjelovic"
                ],
                "id": "uros_sipetic/star-2-5-1b-demo/star-2-5-1-b/41",
                "stdin": "",
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
                "outputs": [
                    {
                        "sbg:fileTypes": "FASTQ",
                        "id": "#unmapped_reads",
                        "label": "Unmapped reads",
                        "outputBinding": {
                            "glob": {
                                "script": "{\n  if ($job.inputs.unmappedOutputName) {\n    return \"*\" + $job.inputs.unmappedOutputName + \"*\"\n  } else {\n    return \"*Unmapped.out*\"\n  }\n  \n}",
                                "engine": "#cwl-js-engine",
                                "class": "Expression"
                            },
                            "sbg:inheritMetadataFrom": "#reads",
                            "sbg:metadata": {
                                "paired_end": {
                                    "script": "{\n  if ($self) {\n    filename = $self.path.split(\"/\").pop();\n    if (filename.lastIndexOf(\".fastq\") !== 0){\n      return filename[filename.lastIndexOf(\".fastq\") - 1 ]\n    } else {\n      return \"\"\n    } \n  } else {\n    return \"\"\n  }\n}",
                                    "engine": "#cwl-js-engine",
                                    "class": "Expression"
                                },
                                "sample_id": {
                                    "script": "{\n \nif([].concat($job.inputs.reads)[0].metadata.sample_id)\n   {\n    return [].concat($job.inputs.reads)[0].metadata.sample_id\n   }\n   else\n   {\n    filename = $self.path.split(\"/\").pop();\n    return filename.split(\".\").shift()\n   }\n}",
                                    "engine": "#cwl-js-engine",
                                    "class": "Expression"
                                }
                            }
                        },
                        "description": "Output of unmapped reads.",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ]
                    },
                    {
                        "sbg:fileTypes": "BAM",
                        "id": "#transcriptome_aligned_reads",
                        "label": "Transcriptome alignments",
                        "outputBinding": {
                            "glob": "*Transcriptome*",
                            "sbg:inheritMetadataFrom": "#reads",
                            "sbg:metadata": {
                                "reference_genome": {
                                    "script": "{\n  if ($job.inputs.genome.metadata) {\n    if ($job.inputs.genome.metadata.reference_genome) {\n      return $job.inputs.genome.metadata.reference_genome\n    } else {\n      return \"\"\n    }\n  } else {\n  return \"\"\n  }\n}",
                                    "engine": "#cwl-js-engine",
                                    "class": "Expression"
                                }
                            }
                        },
                        "description": "Alignments translated into transcript coordinates.",
                        "type": [
                            "null",
                            "File"
                        ]
                    },
                    {
                        "sbg:fileTypes": "TAB",
                        "id": "#splice_junctions",
                        "label": "Splice junctions",
                        "outputBinding": {
                            "glob": "*SJ.out.tab",
                            "sbg:inheritMetadataFrom": "#reads",
                            "sbg:metadata": {}
                        },
                        "description": "High confidence collapsed splice junctions in tab-delimited format. Only junctions supported by uniquely mapping reads are reported.",
                        "type": [
                            "null",
                            "File"
                        ]
                    },
                    {
                        "sbg:fileTypes": "TAB",
                        "id": "#reads_per_gene",
                        "label": "Reads per gene",
                        "outputBinding": {
                            "glob": "*ReadsPerGene*",
                            "sbg:inheritMetadataFrom": "#reads",
                            "sbg:metadata": {}
                        },
                        "description": "File with number of reads per gene. A read is counted if it overlaps (1nt or more) one and only one gene.",
                        "type": [
                            "null",
                            "File"
                        ]
                    },
                    {
                        "sbg:fileTypes": "OUT",
                        "id": "#log_files",
                        "label": "Log files",
                        "outputBinding": {
                            "glob": "*Log*.out",
                            "sbg:inheritMetadataFrom": "#reads",
                            "sbg:metadata": {}
                        },
                        "description": "Log files produced during alignment.",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File"
                            }
                        ]
                    },
                    {
                        "sbg:fileTypes": "TAR",
                        "id": "#intermediate_genome",
                        "label": "Intermediate genome files",
                        "outputBinding": {
                            "glob": "*_STARgenome.tar",
                            "sbg:inheritMetadataFrom": "#reads",
                            "sbg:metadata": {}
                        },
                        "description": "Archive with genome files produced when annotations are included on the fly (in the mapping step).",
                        "type": [
                            "null",
                            "File"
                        ]
                    },
                    {
                        "sbg:fileTypes": "JUNCTION",
                        "id": "#chimeric_junctions",
                        "label": "Chimeric junctions",
                        "outputBinding": {
                            "glob": "*Chimeric.out.junction",
                            "sbg:inheritMetadataFrom": "#reads",
                            "sbg:metadata": {}
                        },
                        "description": "If chimSegmentMin in 'Chimeric Alignments' section is set to 0, 'Chimeric Junctions' won't be output.",
                        "type": [
                            "null",
                            "File"
                        ]
                    },
                    {
                        "sbg:fileTypes": "SAM",
                        "id": "#chimeric_alignments",
                        "label": "Chimeric alignments",
                        "outputBinding": {
                            "glob": "*.Chimeric.out.sam",
                            "sbg:inheritMetadataFrom": "#reads",
                            "sbg:metadata": {
                                "reference_genome": {
                                    "script": "{\n  if ($job.inputs.genome.metadata) {\n    if ($job.inputs.genome.metadata.reference_genome) {\n      return $job.inputs.genome.metadata.reference_genome\n    } else {\n      return \"\"\n    }\n  } else {\n  return \"\"\n  }\n}",
                                    "engine": "#cwl-js-engine",
                                    "class": "Expression"
                                }
                            }
                        },
                        "description": "Aligned Chimeric sequences SAM - if chimSegmentMin = 0, no Chimeric Alignment SAM and Chimeric Junctions outputs.",
                        "type": [
                            "null",
                            "File"
                        ]
                    },
                    {
                        "sbg:fileTypes": "SAM, BAM",
                        "id": "#aligned_reads",
                        "label": "Aligned SAM/BAM",
                        "outputBinding": {
                            "glob": {
                                "script": "{\n  if ($job.inputs.outSortingType == 'SortedByCoordinate') {\n    sort_name = '.sortedByCoord'\n  }\n  else {\n    sort_name = ''\n  }\n  if ($job.inputs.outSAMtype == 'BAM') {\n    sam_name = \"*.Aligned\".concat( sort_name, '.out.bam')\n  }\n  else {\n    sam_name = \"*.Aligned.out.sam\"\n  }\n  return sam_name\n}",
                                "engine": "#cwl-js-engine",
                                "class": "Expression"
                            },
                            "sbg:inheritMetadataFrom": "#reads",
                            "sbg:metadata": {
                                "sample_id": {
                                    "script": "{\n \nif([].concat($job.inputs.reads)[0].metadata.sample_id)\n   {\n    return [].concat($job.inputs.reads)[0].metadata.sample_id\n   }\n   else\n   {\n    filename = $self.path.split(\"/\").pop();\n    return filename.split(\".\").shift()\n   }\n}",
                                    "engine": "#cwl-js-engine",
                                    "class": "Expression"
                                },
                                "reference_genome": {
                                    "script": "{\n  if ($job.inputs.genome.metadata) {\n    if ($job.inputs.genome.metadata.reference_genome) {\n      return $job.inputs.genome.metadata.reference_genome\n    } else {\n      return \"\"\n    }\n  } else {\n  return \"\"\n  }\n}",
                                    "engine": "#cwl-js-engine",
                                    "class": "Expression"
                                }
                            }
                        },
                        "description": "Aligned sequence in SAM/BAM format.",
                        "type": [
                            "null",
                            "File"
                        ]
                    }
                ],
                "sbg:toolkitVersion": "2.5.1b",
                "sbg:links": [
                    {
                        "label": "Homepage",
                        "id": "https://github.com/alexdobin/STAR"
                    },
                    {
                        "label": "Releases",
                        "id": "https://github.com/alexdobin/STAR/releases"
                    },
                    {
                        "label": "Manual",
                        "id": "https://github.com/alexdobin/STAR/blob/master/doc/STARmanual.pdf"
                    },
                    {
                        "label": "Support",
                        "id": "https://groups.google.com/forum/#!forum/rna-star"
                    },
                    {
                        "label": "Publication",
                        "id": "http://www.ncbi.nlm.nih.gov/pubmed/23104886"
                    }
                ],
                "hints": [
                    {
                        "class": "DockerRequirement",
                        "dockerPull": "images.sbgenomics.com/ana_d/star-fusion:2.5.1b",
                        "dockerImageId": ""
                    },
                    {
                        "value": 60000,
                        "class": "sbg:MemRequirement"
                    },
                    {
                        "value": 32,
                        "class": "sbg:CPURequirement"
                    }
                ],
                "sbg:revisionNotes": "Update read group expression to handle cases with files without any metadata.",
                "stdout": "",
                "sbg:createdOn": 1462811121,
                "sbg:image_url": null,
                "sbg:cmdPreview": "tar -xvf genome.ext && /opt/STAR-2.5.1b/bin/Linux_x86_64_static/STAR --runThreadN 32    --sjdbGTFfile /demo/test-data/chr20.gtf  --sjdbGTFchrPrefix chrPrefix --sjdbInsertSave Basic  --twopass1readsN -2  --chimOutType WithinSAM  --outSAMattrRGline ID:1 PI:rg_mfl PL:Ion_Torrent_PGM PU:rg_platform_unit SM:rg_sample  --quantMode TranscriptomeSAM --outFileNamePrefix ./test_sample_1.fastq.  --readFilesIn /test-data/test_sample_1.fastq  && tar -vcf test_sample_1.fastq._STARgenome.tar ./test_sample_1.fastq._STARgenome   && cat test_sample_1.fastq.Unmapped.out.mate1 | sed 's/\\t.*//' | paste - - - - | sort -k1,1 -S 10G | tr '\\t' '\\n' > test_sample_1.fastq.Uncontamimated.mate1.fastq && rm test_sample_1.fastq.Unmapped.out.mate1",
                "sbg:sbgMaintained": false,
                "sbg:id": "admin/sbg-public-data/star-2-5-1-b/41",
                "y": 322.3530543040269,
                "cwlVersion": "sbg:draft-2",
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "Alignment"
                ],
                "sbg:license": "GNU General Public License v3.0 only",
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 60000,
                        "cpu": 32
                    },
                    "inputs": {
                        "alignIntronMax": 0,
                        "outSAMprimaryFlag": "OneBestScore",
                        "readMatesLengthsIn": "NotEqual",
                        "chimJunctionOverhangMin": 0,
                        "unmappedOutputName": "Uncontamimated",
                        "outFilterMultimapScoreRange": 0,
                        "sjdbGTFfile": [
                            {
                                "path": "/demo/test-data/chr20.gtf"
                            }
                        ],
                        "seedPerReadNmax": 0,
                        "sjdbOverhang": null,
                        "quantTranscriptomeBan": "IndelSoftclipSingleend",
                        "alignTranscriptsPerWindowNmax": 0,
                        "outSJfilterCountTotalMin": [
                            3,
                            1,
                            1,
                            1
                        ],
                        "chimScoreJunctionNonGTAG": 0,
                        "chimScoreSeparation": 0,
                        "sjdbGTFfeatureExon": "",
                        "limitOutSJcollapsed": 0,
                        "seedNoneLociPerWindow": 0,
                        "outSJfilterReads": "All",
                        "chimSegmentReadGapMax": 8,
                        "alignSoftClipAtReferenceEnds": "Yes",
                        "outSJfilterIntronMaxVsReadN": [
                            0
                        ],
                        "scoreInsOpen": 0,
                        "outSAMtype": "SAM",
                        "scoreGapNoncan": 0,
                        "alignSplicedMateMapLmin": 0,
                        "outSJfilterDistToOtherSJmin": [
                            0
                        ],
                        "clip3pAdapterMMp": [
                            0
                        ],
                        "outSJfilterOverhangMin": [
                            30,
                            12,
                            12,
                            12
                        ],
                        "outFilterScoreMin": 0,
                        "sjdbGTFchrPrefix": "chrPrefix",
                        "winFlankNbins": 0,
                        "outQSconversionAdd": 0,
                        "chimOutType": "Within",
                        "outReadsUnmapped": "Fastx",
                        "winAnchorDistNbins": 0,
                        "scoreGapGCAG": 0,
                        "outSAMflagAND": 0,
                        "outFilterMismatchNmax": 0,
                        "reads": [
                            {
                                "secondaryFiles": [],
                                "class": "File",
                                "size": 0,
                                "path": "/test-data/test_sample_1.fastq",
                                "metadata": {
                                    "paired_end": "1"
                                }
                            }
                        ],
                        "alignSJstitchMismatchNmax": "alignSJstitchMismatchNmax-string-value",
                        "rg_platform_unit_id": "rg_platform_unit",
                        "alignEndsType": "Local",
                        "no_read_groups": false,
                        "alignIntronMin": 0,
                        "chimScoreMin": 0,
                        "outSAMmode": "Full",
                        "outSAMmapqUnique": 0,
                        "sjdbScore": null,
                        "sjdbGTFtagExonParentTranscript": "",
                        "clip3pNbases": [
                            0,
                            3
                        ],
                        "rg_library_id": "",
                        "sjdbInsertSave": "Basic",
                        "outFilterIntronMotifs": "None",
                        "seedSearchLmax": 0,
                        "genome": {
                            "secondaryFiles": [],
                            "size": 0,
                            "path": "genome.ext",
                            "class": "File"
                        },
                        "scoreGapATAC": 0,
                        "scoreStitchSJshift": 0,
                        "sortUnmappedReads": true,
                        "GENOME_DIR_NAME": "",
                        "outFilterMatchNminOverLread": 0,
                        "alignSJoverhangMin": 0,
                        "outSAMattrIHstart": 6,
                        "outSAMheaderHD": "outSAMheaderHD",
                        "outMultimapperOrder": "Random",
                        "clip3pAfterAdapterNbases": [
                            0
                        ],
                        "chimScoreDropMax": 0,
                        "outSAMfilter": "KeepOnlyAddedReference",
                        "rg_seq_center": "",
                        "limitBAMsortRAM": 0,
                        "limitOutSJoneRead": 0,
                        "scoreGenomicLengthLog2scale": 0,
                        "alignSJDBoverhangMin": 0,
                        "scoreDelBase": 0,
                        "outSAMflagOR": 0,
                        "outSAMheaderPG": "outSAMheaderPG",
                        "outSAMmultNmax": 0,
                        "outSortingType": "SortedByCoordinate",
                        "outSAMattributes": "Standard",
                        "outFilterType": "Normal",
                        "winAnchorMultimapNmax": 0,
                        "seedSearchStartLmax": 0,
                        "clip3pAdapterSeq": [
                            "clip3pAdapterSeq"
                        ],
                        "readMapNumber": 0,
                        "chimFilter": "banGenomicN",
                        "outSAMstrandField": "None",
                        "winBinNbits": 0,
                        "seedPerWindowNmax": 0,
                        "clip5pNbases": [
                            0
                        ],
                        "limitSjdbInsertNsj": 0,
                        "chimSegmentMin": 15,
                        "outSAMunmapped": "None",
                        "alignMatesGapMax": 0,
                        "scoreDelOpen": 0,
                        "outFilterMultimapNmax": 0,
                        "outSJfilterCountUniqueMin": [
                            3,
                            1,
                            1,
                            1
                        ],
                        "quantMode": "TranscriptomeSAM",
                        "rg_mfl": "rg_mfl",
                        "scoreGap": 0,
                        "rg_platform": "Ion Torrent PGM",
                        "scoreInsBase": 0,
                        "sjdbGTFtagExonParentGene": "",
                        "outSAMorder": "Paired",
                        "alignSplicedMateMapLminOverLmate": 0,
                        "alignWindowsPerReadNmax": 0,
                        "seedSearchStartLmaxOverLread": 0,
                        "alignTranscriptsPerReadNmax": 0,
                        "twopassMode": "Basic",
                        "rg_sample_id": "rg_sample",
                        "outSAMreadID": "Standard",
                        "outFilterMatchNmin": 0,
                        "outFilterScoreMinOverLread": 0,
                        "outFilterMismatchNoverLmax": 0,
                        "seedMultimapNmax": 0,
                        "twopass1readsN": -2,
                        "outFilterMismatchNoverReadLmax": 0
                    }
                },
                "label": "STAR",
                "sbg:modifiedBy": "uros_sipetic",
                "sbg:toolAuthor": "Alexander Dobin/CSHL",
                "baseCommand": [
                    "tar",
                    "-xvf",
                    {
                        "script": "$job.inputs.genome.path",
                        "engine": "#cwl-js-engine",
                        "class": "Expression"
                    },
                    "&&",
                    "/opt/STAR-2.5.1b/bin/Linux_x86_64_static/STAR",
                    "--runThreadN",
                    {
                        "script": "{\n  return $job.allocatedResources.cpu\n}",
                        "engine": "#cwl-js-engine",
                        "class": "Expression"
                    }
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1462811121,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 0
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1462878623,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 1
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1462878822,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 2
                    },
                    {
                        "sbg:modifiedBy": "ana_d",
                        "sbg:modifiedOn": 1462888744,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 3
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1467305514,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 4
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1469452910,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 5
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1469454687,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 6
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1469454843,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 7
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1469456917,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 8
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1469457344,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 9
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1469457393,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 10
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1469457447,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 11
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1469457642,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 12
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1469458688,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 13
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1469458758,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 14
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1470660796,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 15
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1471862645,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 16
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1471865665,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 17
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1471865803,
                        "sbg:revisionNotes": "Added proper 'paired_end' metadata to 'unmapped_reads' output.",
                        "sbg:revision": 18
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1471871327,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 19
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1472206922,
                        "sbg:revisionNotes": "Add proper 'Reference Genome' metadata to BAM/SAM outputs.",
                        "sbg:revision": 20
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1472737519,
                        "sbg:revisionNotes": "Addressed peer-review tickets.",
                        "sbg:revision": 21
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1473070666,
                        "sbg:revisionNotes": "Changed number of cores from 15 to 32.",
                        "sbg:revision": 22
                    },
                    {
                        "sbg:modifiedBy": "ana_d",
                        "sbg:modifiedOn": 1475170418,
                        "sbg:revisionNotes": "Multiple read pairs can be included in the command line now. In order to have the same order of the pairs, \"Sample ID\" metadata has to be set. Otherwise, pairs will be ordered randomly.",
                        "sbg:revision": 23
                    },
                    {
                        "sbg:modifiedBy": "ana_d",
                        "sbg:modifiedOn": 1475749395,
                        "sbg:revisionNotes": "Expression for reads input fixed.",
                        "sbg:revision": 24
                    },
                    {
                        "sbg:modifiedBy": "dusan_randjelovic",
                        "sbg:modifiedOn": 1476200602,
                        "sbg:revisionNotes": "Toolkit version changed to 2.5.1b",
                        "sbg:revision": 25
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1476893159,
                        "sbg:revisionNotes": "Update some expressions to accpet multiple fastq files.",
                        "sbg:revision": 26
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1477489594,
                        "sbg:revisionNotes": "Unmapped reads output extension is now the same as the input read files extensions.",
                        "sbg:revision": 27
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1479131299,
                        "sbg:revisionNotes": "Fixed an encoding bug that could manifest in downstream analysis under Windows platform.",
                        "sbg:revision": 28
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1479808942,
                        "sbg:revisionNotes": "Fix unmapped reads output bug when no unmapped reads are found.",
                        "sbg:revision": 29
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1479911806,
                        "sbg:revisionNotes": "Add option to rename unmapped output files.",
                        "sbg:revision": 30
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1479919037,
                        "sbg:revisionNotes": "Add proper glob expression for unmapped reads",
                        "sbg:revision": 31
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1479921837,
                        "sbg:revisionNotes": "Fix unmapped output expression",
                        "sbg:revision": 32
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1480512657,
                        "sbg:revisionNotes": "Update sample_id metadata on outputs.",
                        "sbg:revision": 33
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1480516020,
                        "sbg:revisionNotes": "Update sample_id metadata.",
                        "sbg:revision": 34
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1480522227,
                        "sbg:revisionNotes": "Add boolean option for turning off read groups.",
                        "sbg:revision": 35
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1481205088,
                        "sbg:revisionNotes": "Add option to sort unmapped reads by ID.",
                        "sbg:revision": 36
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1481226048,
                        "sbg:revisionNotes": "Make sorting unmapped reads by read ID the default behavior.",
                        "sbg:revision": 37
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1481282806,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 38
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1481283137,
                        "sbg:revisionNotes": "Add option to sort unmapped reads by read ID (default is OFF).",
                        "sbg:revision": 39
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1485433817,
                        "sbg:revisionNotes": "Update read group expression to handle cases with files without any metadata.",
                        "sbg:revision": 40
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1485434147,
                        "sbg:revisionNotes": "Update read group expression to handle cases with files without any metadata.",
                        "sbg:revision": 41
                    }
                ],
                "sbg:createdBy": "uros_sipetic",
                "x": 583.5295573831879,
                "sbg:modifiedOn": 1485434147,
                "sbg:latestRevision": 41,
                "class": "CommandLineTool",
                "successCodes": [],
                "sbg:project": "uros_sipetic/star-2-5-1b-demo",
                "arguments": [
                    {
                        "valueFrom": {
                            "script": "{\n  file = [].concat($job.inputs.reads)[0].path\n  extension = /(?:\\.([^.]+))?$/.exec(file)[1]\n  if (extension == \"gz\") {\n    return \"--readFilesCommand zcat\"\n  } else if (extension == \"bz2\") {\n    return \"--readFilesCommand bzcat\"\n  }\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\t\n  var sjFormat = \"False\"\n  var gtfgffFormat = \"False\"\n  var list = $job.inputs.sjdbGTFfile\n  var paths_list = []\n  var joined_paths = \"\"\n  \n  if (list) {\n    list.forEach(function(f){return paths_list.push(f.path)})\n    joined_paths = paths_list.join(\" \")\n\n\n    paths_list.forEach(function(f){\n      ext = f.replace(/^.*\\./, '')\n      if (ext == \"gff\" || ext == \"gtf\") {\n        gtfgffFormat = \"True\"\n        return gtfgffFormat\n      }\n      if (ext == \"txt\") {\n        sjFormat = \"True\"\n        return sjFormat\n      }\n    })\n\n    if ($job.inputs.sjdbGTFfile && $job.inputs.sjdbInsertSave != \"None\") {\n      if (sjFormat == \"True\") {\n        return \"--sjdbFileChrStartEnd \".concat(joined_paths)\n      }\n      else if (gtfgffFormat == \"True\") {\n        return \"--sjdbGTFfile \".concat(joined_paths)\n      }\n    }\n  }\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n  a = b = c = d = e = f = g = []\n  if ($job.inputs.sjdbGTFchrPrefix) {\n    a = [\"--sjdbGTFchrPrefix\", $job.inputs.sjdbGTFchrPrefix]\n  }\n  if ($job.inputs.sjdbGTFfeatureExon) {\n    b = [\"--sjdbGTFfeatureExon\", $job.inputs.sjdbGTFfeatureExon]\n  }\n  if ($job.inputs.sjdbGTFtagExonParentTranscript) {\n    c = [\"--sjdbGTFtagExonParentTranscript\", $job.inputs.sjdbGTFtagExonParentTranscript]\n  }\n  if ($job.inputs.sjdbGTFtagExonParentGene) {\n    d = [\"--sjdbGTFtagExonParentGene\", $job.inputs.sjdbGTFtagExonParentGene]\n  }\n  if ($job.inputs.sjdbOverhang) {\n    e = [\"--sjdbOverhang\", $job.inputs.sjdbOverhang]\n  }\n  if ($job.inputs.sjdbScore) {\n    f = [\"--sjdbScore\", $job.inputs.sjdbScore]\n  }\n  if ($job.inputs.sjdbInsertSave) {\n    g = [\"--sjdbInsertSave\", $job.inputs.sjdbInsertSave]\n  }\n  \n  \n  \n  if ($job.inputs.sjdbInsertSave != \"None\" && $job.inputs.sjdbGTFfile) {\n    new_list = a.concat(b, c, d, e, f, g)\n    return new_list.join(\" \")\n  }\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n  if ($job.inputs.twopassMode == \"Basic\") {\n    if ($job.inputs.twopass1readsN) {\n      return \"--twopass1readsN \".concat($job.inputs.twopass1readsN) \n    } else {\n      return \"--twopass1readsN -1\"\n    }\n  }\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n  if ($job.inputs.chimOutType == \"Within\") {\n    return \"--chimOutType \".concat(\"Within\", $job.inputs.outSAMtype)\n  }\n  else {\n    return \"--chimOutType SeparateSAMold\"\n  }\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n  if ($job.inputs.no_read_groups) {\n  return \"\" }\n  else {\n  var param_list = []\n  var all_samples = []\n  var list = [].concat($job.inputs.reads)\n  getUnique = function(arr){\n    var u = {}, a = [];\n    for(var i = 0, l = arr.length; i < l; ++i){\n      if(u.hasOwnProperty(arr[i])) {\n        continue;\n      }\n      a.push(arr[i]);\n      u[arr[i]] = 1;\n    }\n    return a;\n  }\n  \n  function add_param(key, value){\n    if (value == \"\") {\n      return\n    }\n    else {\n      return param_list.push(key.concat(\":\", value))\n    }\n  }\n  for (index = 0; index < list.length; ++index) {\n    if (list[index].metadata != null){\n    \tif (list[index].metadata.sample_id != null){\n      \t\tall_samples.push(list[index].metadata.sample_id)\n      }\n    }\n  }\n  \n  samples = getUnique(all_samples)\n  var samples_given = all_samples.length == list.length\n  var all_rg = []\n  if (samples_given){\n    for (sample_ind = 0; sample_ind < samples.length; ++sample_ind){\n    \tfor (read_ind = 0; read_ind < list.length; ++read_ind) {\n        var param_list = []\n        if (list[read_ind].metadata && list[read_ind].metadata.sample_id == samples[sample_ind] && list[read_ind].metadata.paired_end != \"2\") {\n        \tadd_param('ID', samples[sample_ind])\n          if ($job.inputs.rg_seq_center) {\n            add_param('CN', $job.inputs.rg_seq_center)\n          } else if (list[read_ind].metadata && list[read_ind].metadata.seq_center) {\n            add_param('CN', list[read_ind].metadata.seq_center)\n          }\n          if ($job.inputs.rg_library_id) {\n            add_param('LB', $job.inputs.rg_library_id)\n          } else if (list[read_ind].metadata && list[read_ind].metadata.library_id) {\n            add_param('LB', list[read_ind].metadata.library_id)\n          }\n          if ($job.inputs.rg_mfl) {\n            add_param('PI', $job.inputs.rg_mfl)\n          } else if (list[read_ind].metadata && list[read_ind].metadata.median_fragment_length) {\n            add_param('PI', list[read_ind].metadata.median_fragment_length)\n          }\n          if ($job.inputs.rg_platform) {\n            add_param('PL', $job.inputs.rg_platform.replace(/ /g,\"_\"))\n          } else if (list[read_ind].metadata && list[read_ind].metadata.platform) {\n            add_param('PL', list[read_ind].metadata.platform.replace(/ /g,\"_\"))\n          }\n          if ($job.inputs.rg_platform_unit_id) {\n            add_param('PU', $job.inputs.rg_platform_unit_id)\n          } else if (list[read_ind].metadata && list[read_ind].metadata.platform_unit_id) {\n            add_param('PU', list[read_ind].metadata.platform_unit_id)\n          }\n          if ($job.inputs.rg_sample_id) {\n            add_param('SM', $job.inputs.rg_sample_id)\n          } else if (list[read_ind].metadata && list[read_ind].metadata.sample_id) {\n            add_param('SM', list[read_ind].metadata.sample_id)\n          }\n          all_rg.push(param_list.join(\" \"))\n       }\n\n    }\n   }\n  } else {\n  \tvar param_list = []\n    add_param('ID', \"1\")\n    if ($job.inputs.rg_seq_center) {\n      add_param('CN', $job.inputs.rg_seq_center)\n    } else if ([].concat($job.inputs.reads)[0].metadata && [].concat($job.inputs.reads)[0].metadata.seq_center) {\n      add_param('CN', [].concat($job.inputs.reads)[0].metadata.seq_center)\n    }\n    if ($job.inputs.rg_library_id) {\n      add_param('LB', $job.inputs.rg_library_id)\n    } else if ([].concat($job.inputs.reads)[0].metadata && [].concat($job.inputs.reads)[0].metadata.library_id) {\n      add_param('LB', [].concat($job.inputs.reads)[0].metadata.library_id)\n    }\n    if ($job.inputs.rg_mfl) {\n      add_param('PI', $job.inputs.rg_mfl)\n    } else if ([].concat($job.inputs.reads)[0].metadata && [].concat($job.inputs.reads)[0].metadata.median_fragment_length) {\n      add_param('PI', [].concat($job.inputs.reads)[0].metadata.median_fragment_length)\n    }\n    if ($job.inputs.rg_platform) {\n      add_param('PL', $job.inputs.rg_platform.replace(/ /g,\"_\"))\n    } else if ([].concat($job.inputs.reads)[0].metadata && [].concat($job.inputs.reads)[0].metadata.platform) {\n      add_param('PL', [].concat($job.inputs.reads)[0].metadata.platform.replace(/ /g,\"_\"))\n    }\n    if ($job.inputs.rg_platform_unit_id) {\n      add_param('PU', $job.inputs.rg_platform_unit_id)\n    } else if ([].concat($job.inputs.reads)[0].metadata && [].concat($job.inputs.reads)[0].metadata.platform_unit_id) {\n      add_param('PU', [].concat($job.inputs.reads)[0].metadata.platform_unit_id)\n    }\n    if ($job.inputs.rg_sample_id) {\n      add_param('SM', $job.inputs.rg_sample_id)\n    } else if ([].concat($job.inputs.reads)[0].metadata && [].concat($job.inputs.reads)[0].metadata.sample_id) {\n      add_param('SM', [].concat($job.inputs.reads)[0].metadata.sample_id)\n    }\n    return \"--outSAMattrRGline \".concat(param_list.join(\" \"))\n  }\n  return \"--outSAMattrRGline \".concat(all_rg.join(\" , \"))\n  }\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\n  if ($job.inputs.sjdbGTFfile && $job.inputs.quantMode) {\n    return \"--quantMode \".concat($job.inputs.quantMode)\n  }\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "separate": true
                    },
                    {
                        "position": 100,
                        "valueFrom": {
                            "script": "{\n  function sharedStart(array){\n  var A= array.concat().sort(), \n      a1= A[0], a2= A[A.length-1], L= a1.length, i= 0;\n  while(i<L && a1.charAt(i)=== a2.charAt(i)) i++;\n  return a1.substring(0, i);\n  }\n  path_list = []\n  arr = [].concat($job.inputs.reads)\n  arr.forEach(function(f){return path_list.push(f.path.replace(/\\\\/g,'/').replace( /.*\\//, '' ))})\n  common_prefix = sharedStart(path_list)\n  intermediate = common_prefix.replace( /\\-$|\\_$|\\.$/, '' ).concat(\"._STARgenome\")\n  source = \"./\".concat(intermediate)\n  destination = intermediate.concat(\".tar\")\n  if ($job.inputs.sjdbGTFfile && $job.inputs.sjdbInsertSave && $job.inputs.sjdbInsertSave != \"None\") {\n    return \"&& tar -vcf \".concat(destination, \" \", source)\n  }\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "separate": true
                    },
                    {
                        "prefix": "--outFileNamePrefix",
                        "valueFrom": {
                            "script": "{\n  function sharedStart(array){\n  var A= array.concat().sort(), \n      a1= A[0], a2= A[A.length-1], L= a1.length, i= 0;\n  while(i<L && a1.charAt(i)=== a2.charAt(i)) i++;\n  return a1.substring(0, i);\n  }\n  path_list = []\n  arr = [].concat($job.inputs.reads)\n  arr.forEach(function(f){return path_list.push(f.path.replace(/\\\\/g,'/').replace( /.*\\//, '' ))})\n  common_prefix = sharedStart(path_list)\n  if (common_prefix == \"\" || common_prefix == \"_\"){\n    common_prefix = \"All\"\n  }\n  return \"./\".concat(common_prefix.replace( /\\-$|\\_$|\\.$/, '' ), \".\")\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "separate": true
                    },
                    {
                        "position": 101,
                        "valueFrom": {
                            "script": "{\n  function sharedStart(array){\n  var A= array.concat().sort(), \n      a1= A[0], a2= A[A.length-1], L= a1.length, i= 0;\n  while(i<L && a1.charAt(i)=== a2.charAt(i)) i++;\n  return a1.substring(0, i);\n  }\n  path_list = []\n  arr = [].concat($job.inputs.reads)\n  arr.forEach(function(f){return path_list.push(f.path.replace(/\\\\/g,'/').replace( /.*\\//, '' ))})\n  common_prefix = sharedStart(path_list)\n  mate1 = common_prefix.replace( /\\-$|\\_$|\\.$/, '' ).concat(\".Unmapped.out.mate1\")\n  mate2 = common_prefix.replace( /\\-$|\\_$|\\.$/, '' ).concat(\".Unmapped.out.mate2\")\n  var x = arr[0].path.split('/').pop()\n  var y = x.toLowerCase()\n  \n  if ($job.inputs.unmappedOutputName) {\n  \tvar output_name = \".\" + $job.inputs.unmappedOutputName + \".\"\n  } else {\n    var output_name = \".Unmapped.out.\"\n  }\n  \n  mate1_1 = common_prefix.replace( /\\-$|\\_$|\\.$/, '' ).concat(output_name + \"mate1\")\n  mate2_1 = common_prefix.replace( /\\-$|\\_$|\\.$/, '' ).concat(output_name + \"mate2\")\n  \n  \n  if (y.endsWith('fastq') || y.endsWith('fq') || y.endsWith('fastq.gz') || y.endsWith('fastq.bz2') || y.endsWith('fq.gz') || y.endsWith('fq.bz2')) { \n    mate1fq = mate1_1.concat(\".fastq\")\n    mate2fq = mate2_1.concat(\".fastq\")\n  } else if (y.endsWith('fasta') || y.endsWith('fa') || y.endsWith('fasta.gz') || y.endsWith('fasta.bz2') || y.endsWith('fa.gz') || y.endsWith('fa.bz2')) {\n    mate1fq = mate1_1.concat(\".fasta\")\n    mate2fq = mate2_1.concat(\".fasta\")\n  }\n\n  \n  if ($job.inputs.sortUnmappedReads) {\n    \n  var cmd = \"\"\n  var sort_cmd = \" | sed 's/\\\\t.*//' | paste - - - - | sort -k1,1 -S 10G | tr '\\\\t' '\\\\n' > \"\n  if ($job.inputs.outReadsUnmapped == \"Fastx\" && arr.length > 1) {\n    cmd = cmd.concat(\" && cat \", mate2, sort_cmd, mate2fq, \" && rm \", mate2)\n  }\n  if ($job.inputs.outReadsUnmapped == \"Fastx\") {\n    cmd = cmd.concat(\" && cat \", mate1, sort_cmd, mate1fq, \" && rm \", mate1)\n  }\n  return cmd\n  \n  } else {\n\n  if ($job.inputs.outReadsUnmapped == \"Fastx\" && arr.length > 1) {\n    return \"&& mv \".concat(mate1, \" \", mate1fq, \" && mv \", mate2, \" \", mate2fq)\n  }\n  else if ($job.inputs.outReadsUnmapped == \"Fastx\" && arr.length == 1) {\n    return \"&& mv \".concat(mate1, \" \", mate1fq)\n  }\n\n  }\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "separate": true
                    }
                ],
                "description": "STAR is an ultrafast universal RNA-seq aligner. It has very high mapping speed, accurate alignment of contiguous and spliced reads, detection of polyA-tails, non-canonical splices and chimeric (fusion) junctions. It works with reads starting from lengths ~15 bases up to ~300 bases. In case of having longer reads, use of STAR Long is recommended.\n\n###Common issues###\n1. In case of paired-end alignment it is crucial to set metadata 'paired-end' field to 1/2.\n2. Files in multi-FASTQ format are currently not supported, i.e. if you have single-end reads that span multiple FASTQ files, or paired-end reads that span more than 2 files, please use a tool like SBG Merge FASTQs before providing your files to the STAR aligner. \n3. If you are providing a GFF3 file and wish to use STAR results for further downstream analysis, a good idea would be to set the \"Exons' parents name\" (id: sjdbGTFtagExonParentTranscript) option to \"Parent\".\n4. Unmapped reads are, by default, unsorted. If you want to sort them by read ID, please specify the \"Sort unmapped reads\" option, though keep in mind that this can increase STAR run time.",
                "sbg:revision": 41,
                "sbg:toolkit": "STAR",
                "inputs": [
                    {
                        "id": "#winFlankNbins",
                        "sbg:toolDefaultValue": "4",
                        "inputBinding": {
                            "prefix": "--winFlankNbins",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Flanking regions size",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Windows, Anchors, Binning",
                        "description": "=log2(winFlank), where win Flank is the size of the left and right flanking regions for each window (int>0)."
                    },
                    {
                        "id": "#winBinNbits",
                        "sbg:toolDefaultValue": "16",
                        "inputBinding": {
                            "prefix": "--winBinNbits",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Bin size",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Windows, Anchors, Binning",
                        "description": "=log2(winBin), where winBin is the size of the bin for the windows/clustering, each window will occupy an integer number of bins (int>0)."
                    },
                    {
                        "id": "#winAnchorMultimapNmax",
                        "sbg:toolDefaultValue": "50",
                        "inputBinding": {
                            "prefix": "--winAnchorMultimapNmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max loci anchors",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Windows, Anchors, Binning",
                        "description": "Max number of loci anchors are allowed to map to (int>0)."
                    },
                    {
                        "id": "#winAnchorDistNbins",
                        "sbg:toolDefaultValue": "9",
                        "inputBinding": {
                            "prefix": "--winAnchorDistNbins",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max bins between anchors",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Windows, Anchors, Binning",
                        "description": "Max number of bins between two anchors that allows aggregation of anchors into one window (int>0)."
                    },
                    {
                        "id": "#unmappedOutputName",
                        "sbg:toolDefaultValue": "\"Unmapped.out\"",
                        "label": "Unmapped output file names",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Output",
                        "description": "Names of the unmapped output files."
                    },
                    {
                        "id": "#twopassMode",
                        "sbg:toolDefaultValue": "None",
                        "inputBinding": {
                            "prefix": "--twopassMode",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Two-pass mode",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "None",
                                    "Basic"
                                ],
                                "name": "twopassMode"
                            }
                        ],
                        "sbg:category": "2-pass mapping",
                        "description": "2-pass mapping mode. None: 1-pass mapping; Basic: basic 2-pass mapping, with all 1st pass junctions inserted into the genome indices on the fly."
                    },
                    {
                        "id": "#twopass1readsN",
                        "sbg:toolDefaultValue": "-1",
                        "label": "Reads to process in 1st step",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "2-pass mapping",
                        "description": "Number of reads to process for the 1st step. 0: 1-step only, no 2nd pass; use very large number (or default -1) to map all reads in the first step (int>0)."
                    },
                    {
                        "id": "#sortUnmappedReads",
                        "sbg:toolDefaultValue": "Off",
                        "label": "Sort unmapped reads",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:category": "Output",
                        "description": "Unmapped reads are, by default, unsorted. If you want to sort them by read ID, please specify this option, though keep in mind that this can increase STAR run time."
                    },
                    {
                        "id": "#sjdbScore",
                        "sbg:toolDefaultValue": "2",
                        "label": "Extra alignment score",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Splice junctions database",
                        "description": "Extra alignment score for alignments that cross database junctions."
                    },
                    {
                        "id": "#sjdbOverhang",
                        "sbg:toolDefaultValue": "100",
                        "label": "\"Overhang\" length",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Splice junctions database",
                        "description": "Length of the donor/acceptor sequence on each side of the junctions, ideally = (mate_length - 1) (int >= 0), if int = 0, splice junction database is not used."
                    },
                    {
                        "id": "#sjdbInsertSave",
                        "sbg:toolDefaultValue": "None",
                        "label": "Save junction files",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "Basic",
                                    "All",
                                    "None"
                                ],
                                "name": "sjdbInsertSave"
                            }
                        ],
                        "sbg:category": "Splice junctions database",
                        "description": "Which files to save when sjdb junctions are inserted on the fly at the mapping step. None: not saving files at all; Basic: only small junction/transcript files; All: all files including big Genome, SA and SAindex. These files are output as archive."
                    },
                    {
                        "id": "#sjdbGTFtagExonParentTranscript",
                        "sbg:toolDefaultValue": "transcript_id",
                        "label": "Exons' parents name",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Splice junctions database",
                        "description": "Tag name to be used as exons transcript-parents."
                    },
                    {
                        "id": "#sjdbGTFtagExonParentGene",
                        "sbg:toolDefaultValue": "gene_id",
                        "label": "Gene name",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Splice junctions database",
                        "description": "Tag name to be used as exons gene-parents."
                    },
                    {
                        "sbg:fileTypes": "GTF, GFF, GFF2, GFF3, TXT",
                        "id": "#sjdbGTFfile",
                        "required": false,
                        "label": "Splice junction file",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File",
                                "name": "sjdbGTFfile"
                            }
                        ],
                        "sbg:category": "Basic",
                        "description": "Gene model annotations and/or known transcripts. No need to include this input, except in case of using \"on the fly\" annotations. If you are providing a GFF3 file and wish to use STAR results for further downstream analysis, a good idea would be to set the \"Exons' parents name\" (id: sjdbGTFtagExonParentTranscript) option to \"Parent\"."
                    },
                    {
                        "id": "#sjdbGTFfeatureExon",
                        "sbg:toolDefaultValue": "exon",
                        "label": "Set exons feature",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Splice junctions database",
                        "description": "Feature type in GTF file to be used as exons for building transcripts."
                    },
                    {
                        "id": "#sjdbGTFchrPrefix",
                        "sbg:toolDefaultValue": "-",
                        "label": "Chromosome names",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Splice junctions database",
                        "description": "Prefix for chromosome names in a GTF file (e.g. 'chr' for using ENSMEBL annotations with UCSC geneomes)."
                    },
                    {
                        "id": "#seedSearchStartLmaxOverLread",
                        "sbg:toolDefaultValue": "1.0",
                        "inputBinding": {
                            "prefix": "--seedSearchStartLmaxOverLread",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Search start point normalized",
                        "type": [
                            "null",
                            "float"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "SeedSearchStartLmax normalized to read length (sum of mates' lengths for paired-end reads)."
                    },
                    {
                        "id": "#seedSearchStartLmax",
                        "sbg:toolDefaultValue": "50",
                        "inputBinding": {
                            "prefix": "--seedSearchStartLmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Search start point",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Defines the search start point through the read - the read is split into pieces no longer than this value (int>0)."
                    },
                    {
                        "id": "#seedSearchLmax",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--seedSearchLmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max seed length",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Defines the maximum length of the seeds, if =0 max seed length is infinite (int>=0)."
                    },
                    {
                        "id": "#seedPerWindowNmax",
                        "sbg:toolDefaultValue": "50",
                        "inputBinding": {
                            "prefix": "--seedPerWindowNmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max seeds per window",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Max number of seeds per window (int>=0)."
                    },
                    {
                        "id": "#seedPerReadNmax",
                        "sbg:toolDefaultValue": "1000",
                        "inputBinding": {
                            "prefix": "--seedPerReadNmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max seeds per read",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Max number of seeds per read (int>=0)."
                    },
                    {
                        "id": "#seedNoneLociPerWindow",
                        "sbg:toolDefaultValue": "10",
                        "inputBinding": {
                            "prefix": "--seedNoneLociPerWindow",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max one-seed loci per window",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Max number of one seed loci per window (int>=0)."
                    },
                    {
                        "id": "#seedMultimapNmax",
                        "sbg:toolDefaultValue": "10000",
                        "inputBinding": {
                            "prefix": "--seedMultimapNmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Filter pieces for stitching",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Only pieces that map fewer than this value are utilized in the stitching procedure (int>=0)."
                    },
                    {
                        "id": "#scoreStitchSJshift",
                        "sbg:toolDefaultValue": "1",
                        "inputBinding": {
                            "prefix": "--scoreStitchSJshift",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max score reduction",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Scoring",
                        "description": "Maximum score reduction while searching for SJ boundaries in the stitching step."
                    },
                    {
                        "id": "#scoreInsOpen",
                        "sbg:toolDefaultValue": "-2",
                        "inputBinding": {
                            "prefix": "--scoreInsOpen",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Insertion Open Penalty",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Scoring",
                        "description": "Insertion open penalty."
                    },
                    {
                        "id": "#scoreInsBase",
                        "sbg:toolDefaultValue": "-2",
                        "inputBinding": {
                            "prefix": "--scoreInsBase",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Insertion extension penalty",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Scoring",
                        "description": "Insertion extension penalty per base (in addition to --scoreInsOpen)."
                    },
                    {
                        "id": "#scoreGenomicLengthLog2scale",
                        "sbg:toolDefaultValue": "-0.25",
                        "inputBinding": {
                            "prefix": "--scoreGenomicLengthLog2scale",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Log scaled score",
                        "type": [
                            "null",
                            "float"
                        ],
                        "sbg:category": "Scoring",
                        "description": "Extra score logarithmically scaled with genomic length of the alignment: <int>*log2(genomicLength)."
                    },
                    {
                        "id": "#scoreGapNoncan",
                        "sbg:toolDefaultValue": "-8",
                        "inputBinding": {
                            "prefix": "--scoreGapNoncan",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Non-canonical gap open",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Scoring",
                        "description": "Non-canonical gap open penalty (in addition to --scoreGap)."
                    },
                    {
                        "id": "#scoreGapGCAG",
                        "sbg:toolDefaultValue": "-4",
                        "inputBinding": {
                            "prefix": "--scoreGapGCAG",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "GC/AG and CT/GC gap open",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Scoring",
                        "description": "GC/AG and CT/GC gap open penalty (in addition to --scoreGap)."
                    },
                    {
                        "id": "#scoreGapATAC",
                        "sbg:toolDefaultValue": "-8",
                        "inputBinding": {
                            "prefix": "--scoreGapATAC",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "AT/AC and GT/AT gap open",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Scoring",
                        "description": "AT/AC and GT/AT gap open penalty (in addition to --scoreGap)."
                    },
                    {
                        "id": "#scoreGap",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--scoreGap",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Gap open penalty",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Scoring",
                        "description": "Gap open penalty."
                    },
                    {
                        "id": "#scoreDelOpen",
                        "sbg:toolDefaultValue": "-2",
                        "inputBinding": {
                            "prefix": "--scoreDelOpen",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Deletion open penalty",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Scoring",
                        "description": "Deletion open penalty."
                    },
                    {
                        "id": "#scoreDelBase",
                        "sbg:toolDefaultValue": "-2",
                        "inputBinding": {
                            "prefix": "--scoreDelBase",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Deletion extension penalty",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Scoring",
                        "description": "Deletion extension penalty per base (in addition to --scoreDelOpen)."
                    },
                    {
                        "id": "#rg_seq_center",
                        "sbg:toolDefaultValue": "Inferred from metadata",
                        "label": "Sequencing center",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Read group",
                        "description": "Specify the sequencing center for RG line."
                    },
                    {
                        "id": "#rg_sample_id",
                        "sbg:toolDefaultValue": "Inferred from metadata",
                        "label": "Sample ID",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Read group",
                        "description": "Specify the sample ID for RG line."
                    },
                    {
                        "id": "#rg_platform_unit_id",
                        "sbg:toolDefaultValue": "Inferred from metadata",
                        "label": "Platform unit ID",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Read group",
                        "description": "Specify the platform unit ID for RG line."
                    },
                    {
                        "id": "#rg_platform",
                        "sbg:toolDefaultValue": "Inferred from metadata",
                        "label": "Platform",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "LS 454",
                                    "Helicos",
                                    "Illumina",
                                    "ABI SOLiD",
                                    "Ion Torrent PGM",
                                    "PacBio"
                                ],
                                "name": "rg_platform"
                            }
                        ],
                        "sbg:category": "Read group",
                        "description": "Specify the version of the technology that was used for sequencing or assaying."
                    },
                    {
                        "id": "#rg_mfl",
                        "sbg:toolDefaultValue": "Inferred from metadata",
                        "label": "Median fragment length",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Read group",
                        "description": "Specify the median fragment length for RG line."
                    },
                    {
                        "id": "#rg_library_id",
                        "sbg:toolDefaultValue": "Inferred from metadata",
                        "label": "Library ID",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Read group",
                        "description": "Specify the library ID for RG line."
                    },
                    {
                        "sbg:fileTypes": "FASTA, FASTQ, FA, FQ, FASTQ.GZ, FQ.GZ, FASTQ.BZ2, FQ.BZ2",
                        "id": "#reads",
                        "required": true,
                        "inputBinding": {
                            "sbg:cmdInclude": true,
                            "position": 10,
                            "itemSeparator": " ",
                            "valueFrom": {
                                "script": "{\n  var list = [].concat($job.inputs.reads)\n  var all_samples = []\n  \n  getUnique = function(arr){\n    var u = {}, a = [];\n    for(var i = 0, l = arr.length; i < l; ++i){\n      if(u.hasOwnProperty(arr[i])) {\n        continue;\n      }\n      a.push(arr[i]);\n      u[arr[i]] = 1;\n    }\n    return a;\n  }\n    \n  for (index = 0; index < list.length; ++index) {\n    if (list[index].metadata != null){\n      all_samples.push(list[index].metadata.sample_id)\n    }\n  }\n  samples = getUnique(all_samples)\n  \n  var samples_given = all_samples.length == list.length\n  \n  for (index = 0; index < list.length; ++index) {\n    if (list[index].metadata != null){\n      all_samples.push(list[index].metadata.sample_id)\n    }\n  }\n  samples = getUnique(all_samples)\n  var resp = []\n  \n  if (list.length == 1){\n    resp.push(list[0].path)\n    \n  }else if (list.length == 2){    \n    \n    left = \"\"\n    right = \"\"\n      \n    for (index = 0; index < list.length; ++index) {\n      \n      if (list[index].metadata != null){\n        if (list[index].metadata.paired_end == 1){\n          left = list[index].path\n        }else if (list[index].metadata.paired_end == 2){\n          right = list[index].path\n        }\n      }\n    }\n    \n    if (left != \"\" && right != \"\"){      \n      resp.push(left)\n      resp.push(right)\n    }\n  }\n  else if (list.length > 2){\n    left = []\n    right = []\n    if (samples_given){\n      for (sample_ind = 0; sample_ind < samples.length; ++sample_ind){\n        for (read_ind = 0; read_ind < list.length; ++read_ind){\n          if (list[read_ind].metadata != null){\n            if (list[read_ind].metadata.paired_end == 1 && list[read_ind].metadata.sample_id == samples[sample_ind]){\n              left.push(list[read_ind].path)\n            }else if (list[read_ind].metadata.paired_end == 2 && list[read_ind].metadata.sample_id == samples[sample_ind]){\n              right.push(list[read_ind].path)}\n          }\n        }\n      }\n    } else {\n        for (index = 0; index < list.length; ++index) {\n\n          if (list[index].metadata != null){\n            if (list[index].metadata.paired_end == 1){\n              left.push(list[index].path)\n            }else if (list[index].metadata.paired_end == 2){\n              right.push(list[index].path)\n            }\n          }\n        }\n    }\n\n\n    left_join = left.join()\n    right_join = right.join()\n    if (left != [] && right != []){      \n      resp.push(left_join)\n      resp.push(right_join)\n    }\n    }\n  \n  if(resp.length > 0){    \n    return \"--readFilesIn \".concat(resp.join(\" \"))\n  }\n}",
                                "engine": "#cwl-js-engine",
                                "class": "Expression"
                            },
                            "separate": true
                        },
                        "label": "Read sequence",
                        "type": [
                            {
                                "type": "array",
                                "items": "File",
                                "name": "reads"
                            }
                        ],
                        "sbg:category": "Basic",
                        "description": "Read sequence."
                    },
                    {
                        "id": "#readMatesLengthsIn",
                        "sbg:toolDefaultValue": "NotEqual",
                        "inputBinding": {
                            "prefix": "--readMatesLengthsIn",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Reads lengths",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "NotEqual",
                                    "Equal"
                                ],
                                "name": "readMatesLengthsIn"
                            }
                        ],
                        "sbg:category": "Read parameters",
                        "description": "Equal/Not equal - lengths of names, sequences, qualities for both mates are the same/not the same. \"Not equal\" is safe in all situations."
                    },
                    {
                        "id": "#readMapNumber",
                        "sbg:toolDefaultValue": "-1",
                        "inputBinding": {
                            "prefix": "--readMapNumber",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Reads to map",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Read parameters",
                        "description": "Number of reads to map from the beginning of the file."
                    },
                    {
                        "id": "#quantTranscriptomeBan",
                        "sbg:toolDefaultValue": "IndelSoftclipSingleend",
                        "inputBinding": {
                            "prefix": "--quantTranscriptomeBan",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Prohibit alignment type",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "IndelSoftclipSingleend",
                                    "Singleend"
                                ],
                                "name": "quantTranscriptomeBan"
                            }
                        ],
                        "sbg:category": "Quantification of Annotations",
                        "description": "Prohibit various alignment type. IndelSoftclipSingleend: prohibit indels, soft clipping and single-end alignments - compatible with RSEM; Singleend: prohibit single-end alignments."
                    },
                    {
                        "id": "#quantMode",
                        "sbg:toolDefaultValue": "-",
                        "label": "Quantification mode",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "TranscriptomeSAM",
                                    "GeneCounts",
                                    "TranscriptomeSAM GeneCounts"
                                ],
                                "name": "quantMode"
                            }
                        ],
                        "sbg:category": "Quantification of Annotations",
                        "description": "Types of quantification requested. 'TranscriptomeSAM' option outputs SAM/BAM alignments to transcriptome into a separate file. With 'GeneCounts' option, STAR will count number of reads per gene while mapping."
                    },
                    {
                        "id": "#outSortingType",
                        "sbg:toolDefaultValue": "Unsorted",
                        "label": "Output sorting type",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "Unsorted",
                                    "SortedByCoordinate"
                                ],
                                "name": "outSortingType"
                            }
                        ],
                        "sbg:category": "Output",
                        "description": "Type of output sorting."
                    },
                    {
                        "id": "#outSJfilterReads",
                        "sbg:toolDefaultValue": "All",
                        "inputBinding": {
                            "prefix": "--outSJfilterReads",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Collapsed junctions reads",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "All",
                                    "Unique"
                                ],
                                "name": "outSJfilterReads"
                            }
                        ],
                        "sbg:category": "Output filtering: splice junctions",
                        "description": "Which reads to consider for collapsed splice junctions output. All: all reads, unique- and multi-mappers; Unique: uniquely mapping reads only."
                    },
                    {
                        "id": "#outSJfilterOverhangMin",
                        "sbg:toolDefaultValue": "30 12 12 12",
                        "inputBinding": {
                            "prefix": "--outSJfilterOverhangMin",
                            "itemSeparator": " ",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min overhang SJ",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "int"
                            }
                        ],
                        "sbg:category": "Output filtering: splice junctions",
                        "description": "Minimum overhang length for splice junctions on both sides for each of the motifs. To set no output for desired motif, assign -1 to the corresponding field. Does not apply to annotated junctions."
                    },
                    {
                        "id": "#outSJfilterIntronMaxVsReadN",
                        "sbg:toolDefaultValue": "50000 100000 200000",
                        "inputBinding": {
                            "prefix": "--outSJfilterIntronMaxVsReadN",
                            "itemSeparator": " ",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max gap allowed",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "int"
                            }
                        ],
                        "sbg:category": "Output filtering: splice junctions",
                        "description": "Maximum gap allowed for junctions supported by 1,2,3...N reads (int >= 0) i.e. by default junctions supported by 1 read can have gaps <=50000b, by 2 reads: <=100000b, by 3 reads: <=200000. By 4 or more reads: any gap <=alignIntronMax. Does not apply to annotated junctions."
                    },
                    {
                        "id": "#outSJfilterDistToOtherSJmin",
                        "sbg:toolDefaultValue": "10 0 5 10",
                        "inputBinding": {
                            "prefix": "--outSJfilterDistToOtherSJmin",
                            "itemSeparator": " ",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min distance to other donor/acceptor",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "int"
                            }
                        ],
                        "sbg:category": "Output filtering: splice junctions",
                        "description": "Minimum allowed distance to other junctions' donor/acceptor for each of the motifs (int >= 0). Does not apply to annotated junctions."
                    },
                    {
                        "id": "#outSJfilterCountUniqueMin",
                        "sbg:toolDefaultValue": "3 1 1 1",
                        "inputBinding": {
                            "prefix": "--outSJfilterCountUniqueMin",
                            "itemSeparator": " ",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min unique count",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "int"
                            }
                        ],
                        "sbg:category": "Output filtering: splice junctions",
                        "description": "Minimum uniquely mapping read count per junction for each of the motifs. To set no output for desired motif, assign -1 to the corresponding field. Junctions are output if one of --outSJfilterCountUniqueMin OR --outSJfilterCountTotalMin conditions are satisfied. Does not apply to annotated junctions."
                    },
                    {
                        "id": "#outSJfilterCountTotalMin",
                        "sbg:toolDefaultValue": "3 1 1 1",
                        "inputBinding": {
                            "prefix": "--outSJfilterCountTotalMin",
                            "itemSeparator": " ",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min total count",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "int"
                            }
                        ],
                        "sbg:category": "Output filtering: splice junctions",
                        "description": "Minimum total (multi-mapping+unique) read count per junction for each of the motifs. To set no output for desired motif, assign -1 to the corresponding field. Junctions are output if one of --outSJfilterCountUniqueMin OR --outSJfilterCountTotalMin conditions are satisfied. Does not apply to annotated junctions."
                    },
                    {
                        "id": "#outSAMunmapped",
                        "sbg:toolDefaultValue": "None",
                        "inputBinding": {
                            "prefix": "--outSAMunmapped",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Write unmapped in SAM",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "None",
                                    "Within",
                                    "Within KeepPairs"
                                ],
                                "name": "outSAMunmapped"
                            }
                        ],
                        "sbg:category": "Output",
                        "description": "Output of unmapped reads in the SAM format. None: no output Within: output unmapped reads within the main SAM file (i.e. Aligned.out.sam)."
                    },
                    {
                        "id": "#outSAMtype",
                        "sbg:toolDefaultValue": "SAM",
                        "inputBinding": {
                            "sbg:cmdInclude": true,
                            "valueFrom": {
                                "script": "{\n  SAM_type = $job.inputs.outSAMtype\n  SORT_type = $job.inputs.outSortingType\n  if (SAM_type && SORT_type) {\n    if (SAM_type==\"SAM\") {\n      return \"--outSAMtype SAM\"\n    } else {\n      return \"--outSAMtype \".concat(SAM_type, \" \", SORT_type)\n    }\n  } else if (SAM_type && SORT_type==null) {\n    if (SAM_type==\"SAM\") {\n      return \"--outSAMtype SAM\"\n    } else {\n      return \"--outSAMtype \".concat(SAM_type, \" Unsorted\")\n    }\n  }\n}",
                                "engine": "#cwl-js-engine",
                                "class": "Expression"
                            },
                            "separate": true
                        },
                        "label": "Output format",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "SAM",
                                    "BAM"
                                ],
                                "name": "outSAMtype"
                            }
                        ],
                        "sbg:category": "Output",
                        "description": "Format of output alignments."
                    },
                    {
                        "id": "#outSAMstrandField",
                        "sbg:toolDefaultValue": "None",
                        "inputBinding": {
                            "prefix": "--outSAMstrandField",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Strand field flag",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "None",
                                    "intronMotif"
                                ],
                                "name": "outSAMstrandField"
                            }
                        ],
                        "sbg:category": "Output",
                        "description": "Cufflinks-like strand field flag. None: not used; intronMotif: strand derived from the intron motif. Reads with inconsistent and/or non-canonical introns are filtered out."
                    },
                    {
                        "id": "#outSAMreadID",
                        "sbg:toolDefaultValue": "Standard",
                        "inputBinding": {
                            "prefix": "--outSAMreadID",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Read ID",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "Standard",
                                    "Number"
                                ],
                                "name": "outSAMreadID"
                            }
                        ],
                        "sbg:category": "Output",
                        "description": "Read ID record type. Standard: first word (until space) from the FASTx read ID line, removing /1,/2 from the end; Number: read number (index) in the FASTx file."
                    },
                    {
                        "id": "#outSAMprimaryFlag",
                        "sbg:toolDefaultValue": "OneBestScore",
                        "inputBinding": {
                            "prefix": "--outSAMprimaryFlag",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Primary alignments",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "OneBestScore",
                                    "AllBestScore"
                                ],
                                "name": "outSAMprimaryFlag"
                            }
                        ],
                        "sbg:category": "Output",
                        "description": "Which alignments are considered primary - all others will be marked with 0x100 bit in the FLAG. OneBestScore: only one alignment with the best score is primary; AllBestScore: all alignments with the best score are primary."
                    },
                    {
                        "id": "#outSAMorder",
                        "sbg:toolDefaultValue": "Paired",
                        "inputBinding": {
                            "prefix": "--outSAMorder",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Sorting in SAM",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "Paired",
                                    "PairedKeepInputOrder"
                                ],
                                "name": "outSAMorder"
                            }
                        ],
                        "sbg:category": "Output",
                        "description": "Type of sorting for the SAM output. Paired: one mate after the other for all paired alignments; PairedKeepInputOrder: one mate after the other for all paired alignments, the order is kept the same as in the input FASTQ files."
                    },
                    {
                        "id": "#outSAMmultNmax",
                        "sbg:toolDefaultValue": "-1",
                        "inputBinding": {
                            "prefix": "--outSAMmultNmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max number of multiple alignment",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Output",
                        "description": "Max number of multiple alignments for a read that will be output to the SAM/BAM files."
                    },
                    {
                        "id": "#outSAMmode",
                        "sbg:toolDefaultValue": "Full",
                        "inputBinding": {
                            "prefix": "--outSAMmode",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "SAM mode",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "Full",
                                    "NoQS",
                                    "None"
                                ],
                                "name": "outSAMmode"
                            }
                        ],
                        "sbg:category": "Output",
                        "description": "Mode of SAM output. Full: full SAM output; NoQS: full SAM but without quality scores."
                    },
                    {
                        "id": "#outSAMmapqUnique",
                        "sbg:toolDefaultValue": "255",
                        "inputBinding": {
                            "prefix": "--outSAMmapqUnique",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "MAPQ value",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Output",
                        "description": "MAPQ value for unique mappers (0 to 255)."
                    },
                    {
                        "id": "#outSAMheaderPG",
                        "sbg:toolDefaultValue": "-",
                        "inputBinding": {
                            "prefix": "--outSAMheaderPG",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "SAM header @PG",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Output",
                        "description": "Extra @PG (software) line of the SAM header (in addition to STAR)."
                    },
                    {
                        "id": "#outSAMheaderHD",
                        "sbg:toolDefaultValue": "-",
                        "inputBinding": {
                            "prefix": "--outSAMheaderHD",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "SAM header @HD",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Output",
                        "description": "@HD (header) line of the SAM header."
                    },
                    {
                        "id": "#outSAMflagOR",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--outSAMflagOR",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "OR SAM flag",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Output",
                        "description": "Set specific bits of the SAM FLAG."
                    },
                    {
                        "id": "#outSAMflagAND",
                        "sbg:toolDefaultValue": "65535",
                        "inputBinding": {
                            "prefix": "--outSAMflagAND",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "AND SAM flag",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Output",
                        "description": "Set specific bits of the SAM FLAG."
                    },
                    {
                        "id": "#outSAMfilter",
                        "sbg:toolDefaultValue": "None",
                        "inputBinding": {
                            "prefix": "--outSAMfilter",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Output filter",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "KeepOnlyAddedReferences",
                                    "None"
                                ],
                                "name": "outSAMfilter"
                            }
                        ],
                        "sbg:category": "Output",
                        "description": "Filter the output into main SAM/BAM files."
                    },
                    {
                        "id": "#outSAMattributes",
                        "sbg:toolDefaultValue": "Standard",
                        "inputBinding": {
                            "prefix": "--outSAMattributes",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "SAM attributes",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "Standard",
                                    "NH",
                                    "All",
                                    "None"
                                ],
                                "name": "outSAMattributes"
                            }
                        ],
                        "sbg:category": "Output",
                        "description": "Desired SAM attributes, in the order desired for the output SAM. NH: any combination in any order; Standard: NH HI AS nM; All: NH HI AS nM NM MD jM jI; None: no attributes."
                    },
                    {
                        "id": "#outSAMattrIHstart",
                        "sbg:toolDefaultValue": "1",
                        "inputBinding": {
                            "prefix": "--outSAMattrIHstart",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "IH attribute start value",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Output",
                        "description": "Start value for the IH attribute. 0 may be required by some downstream software, such as Cufflinks or StringTie."
                    },
                    {
                        "id": "#outReadsUnmapped",
                        "sbg:toolDefaultValue": "None",
                        "inputBinding": {
                            "prefix": "--outReadsUnmapped",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Output unmapped reads",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "None",
                                    "Fastx"
                                ],
                                "name": "outReadsUnmapped"
                            }
                        ],
                        "sbg:category": "Output",
                        "description": "Output of unmapped reads (besides SAM). None: no output; Fastx: output in separate fasta/fastq files, Unmapped.out.mate1/2."
                    },
                    {
                        "id": "#outQSconversionAdd",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--outQSconversionAdd",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Quality conversion",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Output",
                        "description": "Add this number to the quality score (e.g. to convert from Illumina to Sanger, use -31)."
                    },
                    {
                        "id": "#outMultimapperOrder",
                        "sbg:toolDefaultValue": "Old_2.4",
                        "inputBinding": {
                            "prefix": "--outMultimapperOrder",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Order of multimapping alignment",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "Random",
                                    "Old_2.4"
                                ],
                                "name": "outMultimapperOrder"
                            }
                        ],
                        "sbg:category": "Output",
                        "description": "Random option outputs multiple alignments for each read in random order, and also also randomizes the choice of the primary alignment from the highest scoring alignments."
                    },
                    {
                        "id": "#outFilterType",
                        "sbg:toolDefaultValue": "Normal",
                        "inputBinding": {
                            "prefix": "--outFilterType",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Filtering type",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "Normal",
                                    "BySJout"
                                ],
                                "name": "outFilterType"
                            }
                        ],
                        "sbg:category": "Output filtering",
                        "description": "Type of filtering. Normal: standard filtering using only current alignment; BySJout: keep only those reads that contain junctions that passed filtering into SJ.out.tab."
                    },
                    {
                        "id": "#outFilterScoreMinOverLread",
                        "sbg:toolDefaultValue": "0.66",
                        "inputBinding": {
                            "prefix": "--outFilterScoreMinOverLread",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min score normalized",
                        "type": [
                            "null",
                            "float"
                        ],
                        "sbg:category": "Output filtering",
                        "description": "'Minimum score' normalized to read length (sum of mates' lengths for paired-end reads)."
                    },
                    {
                        "id": "#outFilterScoreMin",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--outFilterScoreMin",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min score",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Output filtering",
                        "description": "Alignment will be output only if its score is higher than this value."
                    },
                    {
                        "id": "#outFilterMultimapScoreRange",
                        "sbg:toolDefaultValue": "1",
                        "inputBinding": {
                            "prefix": "--outFilterMultimapScoreRange",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Multimapping score range",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Output filtering",
                        "description": "The score range below the maximum score for multimapping alignments."
                    },
                    {
                        "id": "#outFilterMultimapNmax",
                        "sbg:toolDefaultValue": "10",
                        "inputBinding": {
                            "prefix": "--outFilterMultimapNmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max number of mappings",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Output filtering",
                        "description": "Read alignments will be output only if the read maps fewer than this value, otherwise no alignments will be output."
                    },
                    {
                        "id": "#outFilterMismatchNoverReadLmax",
                        "sbg:toolDefaultValue": "1",
                        "inputBinding": {
                            "prefix": "--outFilterMismatchNoverReadLmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Mismatches to *read* length",
                        "type": [
                            "null",
                            "float"
                        ],
                        "sbg:category": "Output filtering",
                        "description": "Alignment will be output only if its ratio of mismatches to *read* length is less than this value."
                    },
                    {
                        "id": "#outFilterMismatchNoverLmax",
                        "sbg:toolDefaultValue": "0.3",
                        "inputBinding": {
                            "prefix": "--outFilterMismatchNoverLmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Mismatches to *mapped* length",
                        "type": [
                            "null",
                            "float"
                        ],
                        "sbg:category": "Output filtering",
                        "description": "Alignment will be output only if its ratio of mismatches to *mapped* length is less than this value."
                    },
                    {
                        "id": "#outFilterMismatchNmax",
                        "sbg:toolDefaultValue": "10",
                        "inputBinding": {
                            "prefix": "--outFilterMismatchNmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max number of mismatches",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Output filtering",
                        "description": "Alignment will be output only if it has fewer mismatches than this value."
                    },
                    {
                        "id": "#outFilterMatchNminOverLread",
                        "sbg:toolDefaultValue": "0.66",
                        "inputBinding": {
                            "prefix": "--outFilterMatchNminOverLread",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min matched bases normalized",
                        "type": [
                            "null",
                            "float"
                        ],
                        "sbg:category": "Output filtering",
                        "description": "'Minimum matched bases' normalized to read length (sum of mates lengths for paired-end reads)."
                    },
                    {
                        "id": "#outFilterMatchNmin",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--outFilterMatchNmin",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min matched bases",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Output filtering",
                        "description": "Alignment will be output only if the number of matched bases is higher than this value."
                    },
                    {
                        "id": "#outFilterIntronMotifs",
                        "sbg:toolDefaultValue": "None",
                        "inputBinding": {
                            "prefix": "--outFilterIntronMotifs",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Motifs filtering",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "None",
                                    "RemoveNoncanonical",
                                    "RemoveNoncanonicalUnannotated"
                                ],
                                "name": "outFilterIntronMotifs"
                            }
                        ],
                        "sbg:category": "Output filtering",
                        "description": "Filter alignment using their motifs. None: no filtering; RemoveNoncanonical: filter out alignments that contain non-canonical junctions; RemoveNoncanonicalUnannotated: filter out alignments that contain non-canonical unannotated junctions when using annotated splice junctions database. The annotated non-canonical junctions will be kept."
                    },
                    {
                        "id": "#no_read_groups",
                        "sbg:toolDefaultValue": "Off",
                        "label": "No read groups",
                        "type": [
                            "null",
                            "boolean"
                        ],
                        "sbg:category": "Read group",
                        "description": "If this boolean argument is specified, no read groups will be set in the resulting BAM header."
                    },
                    {
                        "id": "#limitSjdbInsertNsj",
                        "sbg:toolDefaultValue": "1000000",
                        "inputBinding": {
                            "prefix": "--limitSjdbInsertNsj",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max insert junctions",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Limits",
                        "description": "Maximum number of junction to be inserted to the genome on the fly at the mapping stage, including those from annotations and those detected in the 1st step of the 2-pass run."
                    },
                    {
                        "id": "#limitOutSJoneRead",
                        "sbg:toolDefaultValue": "1000",
                        "inputBinding": {
                            "prefix": "--limitOutSJoneRead",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Junctions max number",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Limits",
                        "description": "Max number of junctions for one read (including all multi-mappers)."
                    },
                    {
                        "id": "#limitOutSJcollapsed",
                        "sbg:toolDefaultValue": "1000000",
                        "inputBinding": {
                            "prefix": "--limitOutSJcollapsed",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Collapsed junctions max number",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Limits",
                        "description": "Max number of collapsed junctions."
                    },
                    {
                        "id": "#limitBAMsortRAM",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--limitBAMsortRAM",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Limit BAM sorting memory",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Limits",
                        "description": "Maximum available RAM for sorting BAM. If set to 0, it will be set to the genome index size."
                    },
                    {
                        "id": "#genomeDirName",
                        "sbg:toolDefaultValue": "genomeDir",
                        "inputBinding": {
                            "prefix": "--genomeDir",
                            "position": 0,
                            "valueFrom": {
                                "script": "$job.inputs.genomeDirName || \"genomeDir\"",
                                "engine": "#cwl-js-engine",
                                "class": "Expression"
                            },
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Genome dir name",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Basic",
                        "description": "Name of the directory which contains genome files (when genome.tar is uncompressed)."
                    },
                    {
                        "sbg:fileTypes": "TAR",
                        "id": "#genome",
                        "required": true,
                        "label": "Genome files",
                        "type": [
                            "File"
                        ],
                        "sbg:category": "Basic",
                        "description": "Genome files created using STAR Genome Generate."
                    },
                    {
                        "id": "#clip5pNbases",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--clip5pNbases",
                            "itemSeparator": " ",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Clip 5p bases",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "int"
                            }
                        ],
                        "sbg:category": "Read parameters",
                        "description": "Number of bases to clip from 5p of each mate. In case only one value is given, it will be assumed the same for both mates."
                    },
                    {
                        "id": "#clip3pNbases",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--clip3pNbases",
                            "itemSeparator": " ",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Clip 3p bases",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "int"
                            }
                        ],
                        "sbg:category": "Read parameters",
                        "description": "Number of bases to clip from 3p of each mate. In case only one value is given, it will be assumed the same for both mates."
                    },
                    {
                        "id": "#clip3pAfterAdapterNbases",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--clip3pAfterAdapterNbases",
                            "itemSeparator": " ",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Clip 3p after adapter seq",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "int",
                                "name": "clip3pAfterAdapterNbases"
                            }
                        ],
                        "sbg:category": "Read parameters",
                        "description": "Number of bases to clip from 3p of each mate after the adapter clipping. In case only one value is given, it will be assumed the same for both mates."
                    },
                    {
                        "id": "#clip3pAdapterSeq",
                        "sbg:toolDefaultValue": "-",
                        "inputBinding": {
                            "prefix": "--clip3pAdapterSeq",
                            "itemSeparator": " ",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Clip 3p adapter sequence",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "string"
                            }
                        ],
                        "sbg:category": "Read parameters",
                        "description": "Adapter sequence to clip from 3p of each mate. In case only one value is given, it will be assumed the same for both mates."
                    },
                    {
                        "id": "#clip3pAdapterMMp",
                        "sbg:toolDefaultValue": "0.1",
                        "inputBinding": {
                            "prefix": "--clip3pAdapterMMp",
                            "itemSeparator": " ",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max mismatches proportions",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "float"
                            }
                        ],
                        "sbg:category": "Read parameters",
                        "description": "Max proportion of mismatches for 3p adapter clipping for each mate. In case only one value is given, it will be assumed the same for both mates."
                    },
                    {
                        "id": "#chimSegmentReadGapMax",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--chimSegmentReadGapMax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Chimeric segment gap",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Chimeric Alignments",
                        "description": "Maximum gap in the read sequence between chimeric segments (int>=0)."
                    },
                    {
                        "id": "#chimSegmentMin",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--chimSegmentMin",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min segment length",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Chimeric Alignments",
                        "description": "Minimum length of chimeric segment length, if =0, no chimeric output (int>=0)."
                    },
                    {
                        "id": "#chimScoreSeparation",
                        "sbg:toolDefaultValue": "10",
                        "inputBinding": {
                            "prefix": "--chimScoreSeparation",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min separation score",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Chimeric Alignments",
                        "description": "Minimum difference (separation) between the best chimeric score and the next one (int>=0)."
                    },
                    {
                        "id": "#chimScoreMin",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--chimScoreMin",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min total score",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Chimeric Alignments",
                        "description": "Minimum total (summed) score of the chimeric segments (int>=0)."
                    },
                    {
                        "id": "#chimScoreJunctionNonGTAG",
                        "sbg:toolDefaultValue": "-1",
                        "inputBinding": {
                            "prefix": "--chimScoreJunctionNonGTAG",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Non-GT/AG penalty",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Chimeric Alignments",
                        "description": "Penalty for a non-GT/AG chimeric junction."
                    },
                    {
                        "id": "#chimScoreDropMax",
                        "sbg:toolDefaultValue": "20",
                        "inputBinding": {
                            "prefix": "--chimScoreDropMax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max drop score",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Chimeric Alignments",
                        "description": "Max drop (difference) of chimeric score (the sum of scores of all chimeric segements) from the read length (int>=0)."
                    },
                    {
                        "id": "#chimOutType",
                        "sbg:toolDefaultValue": "SeparateSAMold",
                        "label": "Chimeric output type",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "SeparateSAMold",
                                    "Within"
                                ],
                                "name": "chimOutType"
                            }
                        ],
                        "sbg:category": "Chimeric Alignments",
                        "description": "Type of chimeric output. SeparateSAMold: output old SAM into separate Chimeric.out.sam file; Within: output into main aligned SAM/BAM files."
                    },
                    {
                        "id": "#chimJunctionOverhangMin",
                        "sbg:toolDefaultValue": "20",
                        "inputBinding": {
                            "prefix": "--chimJunctionOverhangMin",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min junction overhang",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Chimeric Alignments",
                        "description": "Minimum overhang for a chimeric junction (int>=0)."
                    },
                    {
                        "id": "#chimFilter",
                        "sbg:toolDefaultValue": "banGenomicN",
                        "inputBinding": {
                            "prefix": "--chimFilter",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Chimeric filter",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "banGenomicN",
                                    "None"
                                ],
                                "name": "chimFilter"
                            }
                        ],
                        "sbg:category": "Chimeric Alignments",
                        "description": "Different filters for chimeric alignments None no filtering banGenomicN Ns are not allowed in the genome sequence around the chimeric junction."
                    },
                    {
                        "id": "#alignWindowsPerReadNmax",
                        "sbg:toolDefaultValue": "10000",
                        "inputBinding": {
                            "prefix": "--alignWindowsPerReadNmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max windows per read",
                        "type": [
                            "null",
                            "float"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Max number of windows per read (int>0)."
                    },
                    {
                        "id": "#alignTranscriptsPerWindowNmax",
                        "sbg:toolDefaultValue": "100",
                        "inputBinding": {
                            "prefix": "--alignTranscriptsPerWindowNmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max transcripts per window",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Max number of transcripts per window (int>0)."
                    },
                    {
                        "id": "#alignTranscriptsPerReadNmax",
                        "sbg:toolDefaultValue": "10000",
                        "inputBinding": {
                            "prefix": "--alignTranscriptsPerReadNmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max transcripts per read",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Max number of different alignments per read to consider (int>0)."
                    },
                    {
                        "id": "#alignSplicedMateMapLminOverLmate",
                        "sbg:toolDefaultValue": "0.66",
                        "inputBinding": {
                            "prefix": "--alignSplicedMateMapLminOverLmate",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min mapped length normalized",
                        "type": [
                            "null",
                            "float"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "AlignSplicedMateMapLmin normalized to mate length (float>0)."
                    },
                    {
                        "id": "#alignSplicedMateMapLmin",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--alignSplicedMateMapLmin",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min mapped length",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Minimum mapped length for a read mate that is spliced (int>0)."
                    },
                    {
                        "id": "#alignSoftClipAtReferenceEnds",
                        "sbg:toolDefaultValue": "Yes",
                        "inputBinding": {
                            "prefix": "--alignSoftClipAtReferenceEnds",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Soft clipping",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "Yes",
                                    "No"
                                ],
                                "name": "alignSoftClipAtReferenceEnds"
                            }
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Option which allows soft clipping of alignments at the reference (chromosome) ends. Can be disabled for compatibility with Cufflinks/Cuffmerge. Yes: Enables soft clipping; No: Disables soft clipping."
                    },
                    {
                        "id": "#alignSJstitchMismatchNmax",
                        "sbg:toolDefaultValue": "0 -1 0 0",
                        "inputBinding": {
                            "prefix": "--alignSJstitchMismatchNmax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Splice junction stich max mismatch",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "4*int>=0: maximum number of mismatches for stitching of the splice junctions (-1: no limit). (1) non-canonical motifs, (2) GT/AG and CT/AC motif, (3) GC/AG and CT/GC motif, (4) AT/AC and GT/AT motif."
                    },
                    {
                        "id": "#alignSJoverhangMin",
                        "sbg:toolDefaultValue": "5",
                        "inputBinding": {
                            "prefix": "--alignSJoverhangMin",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min overhang",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Minimum overhang (i.e. block size) for spliced alignments (int>0)."
                    },
                    {
                        "id": "#alignSJDBoverhangMin",
                        "sbg:toolDefaultValue": "3",
                        "inputBinding": {
                            "prefix": "--alignSJDBoverhangMin",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min overhang: annotated",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Minimum overhang (i.e. block size) for annotated (sjdb) spliced alignments (int>0)."
                    },
                    {
                        "id": "#alignMatesGapMax",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--alignMatesGapMax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max mates gap",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Maximum gap between two mates, if 0, max intron gap will be determined by (2^winBinNbits)*winAnchorDistNbins."
                    },
                    {
                        "id": "#alignIntronMin",
                        "sbg:toolDefaultValue": "21",
                        "inputBinding": {
                            "prefix": "--alignIntronMin",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Min intron size",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Minimum intron size: genomic gap is considered intron if its length >= alignIntronMin, otherwise it is considered Deletion (int>=0)."
                    },
                    {
                        "id": "#alignIntronMax",
                        "sbg:toolDefaultValue": "0",
                        "inputBinding": {
                            "prefix": "--alignIntronMax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Max intron size",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Maximum intron size, if 0, max intron size will be determined by (2^winBinNbits)*winAnchorDistNbins."
                    },
                    {
                        "id": "#alignEndsType",
                        "sbg:toolDefaultValue": "Local",
                        "inputBinding": {
                            "prefix": "--alignEndsType",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Alignment type",
                        "type": [
                            "null",
                            {
                                "type": "enum",
                                "symbols": [
                                    "Local",
                                    "EndToEnd",
                                    "Extend5pOfRead1",
                                    "Extend3pOfRead1"
                                ],
                                "name": "alignEndsType"
                            }
                        ],
                        "sbg:category": "Alignments and Seeding",
                        "description": "Type of read ends alignment. Local: standard local alignment with soft-clipping allowed. EndToEnd: force end to end read alignment, do not soft-clip; Extend5pOfRead1: fully extend only the 5p of the read1, all other ends: local alignment."
                    }
                ]
            },
            "inputs": [
                {
                    "id": "#STAR_1.winFlankNbins"
                },
                {
                    "id": "#STAR_1.winBinNbits"
                },
                {
                    "id": "#STAR_1.winAnchorMultimapNmax"
                },
                {
                    "id": "#STAR_1.winAnchorDistNbins"
                },
                {
                    "id": "#STAR_1.unmappedOutputName"
                },
                {
                    "id": "#STAR_1.twopassMode"
                },
                {
                    "id": "#STAR_1.twopass1readsN"
                },
                {
                    "id": "#STAR_1.sortUnmappedReads"
                },
                {
                    "id": "#STAR_1.sjdbScore"
                },
                {
                    "default": 100,
                    "id": "#STAR_1.sjdbOverhang"
                },
                {
                    "id": "#STAR_1.sjdbInsertSave"
                },
                {
                    "id": "#STAR_1.sjdbGTFtagExonParentTranscript"
                },
                {
                    "id": "#STAR_1.sjdbGTFtagExonParentGene"
                },
                {
                    "id": "#STAR_1.sjdbGTFfile",
                    "source": [
                        "#sjdbGTFfile"
                    ]
                },
                {
                    "id": "#STAR_1.sjdbGTFfeatureExon"
                },
                {
                    "id": "#STAR_1.sjdbGTFchrPrefix"
                },
                {
                    "id": "#STAR_1.seedSearchStartLmaxOverLread"
                },
                {
                    "id": "#STAR_1.seedSearchStartLmax"
                },
                {
                    "id": "#STAR_1.seedSearchLmax"
                },
                {
                    "id": "#STAR_1.seedPerWindowNmax"
                },
                {
                    "id": "#STAR_1.seedPerReadNmax"
                },
                {
                    "id": "#STAR_1.seedNoneLociPerWindow"
                },
                {
                    "id": "#STAR_1.seedMultimapNmax"
                },
                {
                    "id": "#STAR_1.scoreStitchSJshift"
                },
                {
                    "id": "#STAR_1.scoreInsOpen"
                },
                {
                    "id": "#STAR_1.scoreInsBase"
                },
                {
                    "id": "#STAR_1.scoreGenomicLengthLog2scale"
                },
                {
                    "id": "#STAR_1.scoreGapNoncan"
                },
                {
                    "id": "#STAR_1.scoreGapGCAG"
                },
                {
                    "id": "#STAR_1.scoreGapATAC"
                },
                {
                    "id": "#STAR_1.scoreGap"
                },
                {
                    "id": "#STAR_1.scoreDelOpen"
                },
                {
                    "id": "#STAR_1.scoreDelBase"
                },
                {
                    "id": "#STAR_1.rg_seq_center"
                },
                {
                    "id": "#STAR_1.rg_sample_id"
                },
                {
                    "id": "#STAR_1.rg_platform_unit_id"
                },
                {
                    "id": "#STAR_1.rg_platform"
                },
                {
                    "id": "#STAR_1.rg_mfl"
                },
                {
                    "id": "#STAR_1.rg_library_id"
                },
                {
                    "id": "#STAR_1.reads",
                    "source": [
                        "#SBG_FASTQ_Quality_Detector.result"
                    ]
                },
                {
                    "id": "#STAR_1.readMatesLengthsIn"
                },
                {
                    "id": "#STAR_1.readMapNumber"
                },
                {
                    "id": "#STAR_1.quantTranscriptomeBan"
                },
                {
                    "default": "TranscriptomeSAM",
                    "id": "#STAR_1.quantMode"
                },
                {
                    "id": "#STAR_1.outSortingType"
                },
                {
                    "id": "#STAR_1.outSJfilterReads"
                },
                {
                    "id": "#STAR_1.outSJfilterOverhangMin"
                },
                {
                    "id": "#STAR_1.outSJfilterIntronMaxVsReadN"
                },
                {
                    "id": "#STAR_1.outSJfilterDistToOtherSJmin"
                },
                {
                    "id": "#STAR_1.outSJfilterCountUniqueMin"
                },
                {
                    "id": "#STAR_1.outSJfilterCountTotalMin"
                },
                {
                    "id": "#STAR_1.outSAMunmapped"
                },
                {
                    "default": "BAM",
                    "id": "#STAR_1.outSAMtype"
                },
                {
                    "id": "#STAR_1.outSAMstrandField"
                },
                {
                    "id": "#STAR_1.outSAMreadID"
                },
                {
                    "id": "#STAR_1.outSAMprimaryFlag"
                },
                {
                    "id": "#STAR_1.outSAMorder"
                },
                {
                    "id": "#STAR_1.outSAMmultNmax"
                },
                {
                    "id": "#STAR_1.outSAMmode"
                },
                {
                    "id": "#STAR_1.outSAMmapqUnique"
                },
                {
                    "id": "#STAR_1.outSAMheaderPG"
                },
                {
                    "id": "#STAR_1.outSAMheaderHD"
                },
                {
                    "id": "#STAR_1.outSAMflagOR"
                },
                {
                    "id": "#STAR_1.outSAMflagAND"
                },
                {
                    "id": "#STAR_1.outSAMfilter"
                },
                {
                    "id": "#STAR_1.outSAMattributes"
                },
                {
                    "id": "#STAR_1.outSAMattrIHstart"
                },
                {
                    "default": "Fastx",
                    "id": "#STAR_1.outReadsUnmapped"
                },
                {
                    "id": "#STAR_1.outQSconversionAdd"
                },
                {
                    "id": "#STAR_1.outMultimapperOrder"
                },
                {
                    "id": "#STAR_1.outFilterType"
                },
                {
                    "id": "#STAR_1.outFilterScoreMinOverLread"
                },
                {
                    "id": "#STAR_1.outFilterScoreMin"
                },
                {
                    "id": "#STAR_1.outFilterMultimapScoreRange"
                },
                {
                    "id": "#STAR_1.outFilterMultimapNmax"
                },
                {
                    "id": "#STAR_1.outFilterMismatchNoverReadLmax"
                },
                {
                    "id": "#STAR_1.outFilterMismatchNoverLmax"
                },
                {
                    "id": "#STAR_1.outFilterMismatchNmax"
                },
                {
                    "id": "#STAR_1.outFilterMatchNminOverLread"
                },
                {
                    "id": "#STAR_1.outFilterMatchNmin"
                },
                {
                    "id": "#STAR_1.outFilterIntronMotifs"
                },
                {
                    "id": "#STAR_1.no_read_groups"
                },
                {
                    "id": "#STAR_1.limitSjdbInsertNsj"
                },
                {
                    "id": "#STAR_1.limitOutSJoneRead"
                },
                {
                    "id": "#STAR_1.limitOutSJcollapsed"
                },
                {
                    "id": "#STAR_1.limitBAMsortRAM"
                },
                {
                    "id": "#STAR_1.genomeDirName"
                },
                {
                    "id": "#STAR_1.genome",
                    "source": [
                        "#STAR_Genome_Generate.genome"
                    ]
                },
                {
                    "id": "#STAR_1.clip5pNbases"
                },
                {
                    "id": "#STAR_1.clip3pNbases"
                },
                {
                    "id": "#STAR_1.clip3pAfterAdapterNbases"
                },
                {
                    "id": "#STAR_1.clip3pAdapterSeq"
                },
                {
                    "id": "#STAR_1.clip3pAdapterMMp"
                },
                {
                    "id": "#STAR_1.chimSegmentReadGapMax"
                },
                {
                    "id": "#STAR_1.chimSegmentMin"
                },
                {
                    "id": "#STAR_1.chimScoreSeparation"
                },
                {
                    "id": "#STAR_1.chimScoreMin"
                },
                {
                    "id": "#STAR_1.chimScoreJunctionNonGTAG"
                },
                {
                    "id": "#STAR_1.chimScoreDropMax"
                },
                {
                    "id": "#STAR_1.chimOutType"
                },
                {
                    "id": "#STAR_1.chimJunctionOverhangMin"
                },
                {
                    "id": "#STAR_1.chimFilter"
                },
                {
                    "id": "#STAR_1.alignWindowsPerReadNmax"
                },
                {
                    "id": "#STAR_1.alignTranscriptsPerWindowNmax"
                },
                {
                    "id": "#STAR_1.alignTranscriptsPerReadNmax"
                },
                {
                    "id": "#STAR_1.alignSplicedMateMapLminOverLmate"
                },
                {
                    "id": "#STAR_1.alignSplicedMateMapLmin"
                },
                {
                    "id": "#STAR_1.alignSoftClipAtReferenceEnds"
                },
                {
                    "id": "#STAR_1.alignSJstitchMismatchNmax"
                },
                {
                    "id": "#STAR_1.alignSJoverhangMin"
                },
                {
                    "id": "#STAR_1.alignSJDBoverhangMin"
                },
                {
                    "id": "#STAR_1.alignMatesGapMax"
                },
                {
                    "id": "#STAR_1.alignIntronMin"
                },
                {
                    "id": "#STAR_1.alignIntronMax"
                },
                {
                    "id": "#STAR_1.alignEndsType"
                }
            ]
        },
        {
            "id": "#STAR_Genome_Generate",
            "outputs": [
                {
                    "id": "#STAR_Genome_Generate.genome"
                }
            ],
            "sbg:y": 445.88244087721034,
            "sbg:x": 372.9412336052509,
            "run": {
                "sbg:contributors": [
                    "dusan_randjelovic",
                    "uros_sipetic"
                ],
                "id": "https://api.sbgenomics.com/v2/apps/uros_sipetic/star-2-5-1b-demo/star-genome-generate-2-5-1-b/20/raw/",
                "stdin": "",
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
                "outputs": [
                    {
                        "sbg:fileTypes": "TAR",
                        "id": "#genome",
                        "label": "Genome Files",
                        "outputBinding": {
                            "glob": "*.tar",
                            "sbg:inheritMetadataFrom": "#reference_or_index",
                            "sbg:metadata": {
                                "reference_genome": {
                                    "script": "{\n  var str1 = [].concat($job.inputs.reference_or_index)[0].path.split('/')\n  var str2 = str1[str1.length-1]\n  var str3 = str2.split('.')\n  var str4 = \"\"\n  for (i=0; i<str3.length-1; i++) {\n    if (i<str3.length-2) { \n    str4 = str4 + str3[i] + \".\"\n    }\n    else {\n      str4 = str4 + str3[i]\n    }\n  }\n  var tmp = str3.pop()\n  if (tmp.toLowerCase()=='fa' || tmp.toLowerCase()=='fasta') {\n    return str4\n  } else if (tmp.toLowerCase()=='tar') {\n    return [].concat($job.inputs.reference_or_index)[0].metadata.reference_genome\n  }\n}\n",
                                    "engine": "#cwl-js-engine",
                                    "class": "Expression"
                                }
                            }
                        },
                        "description": "Genome files comprise binary genome sequence, suffix arrays, text chromosome names/lengths, splice junctions coordinates, and transcripts/genes information.",
                        "type": [
                            "null",
                            "File"
                        ]
                    }
                ],
                "sbg:toolkitVersion": "2.5.1b",
                "sbg:links": [
                    {
                        "label": "Homepage",
                        "id": "https://github.com/alexdobin/STAR"
                    },
                    {
                        "label": "Releases",
                        "id": "https://github.com/alexdobin/STAR/releases"
                    },
                    {
                        "label": "Manual",
                        "id": "https://github.com/alexdobin/STAR/blob/master/doc/STARmanual.pdf"
                    },
                    {
                        "label": "Support",
                        "id": "https://groups.google.com/forum/#!forum/rna-star"
                    },
                    {
                        "label": "Publication",
                        "id": "http://www.ncbi.nlm.nih.gov/pubmed/23104886"
                    }
                ],
                "hints": [
                    {
                        "class": "DockerRequirement",
                        "dockerPull": "images.sbgenomics.com/ana_d/star-fusion:2.5.1b",
                        "dockerImageId": ""
                    },
                    {
                        "value": 32,
                        "class": "sbg:CPURequirement"
                    },
                    {
                        "value": 60000,
                        "class": "sbg:MemRequirement"
                    }
                ],
                "sbg:revisionNotes": "Update GTF expression to properly accept files with uppercase extensions.",
                "stdout": "",
                "sbg:createdOn": 1462811152,
                "sbg:image_url": null,
                "sbg:cmdPreview": "mkdir genomeDir && /opt/STAR-2.5.1b/bin/Linux_x86_64_static/STAR --runMode genomeGenerate --genomeDir ./genomeDir --runThreadN 32 --genomeFastaFiles /sbgenomics/test-data/chr20.fa  --sjdbGTFfile /demo/test-files/chr20.gtf  && tar -vcf chr20_chr20_star-2.5.1b-index-archive.tar ./genomeDir",
                "sbg:sbgMaintained": false,
                "sbg:id": "admin/sbg-public-data/star-genome-generate-2-5-1-b/20",
                "temporaryFailCodes": [],
                "cwlVersion": "sbg:draft-2",
                "sbg:validationErrors": [],
                "sbg:categories": [
                    "Alignment"
                ],
                "sbg:license": "GNU General Public License v3.0 only",
                "sbg:job": {
                    "allocatedResources": {
                        "mem": 60000,
                        "cpu": 32
                    },
                    "inputs": {
                        "genomeSuffixLengthMax": 10,
                        "reference_or_index": {
                            "secondaryFiles": [],
                            "size": 0,
                            "path": "/sbgenomics/test-data/chr20.fa",
                            "class": "File"
                        },
                        "sjdbGTFtagExonParentGene": "sjdbGTFtagExonParentGene",
                        "sjdbGTFfeatureExon": "sjdbGTFfeatureExon",
                        "genomeSAsparseD": 0,
                        "sjdbGTFchrPrefix": "sjdbGTFchrPrefix",
                        "sjdbGTFfile": [
                            {
                                "secondaryFiles": [],
                                "size": 0,
                                "path": "/demo/test-files/chr20.gtf",
                                "class": "File"
                            }
                        ],
                        "sjdbScore": 0,
                        "sjdbGTFtagExonParentTranscript": "sjdbGTFtagExonParentTranscript",
                        "genomeSAindexNbases": 0,
                        "sjdbOverhang": 0,
                        "genomeChrBinNbits": "genomeChrBinNbits"
                    }
                },
                "label": "STAR Genome Generate",
                "sbg:modifiedBy": "uros_sipetic",
                "sbg:toolAuthor": "Alexander Dobin/CSHL",
                "baseCommand": [
                    {
                        "script": "{\n  var x = $job.inputs.reference_or_index.path.split('/').pop()\n  var y = x.split('.').pop()\n  var z = $job.allocatedResources.cpu\n  if (y == 'fa' || y == 'fasta' || y == 'FA' || y == \"FASTA\") {\n    return \"mkdir genomeDir && /opt/STAR-2.5.1b/bin/Linux_x86_64_static/STAR --runMode genomeGenerate --genomeDir ./genomeDir --runThreadN \" + z\n  } else if (y == 'tar' || y == 'TAR') {\n    return \"echo 'Tar bundle provided, skipping indexing.' \"\n  }\n}\n",
                        "engine": "#cwl-js-engine",
                        "class": "Expression"
                    }
                ],
                "sbg:revisionsInfo": [
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1462811152,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 0
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1462878623,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 1
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1469452193,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 2
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1469458871,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 3
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1470664269,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 4
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1471013964,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 5
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1471277606,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 6
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1471277909,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 7
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1471435375,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 8
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1471435842,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 9
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1471439923,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 10
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1471871838,
                        "sbg:revisionNotes": "Updated description.",
                        "sbg:revision": 11
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1471881882,
                        "sbg:revisionNotes": "Fixed a command line bug.",
                        "sbg:revision": 12
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1472136332,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 13
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1472223272,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 14
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1473070682,
                        "sbg:revisionNotes": "Changed number of cores from 15 to 32.",
                        "sbg:revision": 15
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1475755006,
                        "sbg:revisionNotes": null,
                        "sbg:revision": 16
                    },
                    {
                        "sbg:modifiedBy": "dusan_randjelovic",
                        "sbg:modifiedOn": 1476200578,
                        "sbg:revisionNotes": "Toolkit version changed to 2.5.1b",
                        "sbg:revision": 17
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1477489679,
                        "sbg:revisionNotes": "Add information about proper use with GFF3 files.",
                        "sbg:revision": 18
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1479131293,
                        "sbg:revisionNotes": "Fixed an encoding bug that could manifest in downstream analysis under Windows platform.",
                        "sbg:revision": 19
                    },
                    {
                        "sbg:modifiedBy": "uros_sipetic",
                        "sbg:modifiedOn": 1486635876,
                        "sbg:revisionNotes": "Update GTF expression to properly accept files with uppercase extensions.",
                        "sbg:revision": 20
                    }
                ],
                "sbg:createdBy": "uros_sipetic",
                "arguments": [
                    {
                        "position": 99,
                        "valueFrom": {
                            "script": "{\n  var tmp1 = [].concat($job.inputs.reference_or_index)[0].path.split('/').pop()\n  if ($job.inputs.sjdbGTFfile) {\n    var tmp2 = [].concat($job.inputs.sjdbGTFfile)[0].path.split('/').pop()\n  } else {\n    var tmp2 = \"\"\n  }\n  \n  var str1 = tmp1.split('.')\n  var x1 = \"\"\n  for (i=0; i<str1.length-1; i++) {\n    if (i<str1.length-2) { \n    x1 = x1 + str1[i] + \".\"\n    }\n    else {\n      x1 = x1 + str1[i]\n    }\n  }\n  \n  var str2 = tmp2.split('.')\n  var x2 = \"\"\n  for (i=0; i<str2.length-1; i++) {\n    if (i<str2.length-2) { \n    x2 = x2 + str2[i] + \".\"\n    }\n    else {\n      x2 = x2 + str2[i] + \"_\"\n    }\n  }\n  var tmp3 = $job.inputs.reference_or_index.path.split('/').pop()\n  var tmp4 = tmp3.split('.').pop()\n  if (tmp4 == 'tar' || tmp4 == 'TAR') {\n    return \"\"\n  } else {\n    return \"&& tar -vcf \" + x1 + \"_\" + x2 + \"star-2.5.1b-index-archive.tar ./genomeDir \"\n  }\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "separate": true
                    },
                    {
                        "valueFrom": {
                            "script": "{\t\n  var sjFormat = \"False\"\n  var gtfgffFormat = \"False\"\n  var list = $job.inputs.sjdbGTFfile\n  var paths_list = []\n  var joined_paths = \"\"\n  \n  if (list) {\n    list.forEach(function(f){return paths_list.push(f.path)})\n    joined_paths = paths_list.join(\" \")\n\n\n    paths_list.forEach(function(f){\n      ext = f.replace(/^.*\\./, '')\n      ext2 = ext.toLowerCase()\n      if (ext2 == \"gff\" || ext2 == \"gtf\" || ext2 == \"gff2\" || ext2 == \"gff3\" || ext2 == \"txt\") {\n        gtfgffFormat = \"True\"\n        return gtfgffFormat\n      }\n      if (ext == \"txt\") {\n        sjFormat = \"True\"\n        return sjFormat\n      }\n    })\n\n    if ($job.inputs.sjdbGTFfile && $job.inputs.sjdbInsertSave != \"None\") {\n      if (sjFormat == \"True\") {\n        return \"--sjdbFileChrStartEnd \".concat(joined_paths)\n      }\n      else if (gtfgffFormat == \"True\") {\n        return \"--sjdbGTFfile \".concat(joined_paths)\n      }\n    }\n  }\n}",
                            "engine": "#cwl-js-engine",
                            "class": "Expression"
                        },
                        "separate": true
                    }
                ],
                "sbg:modifiedOn": 1486635876,
                "sbg:latestRevision": 20,
                "class": "CommandLineTool",
                "successCodes": [],
                "sbg:project": "uros_sipetic/star-2-5-1b-demo",
                "description": "STAR Genome Generate is a tool that generates genome index files. One set of files should be generated per each genome/annotation combination. Once produced, these files could be used as long as genome/annotation combination stays the same. Also, STAR Genome Generate which produced these files and STAR aligner using them must be the same toolkit version.\n\n###Common issues###\n1. If the indexes for a desired fasta/gtf pair have already been generated, make sure to supply the resulting TAR bundle to the tool input if you are using this tool in a workflow in order to skip unnecessary indexing and speed up the whole workflow process.\n2. If you are providing a GFF3 file and wish to use STAR results for further downstream analysis, a good idea would be to set the \"Exons' parents name\" (id: sjdbGTFtagExonParentTranscript) option to \"Parent\".",
                "sbg:revision": 20,
                "sbg:toolkit": "STAR",
                "inputs": [
                    {
                        "sbg:fileTypes": "FASTA, FA, FNA, TAR",
                        "id": "#reference_or_index",
                        "sbg:stageInput": "link",
                        "inputBinding": {
                            "prefix": "--genomeFastaFiles",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Reference/Index files",
                        "type": [
                            "File"
                        ],
                        "sbg:category": "Basic",
                        "description": "Reference sequence to which to align the reads, or a TAR bundle containg already generated indices."
                    },
                    {
                        "id": "#genomeChrBinNbits",
                        "sbg:toolDefaultValue": "18",
                        "inputBinding": {
                            "prefix": "--genomeChrBinNbits",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Bins size",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Genome generation parameters",
                        "description": "Set log2(chrBin), where chrBin is the size (bits) of the bins for genome storage: each chromosome will occupy an integer number of bins. If you are using a genome with a large (>5,000) number of chrosomes/scaffolds, you may need to reduce this number to reduce RAM consumption. The following scaling is recomended: genomeChrBinNbits = min(18, log2(GenomeLength/NumberOfReferences)). For example, for 3 gigaBase genome with 100,000 chromosomes/scaffolds, this is equal to 15."
                    },
                    {
                        "id": "#genomeSAindexNbases",
                        "sbg:toolDefaultValue": "14",
                        "inputBinding": {
                            "prefix": "--genomeSAindexNbases",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Pre-indexing string length",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Genome generation parameters",
                        "description": "Length (bases) of the SA pre-indexing string. Typically between 10 and 15. Longer strings will use much more memory, but allow faster searches. For small genomes, this number needs to be scaled down, with a typical value of min(14, log2(GenomeLength)/2 - 1). For example, for 1 megaBase genome, this is equal to 9, for 100 kiloBase genome, this is equal to 7."
                    },
                    {
                        "id": "#genomeSAsparseD",
                        "sbg:toolDefaultValue": "1",
                        "inputBinding": {
                            "prefix": "--genomeSAsparseD",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Suffux array sparsity",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Genome generation parameters",
                        "description": "Distance between indices: use bigger numbers to decrease needed RAM at the cost of mapping speed reduction (int>0)."
                    },
                    {
                        "sbg:fileTypes": "GTF, GFF, GFF2, GFF3, TXT",
                        "id": "#sjdbGTFfile",
                        "sbg:stageInput": "link",
                        "label": "Splice junction file",
                        "type": [
                            "null",
                            {
                                "type": "array",
                                "items": "File",
                                "name": "sjdbGTFfile"
                            }
                        ],
                        "sbg:category": "Basic",
                        "description": "Gene model annotations and/or known transcripts. If you are providing a GFF3 file and wish to use STAR results for further downstream analysis, a good idea would be to set the \"Exons' parents name\" (id: sjdbGTFtagExonParentTranscript) option to \"Parent\"."
                    },
                    {
                        "id": "#sjdbGTFfeatureExon",
                        "sbg:toolDefaultValue": "exon",
                        "inputBinding": {
                            "prefix": "--sjdbGTFfeatureExon",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Set exons feature",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Splice junctions db parameters",
                        "description": "Feature type in GTF file to be used as exons for building transcripts."
                    },
                    {
                        "id": "#sjdbGTFtagExonParentTranscript",
                        "sbg:toolDefaultValue": "transcript_id",
                        "inputBinding": {
                            "prefix": "--sjdbGTFtagExonParentTranscript",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Exons' parents name",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Splice junctions db parameters",
                        "description": "Tag name to be used as exons transcript-parents."
                    },
                    {
                        "id": "#sjdbGTFtagExonParentGene",
                        "sbg:toolDefaultValue": "gene_id",
                        "inputBinding": {
                            "prefix": "--sjdbGTFtagExonParentGene",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Gene name",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Splice junctions db parameters",
                        "description": "Tag name to be used as exons gene-parents."
                    },
                    {
                        "id": "#sjdbOverhang",
                        "sbg:toolDefaultValue": "100",
                        "inputBinding": {
                            "prefix": "--sjdbOverhang",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "\"Overhang\" length",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Splice junctions db parameters",
                        "description": "Length of the donor/acceptor sequence on each side of the junctions, ideally = (mate_length - 1) (int >= 0), if int = 0, splice junction database is not used."
                    },
                    {
                        "id": "#sjdbScore",
                        "sbg:toolDefaultValue": "2",
                        "inputBinding": {
                            "prefix": "--sjdbScore",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Extra alignment score",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Splice junctions db parameters",
                        "description": "Extra alignment score for alignments that cross database junctions."
                    },
                    {
                        "id": "#sjdbGTFchrPrefix",
                        "sbg:toolDefaultValue": "-",
                        "inputBinding": {
                            "prefix": "--sjdbGTFchrPrefix",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Chromosome names",
                        "type": [
                            "null",
                            "string"
                        ],
                        "sbg:category": "Splice junctions db parameters",
                        "description": "Prefix for chromosome names in a GTF file (e.g. 'chr' for using ENSMEBL annotations with UCSC geneomes)."
                    },
                    {
                        "id": "#genomeSuffixLengthMax",
                        "sbg:toolDefaultValue": "-1",
                        "inputBinding": {
                            "prefix": "--genomeSuffixLengthMax",
                            "separate": true,
                            "sbg:cmdInclude": true
                        },
                        "label": "Maximum genome suffic length",
                        "type": [
                            "null",
                            "int"
                        ],
                        "sbg:category": "Genome generation parameters",
                        "description": "Maximum length of the suffixes, has to be longer than read length. -1 = infinite."
                    }
                ]
            },
            "inputs": [
                {
                    "id": "#STAR_Genome_Generate.reference_or_index",
                    "source": [
                        "#reference_or_index"
                    ]
                },
                {
                    "id": "#STAR_Genome_Generate.genomeChrBinNbits"
                },
                {
                    "id": "#STAR_Genome_Generate.genomeSAindexNbases"
                },
                {
                    "id": "#STAR_Genome_Generate.genomeSAsparseD"
                },
                {
                    "id": "#STAR_Genome_Generate.sjdbGTFfile",
                    "source": [
                        "#sjdbGTFfile"
                    ]
                },
                {
                    "id": "#STAR_Genome_Generate.sjdbGTFfeatureExon"
                },
                {
                    "id": "#STAR_Genome_Generate.sjdbGTFtagExonParentTranscript"
                },
                {
                    "id": "#STAR_Genome_Generate.sjdbGTFtagExonParentGene"
                },
                {
                    "id": "#STAR_Genome_Generate.sjdbOverhang"
                },
                {
                    "id": "#STAR_Genome_Generate.sjdbScore"
                },
                {
                    "id": "#STAR_Genome_Generate.sjdbGTFchrPrefix"
                },
                {
                    "id": "#STAR_Genome_Generate.genomeSuffixLengthMax"
                }
            ]
        }
    ],
    "sbg:canvas_zoom": 0.95,
    "cwlVersion": "sbg:draft-2",
    "sbg:toolAuthor": "Seven Bridges Genomics",
    "sbg:categories": [
        "Alignment",
        "RNA"
    ],
    "sbg:license": "Apache License 2.0",
    "sbg:canvas_y": -66,
    "label": "RNA-seq Alignment - STAR 2.5.1b for TCGA PE tar",
    "sbg:modifiedBy": "bixqa",
    "hints": [],
    "sbg:revisionsInfo": [
        {
            "sbg:modifiedBy": "bixqa",
            "sbg:modifiedOn": 1501518670,
            "sbg:revisionNotes": "Copy of uros_sipetic/rna-seq-alignment-star-2-5-1b-demo/rna-seq-alignment-star-for-tcga-pe-tar/2",
            "sbg:revision": 0
        },
        {
            "sbg:modifiedBy": "bixqa",
            "sbg:modifiedOn": 1501518670,
            "sbg:revisionNotes": null,
            "sbg:revision": 1
        },
        {
            "sbg:modifiedBy": "bixqa",
            "sbg:modifiedOn": 1501518670,
            "sbg:revisionNotes": "Update STAR Genome Generate.",
            "sbg:revision": 2
        }
    ],
    "sbg:createdBy": "bixqa",
    "sbg:modifiedOn": 1501518670,
    "sbg:revision": 2,
    "class": "Workflow",
    "sbg:project": "bixqa/qa-load-2017-07-31-18",
    "sbg:canvas_x": 120,
    "inputs": [
        {
            "sbg:fileTypes": "TAR, TAR.GZ, TGZ, TAR.BZ2, TBZ2,  GZ, BZ2, ZIP",
            "label": "Input Read Files",
            "sbg:y": 323.52939750248936,
            "id": "#input_archive_file",
            "sbg:x": 57.6470513525304,
            "type": [
                "File"
            ]
        },
        {
            "sbg:fileTypes": "FASTA, FA, FNA, TAR",
            "label": "reference_or_index",
            "sbg:y": 469.0909645774147,
            "sbg:suggestedValue": {
                "path": "57bd5d15507c17b56d99b0d5",
                "name": "human_g1k_v37_decoy.phiX174_Homo_sapiens.GRCh37.75_star-2.5.1b.tar",
                "class": "File"
            },
            "sbg:x": 151.969687721946,
            "type": [
                "File"
            ],
            "id": "#reference_or_index"
        },
        {
            "sbg:fileTypes": "GTF, GFF, GFF2, GFF3, TXT",
            "label": "sjdbGTFfile",
            "sbg:y": 176.36363636363635,
            "sbg:suggestedValue": [
                {
                    "path": "5772b6c4507c1752674486cd",
                    "name": "Homo_sapiens.GRCh37.75.gtf",
                    "class": "File"
                }
            ],
            "sbg:x": 139.09088134765625,
            "type": [
                "null",
                {
                    "type": "array",
                    "items": "File",
                    "name": "sjdbGTFfile"
                }
            ],
            "id": "#sjdbGTFfile"
        }
    ],
    "sbg:projectName": "qa-load-2017-07-31-18",
    "sbg:latestRevision": 2,
    "sbg:toolkit": "STAR",
    "description": "This pipeline performs the first step of RNA-Seq analysis - alignment to a reference genome and transcriptome. STAR, an ultrafast RNA-seq aligner is used in this pipeline. STAR is capable of mapping full length RNA sequences and detecting de novo canonical junctions, non-canonical splices, and chimeric (fusion) transcripts. It is optimized for mammalian sequence reads, but fine tuning of its parameters enables customization to satisfy unique needs.\n###Required inputs\nThis workflow has two **required** inputs:\n\n1.Input read files (ID: *input_archive_file*) - STAR accepts one file per sample (TCGA tarball file containing both paired-ends).  \n\n2. Reference/Index files (ID: *reference_or_index*) - reference sequence to which to align the reads, or a TAR bundle of already generated index files.\n###Optional input:\nSplice junction annotations (ID: *sjdbGTFfile*) - this input can optionally be collected from splice junction databases. \n###Outputs\nThis workflow generates **nine** output files:\n\n1. Unmapped reads (ID: *unmapped_reads*) - unmapped reads are reported in FASTQ format.\n\n2. Transcriptome alignments (ID: *transcriptome_aligned_reads*) - alignments translated into transcript coordinates.  \n\n3. Splice junctions (Outputs ID: *splice_junctions*) - high confidence collapsed splice junctions in tab-delimited format. Only junctions supported by uniquely mapping reads are reported.\n\n4. Reads per gene (ID: *reads_per_gene*) - file with number of reads per gene. A read is counted if it overlaps (1nt or more) one and only one gene.\n\n5. Log files (ID: *log_files*) - a set of log files produced during alignment\n\n6. Intermediate genome files (ID: *intermediate_genome*) - archive with genome files produced when annotations are included on the fly (in the mapping step).\n\n7. Chimeric junctions (ID: *chimeric_junctions*) \n\n8. Chimeric alignments (ID: *chimeric_alignments*) \n\n9. Sorted bam file (ID: *sorted_bam*) - output aligned sequence, bam sorted\n\nSTAR can detect chimeric transcripts, but parameter \"Min segment length\" (ID: *chimSegmentMin*) in \"Chimeric Alignments\" category must be adjusted to a desired minimum chimeric segment length.\n\nIf you want to use STAR results as an input to an RNA-seq differential expression analysis(using the cufflinks app), please also set the parameter \"utSAMstrandField\" to \"intronMotif\". \n\n###Common issues###\n1. For paired-end alignments it is crucial to set the metadata 'paired-end' field as 1 and 2 respectively for the two input fastq files, otherwise the task will fail.\n\n2. If you already have a TAR bundle of generated index files (obtained from STAR Genome Generate), you can provide the TAR bundle instead of a FASTA reference file to skip the indexing and reduce the overall workflow execution time."
}