#!/bin/bash
# Save dictionary of base words in dict-politics-200.txt
# Save feature set description file in fs-politics-200.txt
# Create XML with the full corpus in file $1  (argument #1)
# Create attribute vectors data set in $1.relvectors.txt
# 

python mongo2corpus.py tweetcorpus es-generaltweets $2 1 $3 spanish > dict-politics-$2-nsw-$3.txt
python mongo2featureset.py dict-politics-$2-nsw-$3.txt es fs-politics-$2-nsw-$3.txt $4  - - count attrlist1.txt
cd /home/socialarg/socialarg/personals/josep/data2xml
python  data2xml.py  -qt all --db tweetcorpus --collection es-generaltweets --classify --classify_collection  classify-es-generaltweets -ct  only_reply_to  -o ../../../corpus/$1
cd /home/socialarg/socialarg/corpus
python XMLdata2featurevectors.py fs-politics-$2-nsw-$3.txt $1 0  > $1.$2-nsw-$3-relvectorsNSA.txt
