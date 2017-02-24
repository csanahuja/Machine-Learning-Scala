import org.apache.spark.{SparkConf, SparkContext}

import org.apache.spark.rdd.RDD
import org.apache.spark.rdd.MapPartitionsRDD
import org.apache.commons.io.FileUtils
import java.io.File

// Import classes for MLLib
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.util.MLUtils

// DecisionTreeModelCustom
object DTMC {

	def main(args: Array[String]) {
		val conf = new SparkConf().setAppName("DTMC")
		val sc = new SparkContext(conf)


		if (args.size < 1){
      println("Usage -> arguments: input_file <save_model_file> <model parameters>")
      System.exit(1)
    }

		// Load the data stored in LIBSVM format as a DataFrame.
	  val data = MLUtils.loadLibSVMFile(sc, args(0))
		//val data = spark.read.format("libsvm").load(args(0))

		// Split the data into train and test
    val splits = data.randomSplit(Array(0.75, 0.25))
		val (train, test) = (splits(0), splits(1))

		// VALUES OF MODEL
		val numClasses = 3
		val categoricalFeaturesInfo = Map[Int, Int]()
		val impurity = "gini"
		val maxDepth = 8
		val maxBins = 200

		// Call DecisionTree trainClassifier with the train data , which returns the model
		val model = DecisionTree.trainClassifier(train, numClasses,
			categoricalFeaturesInfo, impurity, maxDepth, maxBins)
		val accuracy = getAccuracy(model, test)
		println("Results = " + accuracy)

		// Save model
		// we delete the file where we are going to save to avoid conflicts
		FileUtils.deleteQuietly(new File("target/tmp/DTM"))
		model.save(sc,path="target/tmp/DTM")

		sc.stop()
  	}

    // Auxiliar Function: returns accuracy of a given model with a data set
		def getAccuracy(model: DecisionTreeModel,
											test: RDD[LabeledPoint]): Double = {
			//compute accuracy on the test set
			val labelAndPreds = test.map { point =>
						val prediction = model.predict(point.features)
						(point.label, prediction)
					}
			val accuracy = labelAndPreds.filter(r => r._1 == r._2).count().toDouble / test.count()
			return accuracy
		}


}
