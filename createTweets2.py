#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys
import random
from Vibe.vibe import Vibe

#CRITERIA
#0 UNDERTERMINATED
#1 POSITIVE
#2 NEGATIVE

#LIBSVM FORMAT
# label feature1:value feature2:value ... feature:n:value
#

def getCriteria(sentiment1, sentiment2):

	#First Tweet positive
	if sentiment1 > 5:
		#Second Tweet positive
		if sentiment2 > 5:
			return 1
		#Second Tweet negative
		elif sentiment2 < -5:
			return 2
		else:
			return 0
	#First Tweet negative
	elif sentiment1 < -5:
		#Second Tweet positive
		if sentiment2 > 5:
			return 2
		#Second Tweet negative
		elif sentiment2 < -5:
			return 1
		else:
			return  0
	return 0


#MAIN expects one params, the file where to save the output
if __name__ == '__main__' :

	#TO DO
	#Construct Vibe instance, param is language
	vibe = Vibe("en")

	criteria = 0
	sentiment1 = 0
	sentiment2 = 0


	fftxt = open("tweets.txt", 'w+')
	ffcsv = open("tweets.csv", 'w+')

	for i in xrange(int(sys.argv[1])):
		sentiment1 = random.randint(-50, 50)
		sentiment2 = random.randint(-50, 50)
		criteria = getCriteria(sentiment1, sentiment2)

		#LIBSVM
		fftxt.write(str(criteria) + " 1:" + str(sentiment1+50) + " 2:" + str(sentiment2+50) + "\n")
		#CSV
		ffcsv.write(str(criteria)+","+str(sentiment1+50)+","+str(sentiment2+50)+"\n")
