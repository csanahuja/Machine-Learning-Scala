'''
Artificial Intelligence Research Group
University of Lleida
'''

# Libraries

import xml.etree.ElementTree as ET
import re
import math
import sys

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
      filename1 = "Vibe/pos-adjectives.txt"
      filename2 = "Vibe/neg-adjectives.txt"
      afinn_filename = "Vibe/AFINN-111.txt"
    elif language == 'es' :
      filename1 = "Vibe/pos-adjectives-es.txt"
      filename2 = "Vibe/neg-adjectives-es.txt"
      afinn_filename = "Vibe/AFINN-111-es.txt"
    else :
      filename1 = "Vibe/empty.txt"
      filename2 = "Vibe/empty.txt"
      afinn_filename = "Vibe/empty.txt"

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

class Output():
    '''
    Output in the specified format
    '''

    def __init__(self, args):
        '''
        format: output format
        '''
        self.format = args.output_format

    def vibeAnalysis( self, a, language ) :
        # Emoticons normalization
        is_positive_emoticons = [ x in a.text for x in Vibe.positive_emoticons ]
        is_negative_emoticons = [ x in a.text for x in Vibe.negative_emoticons ]
        if any( is_positive_emoticons ) and any( is_negative_emoticons ) :
          a.set('emoticon', 'neutral' )
          a.set('emoticon-positive-counter', str( is_positive_emoticons.count( True ) ) )
          for x in ( e for i,e in enumerate( Vibe.positive_emoticons ) if is_positive_emoticons[i] ) :
              a.text = a.text.replace( x, Vibe.positive_normalized )
          a.set('emoticon-negative-counter', str( is_negative_emoticons.count( True ) ) )
          for x in ( e for i,e in enumerate( Vibe.negative_emoticons ) if is_negative_emoticons[i] ) :
              a.text = a.text.replace( x, Vibe.negative_normalized )
        elif any( is_positive_emoticons ) :
          a.set('emoticon', 'positive' )
          a.set('emoticon-positive-counter', str( is_positive_emoticons.count( True ) ) )
          for x in ( e for i,e in enumerate( Vibe.positive_emoticons ) if is_positive_emoticons[i] ) :
              a.text = a.text.replace( x, Vibe.positive_normalized )
        elif any( is_negative_emoticons ) :
          a.set('emoticon', 'negative' )
          a.set('emoticon-negative-counter', str( is_negative_emoticons.count( True ) ) )
          for x in ( e for i,e in enumerate( Vibe.negative_emoticons ) if is_negative_emoticons[i] ) :
              a.text = a.text.replace( x, Vibe.negative_normalized )
        else :
          a.set('emoticon', 'none' )

        # Letter multiplication normalization
        m = re.search( r'((\w)\2{2,})', a.text )
        if m :
            a.text = a.text.replace( m.group(1), m.group(2) * 2 )

        vibe = Vibe( language )
        a.set( 'sentiment', str( vibe.get_sentiment( a.text ) ) )

        return a

    def setMediaIncluded(self, tweetelement, xmlarg ):
        listofmediaURLs = list()
        if ('entities' in tweetelement):
           if ('media' in tweetelement['entities']):
             for media in tweetelement['entities']['media']:
                listofmediaURLs.append( media['expanded_url'] )
             xmlarg.set('nummedia', str(len(listofmediaURLs)) )
             xmlarg.set('medialist', str(listofmediaURLs) )
        return xmlarg

    def setURLsIncluded(self, tweetelement,  xmlarg ):
        listofURLs = list()
        if ('entities' in tweetelement):
           if ('urls' in tweetelement['entities']):
             for url in tweetelement['entities']['urls']:
               listofURLs.append( url['expanded_url'] )
             xmlarg.set('numurls', str(len(listofURLs)) )
             xmlarg.set('urllist', str(listofURLs) )
        return xmlarg

    def setHashTagsIncluded(self, tweetelement, xmlarg ):
        listofHTags = list()
        if ('entities' in tweetelement):
           if ('hashtags' in tweetelement['entities']):
             for htag in tweetelement['entities']['hashtags']:
               listofHTags.append( htag['text'] )
             xmlarg.set('numhashs', str(len(listofHTags)) )
             xmlarg.set('hashlist', str(listofHTags) )
        return xmlarg

    def get_weight(self, arg, args, scale = True):
        '''Gets the weight of argument arg depending on the source args.weight_source'''
        weight_source = args.weight_source
        input_source = args.input_source
        if input_source in ['twitter'] and weight_source in ['followers_count']:
            w = arg['user'][weight_source]
        else:
            if weight_source == 'favret_count' and input_source in ['twitter']:
                w = arg['favorite_count'] + arg['retweet_count']
            elif weight_source == 'fo1fa40re20' and input_source in ['twitter']:
                # https://www.deepadvantage.com/blog/retweets-beget-retweets/
                # http://dl.acm.org/citation.cfm?id=2700060
                w = arg['user']['followers_count'] + arg['favorite_count'] * 40 + arg['retweet_count'] * 20
            elif weight_source in arg:
                if input_source in ['twitter']:
                    w = arg[weight_source]
                else:
                    w = int(arg[weight_source])
            else:
                sys.exit('GetWeightError: key "%s" not in input source "%s"' % (weight_source, input_source))
        if scale:
            return self.scale_weight(w, args)
        else:
            return w

    def scale_weight(self, weight, args):
        '''Scales the weight using a log function'''
        if weight > 1:
            return int(math.floor(math.log(weight, args.log_base)) + 1)
        else:
            return 1


    def arg_list_pairs_to_xml(self, arg_list, arg_pairs, args):
        '''Translates the argument list and argument pairs to XML'''
        if self.format == 'xml':
            xml = ET.Element('entailment-corpus')
            al_xml = ET.SubElement(xml, 'argument-list')
            minw = maxw = self.get_weight(arg_list[0], args)
            if 'propagated_weight' in arg_list[0]:
                minpw = maxpw = arg_list[0]['propagated_weight']
            for al in arg_list:
                a = ET.SubElement(al_xml, 'arg')
                a.set('id', str(al['id']))
                a.set('author', al['user']['screen_name'])
                a.set('topic', args.collection_name)
                a.set('lang', al['lang'])
                w = self.get_weight(al, args)
                a.set('weight', str(w))
                if w < minw:
                    minw = w
                elif w > maxw:
                    maxw = w
                if 'propagated_weight' in al:
                    wp = al['propagated_weight']
                    a.set('propagated_weight', str(wp))
                    if wp < minpw:
                        minpw = wp
                    elif wp > maxpw:
                        maxpw = wp
                a.text = al['text']
                a = self.vibeAnalysis( a, al['lang'] )
                a = self.setMediaIncluded( al, a )
                a = self.setURLsIncluded( al, a )
                a = self.setHashTagsIncluded( al, a )
                if (args.showtweetidsurls == True):
                    print "https://twitter.com/"+al['user']['screen_name']+"/status/"+str(al['id'])
            ap_xml = ET.SubElement(xml, 'argument-pairs')
            for i, ap in enumerate(arg_pairs):
                p = ET.SubElement(ap_xml, 'pair')
                p.set('id', str(i))
                p.set('topic', args.collection_name)
                p.set('entailment', ap[2])
                if len(ap[3]):
                    p.set('entailmentprobvector', '%f %f %f' % (ap[3][0], ap[3][1], ap[3][2])) # [3][0] = p_support, [3][1] = p_attack, [3][2] = p_none
                t = ET.SubElement(p, 't')
                t.set('id', str(ap[0]))
                h = ET.SubElement(p, 'h')
                h.set('id', str(ap[1]))
            al_xml.set('minweight', str(minw))
            al_xml.set('maxweight', str(maxw))
            if 'propagated_weight' in arg_list[0]:
                al_xml.set('minpropagatedweight', str(minpw))
                al_xml.set('maxpropagatedweight', str(maxpw))
            return ET.ElementTree(xml)
        else:
            sys.exit('FormatError: output format "%s" not valid' % self.format)
