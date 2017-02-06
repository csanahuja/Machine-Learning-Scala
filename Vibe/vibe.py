'''
Artificial Intelligence Research Group
University of Lleida
'''

# Libraries

import xml.etree.ElementTree as ET
import re
import math

# Classes

class Vibe :
  '''
  Get vibes from a text
  '''
  # http://stackoverflow.com/questions/19149186/how-to-find-and-count-emoticons-in-a-string-using-python
  positive_emoticons = [':)', ':-)', ';-)', ':D', ':-D', u'\u1f601', u'\u1f602', u'\u1f603', u'\u1f604', u'\u1f605', u'\u1f606' ] # with Emoji Unicode
  negative_emoticons = [':(', ':-(', ':(', u'\u1f61e']
  positive_normalized = ':)'
  negative_normalized = ':('

  def __init__( self, language ) :
    if language == 'en' :
      filename1 = "pos-adjectives.txt"
      filename2 = "neg-adjectives.txt"
      afinn_filename = "AFINN-111.txt"
    elif language == 'es' :
      filename1 = "pos-adjectives-es.txt"
      filename2 = "neg-adjectives-es.txt"
      afinn_filename = "AFINN-111-es.txt"
    else :
      filename1 = "empty.txt"
      filename2 = "empty.txt"
      afinn_filename = "empty.txt"

    self.positive_words = []
    for line in open( filename1 ) :
        word, weight = line.split()
        self.positive_words.append( word )

    self.negative_words = []
    for line in open( filename2 ) :
        word, weight = line.split()
        self.negative_words.append( word )

    self.afinn = {}
    for line in open( afinn_filename ) :
       words = line.split()
       if len( words ) != 2 : continue
       k,v = words[0], words[1]
       self.afinn[ k ] = int( v )

    #self.afinn = dict(map(lambda (k,v): (k,int(v)), [ line.split('\t') for line in open(afinn_filename) ]))

  def get_sentiment( self, sentence ) :
    sentiment = 0
    for word in sentence.split() :
      word = word.lower()
      if word in self.positive_words or word in self.positive_emoticons :
        sentiment = sentiment + 1
      if word in self.negative_words or word in self.negative_emoticons :
        sentiment = sentiment - 1

    sentiment += sum(map(lambda word: self.afinn.get(word, 0), sentence.lower().split()))

    return sentiment
