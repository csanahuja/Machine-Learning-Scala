#!/usr/bin/python
from __future__ import print_function

import sys
import json
import pymongo


connection = pymongo.MongoClient()
db = connection.tweets_corpus


def insert_mongo(a):
    try:
    	print(a)
        collection.insert(a)
        print(".")
    except:
        print("*")

def check_insert(a):
    try:
    	found = collection.find_one({ "id" : a["id"] }) 
        if found is None:
             collection.insert(a)
             print (a)
    except:
        print(" NO ")

collection = db[sys.argv[2]]
f = open(sys.argv[1])
for line in f:
    line = line.strip()
    if line.startswith("{") and line.endswith("}"):
        lastline = line
        a = json.loads(line)        
        check_insert(a)


