#!/usr/bin/env python
# Can be used with a bilingual dictionary like the one in:
#  http://www.ilovelanguages.com/IDP/files/Spanish.txt
import sys

eng2esdict = {}
for line in open( sys.argv[1] ) :
    if line.startswith('#') : continue
    words = line.split()
    eng2esdict[words[0]] = words[1]

for line in open( sys.argv[2] ) :
    for word in line.split() :
	    word = word.lower()
	    print word
	    if word in eng2esdict :
	      print eng2esdict[word],
