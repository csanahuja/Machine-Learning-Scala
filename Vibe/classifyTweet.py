#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys
import random
from vibe import Vibe

if __name__ == '__main__' :

	vibe = Vibe("en")
	sentiment1 = vibe.get_sentiment("Good Sample")
	sentiment2 = vibe.get_sentiment("Bad Sample")
	print sentiment1, sentiment2
