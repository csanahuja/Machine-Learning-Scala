#!/usr/bin/python
#import random
import sys
import re
from optparse import OptionParser


parser = OptionParser()
parser.add_option("-f", "--file", dest="file",
                  help="File to open", default='a')
parser.add_option("-d", "--dense", dest="dense",
                  help="Dense format", action='store_true', default=False)
parser.add_option("-n", "--nlabels", dest="elabels",
                  help="Filter out tweet labels", action='store_false', default=True)
parser.add_option("-e", "--elabels", dest="elabels",
                  help="Filter out tweet labels", action='store_true' )



(options, args) = parser.parse_args()

f=open(options.file)
p=re.compile('^#')
for line in f:
    if not p.match(line):
        a=line.rstrip('\n').split()
        s=''
        if (options.elabels == True):
            rv = a[2:]
        else:
            rv = a
        s+=str(rv[0])+' '

        s+='1:'+str(rv[1])+' '
        for i in range(2,len(rv)):
            if  options.dense:
                s+=str(i)+':'+str(rv[i])+' '
            else:
                if not int(rv[i])==0:
                    s+=str(i)+':'+str(rv[i])+' '
        print s
f.close()
