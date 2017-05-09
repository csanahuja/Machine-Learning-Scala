#!/usr/bin/python -ttOO
# -*- coding: utf-8 -*-

import sys
import re


if __name__ == "__main__":

    f = open(sys.argv[1])
    p = re.compile('^#')
    for line in f:
        if not p.match(line):
            a=line.rstrip('\n').split()
            indexes = a[:2]
            print indexes[0] + " " + indexes[1]
    f.close()
