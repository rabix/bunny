# Support notice

## Summary
_Rabix Executor_ will remain available as a local, single node, `sbg:draft2` and `CWL 1.0` test executor. However, 
please note that Seven Bridges is not actively maintaining or extending _Rabix Executor_ at the moment (Q2/Q3 2019). 

If you are looking for an on premises CWL executor with a variety of backends ranging from HPC to Cloud, we would 
suggest looking at [Toil](https://toil.readthedocs.io/en/latest/running/cwl.html#) (`pip install toil[cwl]`) 

If you are looking for the CWL reference runner (suitable for testing and prototyping) we suggest looking at [CWLtool](https://github.com/common-workflow-language/cwltool) (`pip install cwltool`)

You can also browse the complete list of CWL executors [here](https://www.commonwl.org/#Implementations).

_Rabix Executor remains a stable local test runner for those people wishing to run `sbg:draft2` pipelines locally, 
but no new features will be added to it._

**_[Rabix Composer](https://github.com/rabix/composer)_ is and will be actively maintained and extended by 
Seven Bridges and we continue to actively participate in CWL specification development.**

## Background
When the _Rabix_ project was in it's infancy and the CWL specification was just being developed (ca. 2014-2016) 
_Rabix Executor_ was the only executor capable of running CWL. It proved invaluable as a test bed for ideas for 
the specification that evolved into CWL and as a living implementation of the specification.

Now, in 2019, there are a variety of CWL executors running on a variety of OSes and job platforms, meant for a variety
of workloads, ranging from  your laptop to HPC environments to cloud infrastructure. The community maintained 
program `CWLtool` now serves the purpose of CWL test bed and test executor and there is no longer a need for a 
separately maintained CWL test executor. 


# Overview

Rabix is an open-source development kit for the [Common Workflow Language](http://www.commonwl.org/) from [Seven Bridges](https://sbgenomics.com). 
One of its components is the Rabix Executor (Bunny), which can be used to execute apps locally from the command line.

You can read the full documentation on [wiki pages](https://github.com/rabix/bunny/wiki).

# Installing

Before downloading, make sure you have [Docker](https://docs.docker.com/engine/installation/) and [Java Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (version 8+) installed.

Download the latest release available at [https://github.com/rabix/bunny/releases](https://github.com/rabix/bunny/releases).
Once the download has been completed, unpack the downloaded archive.

The following command will automatically download and unpack the archive in the newly created `rabix` directory:

```sh
wget https://github.com/rabix/bunny/releases/download/v1.0.5/rabix-1.0.5.tar.gz -O rabix-1.0.5.tar.gz && tar -xvf rabix-1.0.5.tar.gz
```


# Running the example

To run the example `dna2protein` workflow which is included in the downloaded archive, execute the following command:

```
./rabix examples/dna2protein/dna2protein.cwl.json examples/dna2protein/inputs.json
```

This will execute the dna2protein workflow. Once the execution has been completed, information about the produced output will be shown in the terminal window (name and path of the output file, file size, checksum).

The example consists of two CWL tools (Transcribe, Translate) and one workflow (dna2protein). Transcribe takes a TXT file containing a DNA Sequence as an input and produces a TXT file with an mRNA Sequence. Translate takes an mRNA Sequence, identifies the first ORF, and produces a TXT file with a peptide sequence as an output.


# Command Line Reference
The general format of the command that is used to run the Rabix executor is:

```
./rabix [OPTIONS] <app> <inputs>
```

Under [OPTIONS] you can specify the `--no-container` option which means that the execution will take place on your local machine instead of a Docker container.

For the execution to be successful, you will need all prerequisites for the app(s) you are running the be already installed on your machine. For example, if the app you are running is essentially a Python script, you will need Python on your machine to be able to run the app without an adequate Docker container.

`<app>` - The CWL file that describes the app (dna2protein.cwl.json in the example above).
`<inputs>` - The file in which app inputs are listed and described (inputs.json in the example above). This file is not required if using the inline options described below.

Optionally, you can also explicitly specify values of input parameters in the command line, as follows:

```
./rabix [OPTIONS] <app> <inputs> -- --<input_port_id> <value>
```

The part of the command line where you can specify input parameters comes after the `--` separator. The value for each input parameter is entered in the form of `--<input_port_id> <value>`.
This is what the full command line would look like if providing values two input parameters whose IDs are e.g. fastq and threads:

```
./rabix tool.json inputs.json -- --fastq sample1.fastq.gz --threads 4
```

When specifying the value for an input ports whose type is file, relative paths are resolved relative to the `<inputs>` file if supplied in that file or relative to current working directory is supplied on the command line.

If the same input parameter is specified both in the `<inputs>` file and through the command line, the one specified in the command line will take precedence.

If the input parameter you are specifying through the command line is a list of values, the parameter needs to be repeated as many times as there are values, e.g. `--fasta file1.fasta --fasta file2.fasta`.
