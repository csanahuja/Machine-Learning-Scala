"""

  Getting feature vectors from an XML file using
  a feature set

"""

import sys
import re

import xml.etree.ElementTree as ET
import json
import corpusutils.corpusutils as CUtils
import cosinesim
from collections import Counter


class FeatureVectorExtractor:
  """
    Object class extractor

  """

  def __init__(self, FeatureSetFile, compcosine ):
      f = open(  FeatureSetFile, "r" )
      self.attrDic = {}
      self.wordsDic = {}
      self.compcosine = compcosine
      for line in f:
        line = line.strip()
        jobj = json.loads( line )
        # qprint jobj
        if ( jobj['type'] == 'attr' ):
           self.attrDic[ jobj['attr'] ] = jobj['val']
        if ( jobj['type'] == 'word' ):
           self.wordsDic[ jobj['word'] ] = [jobj['val'],jobj['order']]
      f.close()
      self.workingwordDic = self.wordsDic.copy()
      # print self.attrDic
      # print self.wordsDic.keys()
      # print self.wordsDic.keys().sort()

  def writeFeatureVector( self, argel ):
      for attr in sorted( self.attrDic, key=self.attrDic.get, reverse=True ):
          atel = None
          atel = argel.get( attr )
          # print attr, " ",
          if (atel is not None):
             if (self.attrDic[attr] == 'count'):
               print atel,
             else:
               if (int(atel) > 0):
                 print "1",
               else:
                 print "0",
          else:
             print "0",

      print "",
      for word in self.wordsDic.keys():
          self.workingwordDic[word] = 0
      for word in re.split(  '[\s,]+' , argel.text ):
          wf = CUtils.transform_word(word)
          # print wf
          if (len(wf) > 0):
              subwords = CUtils.extractsubwords(wf)
          else:
              subwords = list()
          for sb in subwords:
              if (len(sb) > 0):
                 if (sb in self.wordsDic.keys()):
                    if (self.wordsDic[sb][0] == 'count'):
                      self.workingwordDic[sb] += 1
                    else:
                      self.workingwordDic[sb] = 1

      for word in sorted(  self.wordsDic, key=self.wordsDic.get, reverse=True ):
            print  self.workingwordDic[word],

  def getvectorfromtext( self, argel ):
      allsubwords = list()
      for word in re.split(  '[\s,]+' , argel.text ):
          wf = CUtils.transform_word(word)
          # print wf
          if (len(wf) > 0):
              allsubwords  +=  CUtils.extractsubwords(wf)

      return Counter(allsubwords)

  def computeCosineDistance( self, tel, hel ):

      vector1 = self.getvectorfromtext( tel )
      vector2 = self.getvectorfromtext( hel )
      distance = cosinesim.get_cosine(vector1, vector2)
      return distance

  def writeFeatureVectorsPair( self, rel, telid, tel, helid, hel ):
      # First, write the pair relation (-1) if it is not known
      print telid, "  ", helid, "   ", rel, "",
      if (self.compcosine):
        print self.computeCosineDistance( tel, hel ),
      self.writeFeatureVector( tel )
      print "",
      self.writeFeatureVector( hel )
      print ""

  def writeFeatureHeaderLine(self):
      print '# ansTweetID  scrTweetID   Rel    '.encode('utf-8'),
      if (self.compcosine):
        print '# CosineDistance(t,h)     '.encode('utf-8'),
      for attr in sorted( self.attrDic, key=self.attrDic.get, reverse=True ):
      # for attr in self.attrDic.keys().sort():
          print attr.encode('utf-8')+'-'+self.attrDic[attr].encode('utf-8')+' ',
      for word in sorted(  self.wordsDic, key=self.wordsDic.get, reverse=True ):
      # for word in self.wordsDic.keys().sort():
          # print unicode( word )
          print word.encode('utf-8')+'-'+str(self.wordsDic[word][1]).encode('utf-8')+'-'+self.wordsDic[word][0].encode('utf-8')+' ',
      # print "\n"
      print "".encode('utf-8')

  def decodeEntailRel( self, entailRel ):
     if ( entailRel == "" ):
        return -1
     if ( entailRel == "NONE" ):
        return 0
     if ( entailRel == "ATTACKS" ):
        return 1
     return 2

  def GetFeatureVectors(self, XMLArgumentPairsList ):
     tree = ET.parse( XMLArgumentPairsList )
     root = tree.getroot()
     argroot = root.find( 'argument-list' )
     pairsroot = root.find( 'argument-pairs' )
     for pair in pairsroot:
           # find first arg of pair (t)
           tele = pair.find( 't' )
           telid = tele.get( 'id' )
           argt = argroot.find("./arg[@id='{id}']".format(id=str(telid)) )
           # find second arg of pair (h)
           hele = pair.find( 'h' )
           helid = hele.get( 'id' )
           argh = argroot.find("./arg[@id='{id}']".format(id=str(helid)) )
           entailrel = pair.get( 'entailment' )
           if (entailrel is None):
             rel = "-1"
           else:
             rel = self.decodeEntailRel( entailrel )
           self.writeFeatureVectorsPair( rel, telid, argt, helid, argh )



if ( __name__ == "__main__" ):
    FeatureSetFile  = sys.argv[1]
    XMLdataset = sys.argv[2]
    computecosine = bool(int(sys.argv[3]))
    FeatureVectorCreator = FeatureVectorExtractor( FeatureSetFile, computecosine )
    FeatureVectorCreator.writeFeatureHeaderLine()
    FeatureVectorCreator.GetFeatureVectors( XMLdataset )
