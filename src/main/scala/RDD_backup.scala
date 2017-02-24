// // MAIN
//
// //Load Data into a RDD, modify route to required
// val textRDD = sc.textFile("tweets.csv")
//
// //Parse the RDD of csv lines into an RDD of Tweet classes
// val tweets2RDD = textRDD.map(parseTweet2).cache()
//
// // DEFINE FEATURES ARRAY
// val mlprep = tweets2RDD.map(tweet => {
//         val sentiment = tweet.sentiment.toInt
//         val sentiment2 = tweet.sentiment2.toInt
//         val relationship = tweet.relationship.toInt
//         Array(relationship, sentiment, sentiment2)
//       })
//
// // CREATE LABELED POINTS
// val mldata = mlprep.map(x => LabeledPoint(x(0), Vectors.dense(x(1), x(2))))
//
//
// //SPLIT MLDATA INTO TRAINING AND TEST DATA
// val splits = mldata.randomSplit(Array(0.3, 0.7))
// val (trainingData, testData) = (splits(0), splits(1))
//
// // INFO ABOUT FEATURES
// var categoricalFeaturesInfo = Map[Int, Int]()
// categoricalFeaturesInfo += (0 -> 101) //sentiment
// categoricalFeaturesInfo += (0 -> 101) //sentiment2

// //val labelAndPreds = test.map { point =>
// val labelAndPreds = test.map { point =>
//   val prediction = model.predict(point.features)
//   (point.label, prediction)
//   }
//
// val goodPrediction =(labelAndPreds.filter{
//   case (label, prediction) => ( label == prediction)
//   })
// val wrongPrediction =(labelAndPreds.filter{
//   case (label, prediction) => ( label != prediction)
//   })
//
// val good = goodPrediction.count()
// val wrong = wrongPrediction.count()
// val ratio = good.toDouble/test.count()
//
// val results = "Result: Good Prediction : " + good.toString + "\n" +
//         "Result: Wrong Prediction: " + wrong.toString + "\n" +
//         "Result: Ratio: " + ratio.toString + "\n"
// print(results)
//
// // OUT MAIN
//
// case class Tweet2(relationship:  Int, sentiment:  Int, sentiment2: Int)
//
// def parseTweet2(str: String): Tweet2 = {
//   val line = str.split(",")
//   Tweet2(line(0).toInt, line(1).toInt, line(2).toInt)
// }
//
// def cPrint(str: String) {
//   val output = "\n" + str + "\n\n"
//   print(output)
// }
