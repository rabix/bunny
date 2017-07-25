[![Build Status](https://travis-ci.org/rabix/bunny.svg?branch=master)](https://travis-ci.org/rabix/bunny)


# Overview

Rabix is an open-source development kit for the [Common Workflow Language](http://www.commonwl.org/) from [Seven Bridges](https://sbgenomics.com). 
One of its components is the Rabix executor Bunny, which can be used to execute apps locally from the command line.

You can read the full documentation on [wiki pages](https://github.com/rabix/bunny/wiki).

# Installing

Before downloading, make sure you have [Docker](https://docs.docker.com/engine/installation/) and [Java Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (version 8+) installed.

Download the latest release available at [https://github.com/rabix/bunny/releases](https://github.com/rabix/bunny/releases).
Once the download has been completed, unpack the downloaded archive.

The following command will automatically download and unpack the archive in the newly created `rabix` directory:

```sh
wget https://github.com/rabix/bunny/releases/download/v1.0.1/rabix-1.0.1.tar.gz && tar -xvf rabix-1.0.1.tar.gz
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

