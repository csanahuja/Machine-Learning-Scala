#!/usr/bin/python -ttOO
# -*- coding: utf-8 -*-

import nltk
import sys
import json
import pymongo

"""
   Format of a FeatureSet: file of lines, with one
    json object per line, and with format:
   { "type": "attr",
     "attr": attribute name to look for inside XML element,
     "val": "count" (# of occurrences) or "check" (+1/0)  }
     OR
   { "type": "world",
      "attr" : word,
      "val":  "count" (# of occurrences) or "check" (+1/0)  }


"""


import corpusutils.corpusutils as CUtils


class FeatureSet:

   def __init__(self, basedictionary, lang,  limitsizebasedict, dbtweets, collect,  wordcount,  extrattrfile ):
       # self.basedictionary = basedictionary
       self.limitsizebasedict = limitsizebasedict
       self.lang = lang
       self.wordcount = wordcount
       self.basedic = None
       self.fsdic = None
       if (basedictionary == '-'):
          self.createBaseDictionaryFromtweetcollection( dbtweets, collect )
       else:
          self.LoadBaseDictionaryFromFile( basedictionary )
       self.LoadAttrSet( extrattrfile )



   def createBaseDictionaryFromtweetcollection( dbase, collect ):
      connection = pymongo.MongoClient('%s://%s:%i/' % ('mongodb', 'localhost', 27017))
      db = connection[dbase]
      collection = db[collect]

      # nwords = 0
      self.basedic = {}
      for post in collection.find():
	 for w in post['text'].split():
                wf = CUtils.transform_word(w)
                # print wf
                if (len(wf) > 0):
                  subwords = CUtils.extractsubwords(wf)
                else:
                  subwords = list()
                # print len(subwords)
                for sb in subwords:
                  # print sb
                  if (len(sb) > 0):
    		    if sb not in self.basedic:
			self.basedic[sb] = 1
                    else:
                        self.basedic[sb] += 1
      nwords = 0
      self.wordlist = list()
      for w in sorted( self.basedic, key=self.basedic.get, reverse=True ):
         if ( nwords < self.limitsizebasedict ):
            self.wordlist.append( w )
            nwords += 1
         else:
            break



#
#    Load first limitsizebasedict words from base dictionary of words,
#    that should be sorted by decreasing freq of occurrence
#
   def LoadBaseDictionaryFromFile( self, basedictionary ):
       df =  open( basedictionary, "r" )
       self.basedic = {}
       self.wordlist = list()
       nwords = 0
       for line in df:
         if (nwords < self.limitsizebasedict):
           toks = line.split()
           w = toks[0]
	   self.basedic[w] = int(toks[2])
           self.wordlist.append( w )
           nwords += 1
         else:
           break
       df.close()

   def LoadAttrSet( self, extrattrfile ):
       self.fsdic = {}
       attrfile =  open( extrattrfile, "r" )
       for line in attrfile:
          toks = line.split()
          self.fsdic[toks[0]] = toks[2]
       attrfile.close()

   def createFeatureSetFile( self, featuresetfile ):
       f = open( featuresetfile, "w" )
       for at in  self.fsdic:
          jobj = { 'type': 'attr', 'attr': at, 'val': self.fsdic[at] }
          f.write( json.dumps( jobj )+'\n' )
       for w in self.wordlist:
          jobj = { 'type': "word", 'word': w, 'val': self.wordcount, 'order' : self.basedic[w] }
          f.write( json.dumps( jobj )+'\n' )
       f.close()




if __name__ == "__main__":
     """
       Program for creating feature sets from dictionaries,
       adding to them any other features not present on the
       base dictionaries

     """

     basedictionary = sys.argv[1]
     lang = sys.argv[2]
     featuresetfile = sys.argv[3]
     limitsizebasedict = int(sys.argv[4])
     wordcount = sys.argv[5]
     extrattrfile = sys.argv[6]
     fset = FeatureSet( basedictionary, lang, limitsizebasedict,
                        None, None, wordcount, extrattrfile )
     fset.createFeatureSetFile( featuresetfile )
