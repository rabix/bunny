#!/usr/bin/env/python
import argparse
import re
import sys

def transcribe(args):
	# create a transcription map and use regex to translate
	map = {"A":"U", "T":"A", "C":"G", "G":"C"}
	map = dict((re.escape(k), v) for k, v in map.iteritems())
	pattern = re.compile("|".join(map.keys()))
	DNA = args['dna'].read().strip()
	mRNA = pattern.sub(lambda m: map[re.escape(m.group(0))], DNA)

	# write a verbose output to stderr and just mRNA to sdtout 
	if args['verbose']:
		sys.stderr.write("Your original DNA sequence: " + DNA + "\n")
		sys.stderr.write("Your translated mRNA sequence: " + mRNA + "\n")
	sys.stdout.write(mRNA + '\n')
	sys.exit(0)
	return mRNA

if __name__ == "__main__":
	""" Parse the command line arguments """
	parser = argparse.ArgumentParser()
	parser.add_argument("-d", "--dna", type=argparse.FileType("r"), default=sys.stdin)
	parser.add_argument("-v", "--verbose", action="store_true", default=False)
	# By setting args as var(...), it becomes a dict, so 'dna' is a key
	# Alternative use: args = parser.parse_args(), and 'dna' is an attr of args!
	# You must change how you call the args you parse based on this usage! 
	args = vars(parser.parse_args())

	""" Run the desired methods """
	transcribe(args)