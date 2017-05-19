# -*- coding: utf-8 -*-
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

  def __init__(self, FeatureSetFile, compcosine, vector_file, pairs_file ):
      f = open(  FeatureSetFile, "r" )
      self.attrDic = {}
      self.wordsDic = {}
      self.compcosine = compcosine
      self.vector_file = vector_file
      self.pairs_file = pairs_file
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

  def writeFeatureVector( self, argel, vector_file):
      for attr in sorted( self.attrDic, key=self.attrDic.get, reverse=True ):
          atel = None
          atel = argel.get( attr )
          # print attr, " ",
          if (atel is not None):
             if (self.attrDic[attr] == 'count'):
               vector_file.write(str(abs(int(atel)))+" ")
             else:
               if (int(atel) > 0):
                 vector_file.write("1 ")
               else:
                 vector_file.write("0 ")
          else:
              vector_file.write("0 ")

      vector_file.write(" ")
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
          vector_file.write(str(self.workingwordDic[word])+" ")

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

  def writeFeatureVectorsPair( self, rel, telid, tel, helid, hel, vector_file, pairs_file ):
      # First, write the pair relation (-1) if it is not known
      vector = str(telid) + "  " + str(helid) + "   " + str(rel) + " "
      pairs = str(telid) + "  " + str(helid) + "\n"
      #vector_file.write(vector)
      vector_file.write(str(rel)+" ")
      pairs_file.write(pairs)
      if (self.compcosine):
        vector_file.write(str(self.computeCosineDistance( tel, hel )))
      self.writeFeatureVector( tel, vector_file )
      vector_file.write(" ")
      self.writeFeatureVector( hel, vector_file )
      vector_file.write("\n")

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
     with open(self.vector_file,'w') as vector_file, open(self.pairs_file,'w') as pairs_file:
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
               self.writeFeatureVectorsPair( rel, telid, argt, helid, argh, vector_file, pairs_file )


class ParserVector():

    def __init__(self, args):
        self.args = args

    def convertSVMfile(self):
        with open(self.args.vector_file,'r+') as vector_file, \
             open(self.args.attributes_file,"w") as attr_file:
            p=re.compile('^#')
            for line in vector_file:
                if not p.match(line):
                    a=line.rstrip('\n').split()
                    s=''
                    if (self.args.elabels == True):
                        rv = a[2:]
                    else:
                        rv = a
                    s+=str(rv[0])+' '

                    s+='1:'+str(rv[1])+' '
                    for i in range(2,len(rv)):
                        if  self.args.dense:
                            s+=str(i)+':'+str(rv[i])+' '
                        else:
                            if not int(rv[i])==0:
                                s+=str(i)+':'+str(rv[i])+' '
                    attr_file.write(s+"\n")

    def xml2Vector(self):
        FeatureSetFile  = self.args.dictionary
        XMLdataset = self.args.xml_file
        computecosine = bool(int(self.args.compute_cosine))
        FeatureVectorCreator = FeatureVectorExtractor( FeatureSetFile, computecosine, self.args.vector_file, self.args.pairs_file )
        #FeatureVectorCreator.writeFeatureHeaderLine()
        FeatureVectorCreator.GetFeatureVectors( XMLdataset )
        self.convertSVMfile()
