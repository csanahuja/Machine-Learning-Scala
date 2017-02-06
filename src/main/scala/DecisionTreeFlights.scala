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

object DecisionTreeFlights {

	def main(args: Array[String]) {
		val conf = new SparkConf().setAppName("DecisionTreeFlights")
		val sc = new SparkContext(conf)


		//Load Data into a RDD, modify route to required
		val textRDD = sc.textFile("flights.csv")

		//Parse the RDD of csv lines into an RDD of flight classes
		val flightsRDD = textRDD.map(parseFlight).cache()
		cPrint(flightsRDD.first().toString)

		// EXTRACT FEATURES: map string city to int
		var cityMap: Map[String, Int] = Map()
		var index: Int = 0
		flightsRDD.map(flight => flight.src).distinct.collect.foreach(
					   x => { cityMap += (x -> index); index += 1 })
		cPrint(cityMap.toString)

		// DEFINE FEATURES ARRAY
		val mlprep = flightsRDD.map(flight => {
					  val src = cityMap(flight.src) // category
					  val dest = cityMap(flight.dest) // category
					  val id = flight.id.toInt
					  val price = flight.price.toInt
					  val expensive = if (flight.price.toInt > 500) 1 else 0
					  Array(expensive.toInt, src.toInt, dest.toInt, id.toInt)
					})
		println(mlprep.take(5).deep.mkString("\n"))

		// CREATE LABELED POINTS
		val mldata = mlprep.map(x => LabeledPoint(x(0), Vectors.dense(x(1), x(2), x(3))))
		println(mldata.take(5).deep.mkString("\n"))

		// SPLIT DATA: TRAINING SET - TEST SET
		// Flights with price < 500 -> get 50%
		val mldata0 = mldata.filter(x => x.label == 0.0).randomSplit(Array(0.50, 0.50))(1)
		// Flights with price > 500 -> get 50%
		val mldata1 = mldata.filter(x => x.label == 1.0).randomSplit(Array(0.50, 0.50))(1)
		// FLights selected
		//val mldata2 = mldata0 ++ mldata1
		val mldata2 = mldata

		//Split mldata2 into training and test data
		val splits = mldata2.randomSplit(Array(0.3, 0.7))
		val (trainingData, testData) = (splits(0), splits(1))

		// INFO ABOUT FEATURES
		var categoricalFeaturesInfo = Map[Int, Int]()
		categoricalFeaturesInfo += (0 -> cityMap.size) //src
		categoricalFeaturesInfo += (1 -> cityMap.size) //dest


		// VALUES OF MODEL
		val numClasses = 2
		// Defining values for the other parameters
		val impurity = "gini"
		val maxDepth = 10
		val maxBins = 5000

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
		println(labelAndPreds.take(10000).deep.mkString("\n"))

		val good = goodPrediction.count()
		val wrong = wrongPrediction.count()
		val ratioWrong=wrong.toDouble/testData.count()

		val results = "Good Prediction : " + good.toString + "\n" +
					  "Wrong Prediction: " + wrong.toString + "\n" +
					  "Ratio Wrong     : " + ratioWrong.toString + "\n"
		cPrint(results)

		sc.stop()
  	}

	case class Flight(src: String, dest: String, id: Int, price: Int)

	def parseFlight(str: String): Flight = {
		val line = str.split(",")
		Flight(line(0), line(1), line(2).toInt, line(3).toInt)
	}

	def cPrint(str: String) {
		val output = "\n" + str + "\n\n"
		print(output)
	}
}
