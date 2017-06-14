#dna2protein

This example consists of two CWL tools (Transcribe, Translate) and one workflow (dna2protein).

Transcribe takes a TXT file containing a DNA Sequence as an input and produces a TXT file with an mRNA Sequence.
Translate takes an mRNA Sequence, identifies the first ORF, and produces a TXT file with a peptide sequence as an output.
dna2protein consists of input -> Transcribe > Translate --> output.

All CWL apps in this example were written using the Seven Bridges platform.
