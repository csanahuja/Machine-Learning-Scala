#!/usr/bin/python -ttOO
# -*- coding: utf-8 -*-

import sys
import re
import pymongo
import nltk
import json

import corpusutils.corpusutils as CUtils



"""  https://docs.python.org/2/library/re.html  """


if __name__ == "__main__":
     """
       Program for creating feature sets from dictionaries,
       adding to them any other features not present on the
       base dictionaries

     """

     tweetscollection = sys.argv[1]
     lang = sys.argv[2]
     limitsize = 200
     excludestopwords = True

     tweets = open(sys.argv[1],'r+')
     tweets_list = []
     for line in open(sys.argv[1],'r+'):
         tweets_list.append(json.loads(line))


     dic = {}
     for tweet in tweets_list:
	   for w in re.split( '[\s,]+' , tweet['text'] ):
                wf = CUtils.transform_word(w)
                # print wf
                if (len(wf) > 0):
                  subwords = CUtils.extractsubwords(wf)
                else:
                  subwords = list()
                # print len(subwords)
                for sb in subwords:
                  # lsb = sb.lower()
                  # print sb
                  if (len(sb) > 0):
                    if (excludestopwords):
		  	dic[sb] = dic.get( sb, 0 ) + 1
                    else:
		      dic[sb] = dic.get( sb, 0 ) + 1

     numwords = 0
     # print len(dic)
     for w in sorted( dic, key=dic.get, reverse=True ):
        if (numwords < limitsize):
          print w.encode('utf8'), ' : ', dic[w]
          numwords += 1
        else:
          break
