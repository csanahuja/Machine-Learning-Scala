"""
    Corpus utils functions to transform word
    and extract and filter subwords




"""
"""  https://docs.python.org/2/library/re.html  """

import re

def transform_word(w):

   wf = []
   cut = 1
   while (cut == 1):
    cut = 0
    if ( w.startswith(".") or w.startswith(",")  or w.startswith("\"")  or w.startswith("(") or 
         w.startswith(":") or  w.startswith(u"\u201C") or w.startswith(u"\u2018") or
         w.startswith(u"\u0027") or w.startswith("#") ):
      if (len(w) > 1):    
        w = w[1:]
        cut = 1
      else:
        return wf
    if ( w.endswith(".") or w.endswith(",") or w.endswith("\"") or w.endswith(")") or
        w.endswith(":") or  w.endswith(u"\u201D") or w.endswith(u"\u2019") or
        w.endswith(u"\u0027") ):
      if (len(w) > 1):
        w = w[:len(w)-1]
        cut = 1
      else:
        return wf
      
   if any( x == w for x in  [':)', ':-)', ';-)', ':D', ':-D'] ) :
      wf = ':)'
   else:
      if any( x == w for x in  [':(', ':-(', ': ('] ) : 
         wf = ':('
      else:
         if ( w.startswith("http://") or w.startswith("HTTP://") or 
               w.startswith("https://") or w.startswith("HTTPS://") ):
           wf = 'URL'
         else:
           # rex = RegexObject( )
           if (  re.search( '@\w+', w ) ):
             wf = '@USERNAME'
           else:
              if (len(w) > 2):
                ll = list()   
                ll.append( w[0] )
                ll.append( w[1] )
                j = 2
                for i in range(2,len(w),1):
                  if (w[i] != w[i-1] or w[i] != w[i-2]):
                    ll.append( w[i] )
                wf = ''.join(ll)
              else:
                wf = w
              wf = wf.lower()

   # try:
     # print w, " changed with ", wf
   # except:
   #  pass
   return wf


def extractsubwords(wf):
      subwords = list()
      result =  re.match( ur'([\u00bf]*)([\u00a1]*)([\u00bf]*)([^\?\!]*)([\?]*)([\!]*)([\?]*)', wf )  
      if (result.group(1) != '' or result.group(3) != ''):
         subwords.append( u'\u00bf')
      if (result.group(2) != '' ):
         subwords.append( u'\u00a1')
      if (result.group(5) != '' or result.group(7) != ''):
         subwords.append( u'?')
      if (result.group(6) != '' ):
         subwords.append( u'!')
      if (result.group(4) != '' ):
         subwords.append(result.group(4))      

      return subwords


