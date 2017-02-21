#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys


if __name__ == '__main__' :

    fo = open(sys.argv[2], 'w+')
    for line in open(sys.argv[1], 'r+'):
        splits = line.split()
        new_line = ''
        i = 0
        for split in splits:
            if i == 1:
                new_line = split
            if i > 1:
                new_line = new_line + " " + split
            i += 1
        new_line += "\n"
        fo.write(new_line)
