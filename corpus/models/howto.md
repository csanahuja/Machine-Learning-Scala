# Models

## Model 1

```sh
python ./XMLdata2featurevectors.py fs-politics-200-nsw-50.txt es-generaltweets-withsentiment.xml 1 > es-generaltweets-withsentiment.xml.relvectors.txt
python convertSVMfile.py -f es-generaltweets-withsentiment.xml.relvectors.txt -e > es-generaltweets-withsentiment.xml.relvectors.clean.txt
svm-train -b 1 es-generaltweets-withsentiment.xml.relvectors.clean.txt
```

Test the model with the same training file:

```sh
svm-predict  -b 1  es-generaltweets-withsentiment.xml.relvectors.clean.txt  es-generaltweets-withsentiment.xml.relvectors.clean.txt.model  t.predict
```


