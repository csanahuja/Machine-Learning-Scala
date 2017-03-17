# Creating and using corpus of labeled pairs of tweets

## 1. Creating a feature set for extracting feature vectors from tweets:

First, create  a base dictionary from a corpus of tweets with command:

```sh
>  python mongo2corpus.py twittercorpusDB twittercorpusCollection 200 > dict-politics-200.txt
```

This creates a dictionary with the first 200 most frequent words in the collection of tweets
twittercorpusCollection in the mongodb  twittercorpusDB

Sample output:

```sh
URL  :  3598
de  :  3395
la  :  2660
el  :  2560
en  :  1543
@USERNAME  :  1402
a  :  1291
los  :  973
y  :  833
```

Then, incorporate additional attributes defined in a external file,
with attribute name and type (count or check) like attrlist1.txt :

sentiment : count
emoticont : count
numurls : count
nummedia : count
numurls : count

with the command:

```sh
> python mongo2featureset.py dict-politics-200.txt es fs-politics-200.txt 200  twittercorpus es_politics count attrlist1.txt
```

where:

- dict-politics-200.txt dictionary of words extracted with  mongo2corpus.py
- lang  : language of tweets to process (es)
- fs-politics-200.txt : feature set file (result)
- 200 maximum number of words to use from  dict-politics-200.txt
- twittercorpusDB twittercorpusCollection to use to create base dictionary if basedictionary is equal to "-"
- count :  "count" if value for the attribute is to count number of occurences of attribute
- attrlist1.txt : attribute file with names of non-word attributes


Sample output:

```python
{"type": "attr", "attr": "numhashs", "val": "count"}
{"type": "attr", "attr": "nummedia", "val": "count"}
{"type": "attr", "attr": "emoticont", "val": "count"}
{"type": "attr", "attr": "sentiment", "val": "count"}
{"type": "attr", "attr": "numurls", "val": "count"}
{"word": "URL", "type": "word", "order": 3598, "val": "count"}
{"word": "de", "type": "word", "order": 3395, "val": "count"}
{"word": "la", "type": "word", "order": 2660, "val": "count"}
{"word": "el", "type": "word", "order": 2560, "val": "count"}
{"word": "en", "type": "word", "order": 1543, "val": "count"}
{"word": "@USERNAME", "type": "word", "order": 1402, "val": "count"}
```


## 2. Get the feature vectors from a conversation XML file (or from a corpus of conversations XML file)

And then, given a conversation graph stored in a XML file with our format (or a corpus
of conversations stored in a single XML file), get its feature vectors with the command:

```sh
> python XMLdata2featurevectors.py models/fs-politics-200-nsw-50.txt conversation.xml 0|1
```

where:
1. the first parameter is the file with the feature set description (obtained with mongo2featureset.py)
2. the second parameter is the conversation file in our XML format
3. The last parameter indicates whether we want to include the cosine distance between the tweets
as a computed feature in the feature vector (it will be the second value in each vector).

Sample output per each tweet in the XML:

IDTW1 IDTW2  RELbetweemPairOfTweets  Attribute values for tweet 1  Attribute values for tweet 2

where:
1. the first two columns are the IDs of the tweets of the answer pair (TW1 answers TW2)

2. RELbetweem pair of tweets  is:
```python
  def decodeEntailRel( self, entailRel ):
     if ( entailRel == "" ):
        return -1
     if ( entailRel == "NONE" ):
        return 0
     if ( entailRel == "ATTACKS" ):
        return 1
     return 2  (if Rel == "SUPPORTS")
```

3. The rest of columns are the columns described in the feature set file provided to the
program, first for the first tweet and then the same columns but for the second tweet.

But the first line of resulting file is a comment line, that starts with #, with the name of each
attribute value in each output vector


For example, for the sample file PardoYRato-logweights.pretty.xml. You can get the resulting file
in PardoYRato-logweights.pretty.xml.relvectors  (when using the feature set fs-politics-200.txt) with
this sintaxis:

```sh
 python XMLdata2featurevectors.py   models/fs-politics-200-nsw-50.txt   PardoYRato-logweights.pretty.xml  1 > PardoYRato-logweights.pretty.xml.relvectors
```

## 3. Learning a labelling model from a corpus of conversations

Executing svmlib to learn a labelling model for pairs of tweets:

First, we have to get a "clean" version of the file with the vector set, excluding the columns with the IDs of the tweets of each pair:

```sh
  python convertSVMfile.py -f file.relvectors  -e  > file.clean.relvectors
```
Then, with the clean file we can already train a model with probabilities for each label with:

```sh
  svm-train  -b 1 file.clean.relvectors
```

or without probability values:

```sh
  svm-train  file.clean.relvectors
```


This generates a SVM model that it is stored in the file    file.clean.relvectors.model


## 4. Predicting labels (and probability distributions over possible labels)

### without probability values

First, we have to filter out from the RELvectorformat file the two first columns,
that contain the tweet IDs of the two tweets of the pair, with the script:

```sh
  python convertSVMfile.py -f  file.relvectors  -e > file.cleanfile.relvectors
```

where file.relvectors is the RELvectorformat file obtained with XMLdata2featurevectors.py,
and cleanfile.relvectors is the same file but in the
sparse data format of svmlib (only non-cero features are stored), and without the
two first columns that are the tweet IDs of the pair.

```sh
 svm-predict  cleanfile.relvectors  labellingmodelwithprob.model  output
```


### if we want to generate probability values for labels:

First, we have to filter out from the RELvectorformat file the two first columns, that contain the tweet IDs of the two tweets of the pair, with the script:

```sh
  python convertSVMfile.py -f  file.relvectors  -e > cleanfile.relvectors
```

where file.relvectors is the RELvectorformat file obtained with XMLdata2featurevectors.py,
and cleanfile.relvectors is the same file but in the
sparse data format of svmlib (only non-cero features are stored), and without the
two first columns that are the tweet IDs of the pair.

Then, with the filtered RELvectorformat we can predict its labels and probability vectors with:

```sh
 svm-predict -b 1 cleanfile.relvectors  labellingmodelwithprob.model  output
```

The output format is a file where the first line indicates the ORDER of the labels that SVM uses in the following lines (but remember that the two first columns have been actually erased with the filter program convertSVMfile), and then each of the following lines gives, for a tweet pair,  first its predicted label, and next (if we have also asked for computing probability distributions), the probability that each possible label was the right answer (following the labels order explained in the first line).
So, for example, for a file with three tweet pairs the output could be something like:

> labels 2 1 0
> 0 0.0447161 0.226854 0.728429
> 2 0.49332 0.248142 0.258538
> 2 0.713506 0.24226 0.0442344

that indicates that:
for the first pair, the most probable label is 0 and its probability vector (in the
order given by the first line) is [p(2)=0.0447161,p(1)=0.226854,p(0)=0.728429]
and analogously for the other lines. The order of the tweet pairs follows the same order of the input file conversationinRELvectorformat


It seems that the order can change depending on the order in which SVMlib finds the
labels when he learns a mode, so the order found in the first line can change from
different executions of SVMlib and cannot be assumed to be any particular one
