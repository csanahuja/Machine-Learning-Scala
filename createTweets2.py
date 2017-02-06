#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys
import random
from Vibe.vibe import Vibe

#CRITERIA
#0 UNDERTERMINATED
#1 POSITIVE
#2 NEGATIVE
if __name__ == '__main__' :

	vibe = Vibe("en")

	Tweet2 = ""
	sentiment1 = 0
	sentiment2 = 0
	criteria = 0

	ff = open(sys.argv[1], 'w+')
	for i in xrange(1000):
		sentiment1 = random.randint(-50, 50)
		sentiment2 = random.randint(-50, 50)

		#First Tweet positive
		if sentiment1 > 5:
			#Second Tweet positive
			if sentiment2 > 5:
				criteria = 1
			#Second Tweet negative
			elif sentiment2 < -5:
				criteria = 2
			else:
				criteria = 0
		#First Tweet negative
		elif sentiment1 < -5:
			#Second Tweet positive
			if sentiment2 > 5:
				criteria = 2
			#Second Tweet negative
			elif sentiment2 < -5:
				criteria = 1
			else:
				criteria = 0
		else:
			criteria = 0

		ff.write(str(i)+","+str(sentiment1+50)+","+str(i+1)+","+str(sentiment2+50)+
				","+str(criteria)+"\n")
