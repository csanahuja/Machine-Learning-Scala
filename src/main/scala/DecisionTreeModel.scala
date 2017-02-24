import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import org.apache.spark._
import org.apache.spark.rdd.RDD
// Import classes for MLLib
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.util.MLUtils

// @Annotation
// Todo Rework to use Dataframe instead of RDD (soon deprecated)
object DecisionTreeModel {

	def main(args: Array[String]) {
		val conf = new SparkConf().setAppName("DTM")
		val sc = new SparkContext(conf)

		//Load Data into a RDD, modify route to required
		val textRDD = sc.textFile("tweets.csv")

		//Parse the RDD of csv lines into an RDD of Tweet classes
		val tweets2RDD = textRDD.map(parseTweet2).cache()

		// DEFINE FEATURES ARRAY
		val mlprep = tweets2RDD.map(tweet => {
					  val sentiment = tweet.sentiment.toInt
            val sentiment2 = tweet.sentiment2.toInt
            val relationship = tweet.relationship.toInt
					  Array(relationship, sentiment, sentiment2)
					})

		// CREATE LABELED POINTS
		val mldata = mlprep.map(x => LabeledPoint(x(0), Vectors.dense(x(1), x(2))))


		//SPLIT MLDATA INTO TRAINING AND TEST DATA
		val splits = mldata.randomSplit(Array(0.3, 0.7))
		val (trainingData, testData) = (splits(0), splits(1))

		// INFO ABOUT FEATURES
		var categoricalFeaturesInfo = Map[Int, Int]()
		categoricalFeaturesInfo += (0 -> 101) //sentiment
		categoricalFeaturesInfo += (0 -> 101) //sentiment2


		// VALUES OF MODEL
		val numClasses = 3
		// Defining values for the other parameters
		val impurity = "gini"
		val maxDepth = 8
		val maxBins = 200

		// Call DecisionTree trainClassifier with the trainingData , which returns the model
		val model = DecisionTree.trainClassifier(trainingData, numClasses,
			categoricalFeaturesInfo, impurity, maxDepth, maxBins)
		model.toDebugString

		//val labelAndPreds = testData.map { point =>
		val labelAndPreds = testData.map { point =>
			val prediction = model.predict(point.features)
			(point.label, prediction)
			}

		val goodPrediction =(labelAndPreds.filter{
			case (label, prediction) => ( label == prediction)
			})
		val wrongPrediction =(labelAndPreds.filter{
			case (label, prediction) => ( label != prediction)
			})

		val good = goodPrediction.count()
		val wrong = wrongPrediction.count()
		val ratio = good.toDouble/testData.count()

		val results = "Result: Good Prediction : " + good.toString + "\n" +
					  "Result: Wrong Prediction: " + wrong.toString + "\n" +
					  "Result: Ratio: " + ratio.toString + "\n"
		cPrint(results)

		sc.stop()
  	}

	case class Tweet2(relationship:  Int, sentiment:  Int, sentiment2: Int)

	def parseTweet2(str: String): Tweet2 = {
		val line = str.split(",")
		Tweet2(line(0).toInt, line(1).toInt, line(2).toInt)
	}

	def cPrint(str: String) {
		val output = "\n" + str + "\n\n"
		print(output)
	}

}
