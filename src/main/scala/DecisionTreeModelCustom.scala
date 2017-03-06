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

	class Params(var input : String, var output : String, var maxDepth : Int,
               var maxBins: Int) {
    def this() {
        this("", "", 8, 200);
    }
		override def toString : String =
		      "Params: \n" +
		      " - Input =    " + input + "\n" +
		      " - Output =   " + output + "\n" +
		      " - MaxDepth = " + maxDepth + "\n" +
		      " - MaxBins =  " + maxBins + "\n"
		  }

	def main(args: Array[String]) {
		val conf = new SparkConf().setAppName("Decision Tree Model")
		val sc = new SparkContext(conf)

		//Parse and Save Params
		val arglist = args.toList
		val params = new Params()
		getOptions(arglist, params)

		if(params.input == ""){
			val msg = """Usage -> arguments: -arg value
									argument: -input required""".stripMargin
			println(msg)
			sys.exit(1)
		}

		// Load the data stored in LIBSVM format as a DataFrame.
	  val data = MLUtils.loadLibSVMFile(sc, params.input)
		//val data = spark.read.format("libsvm").load(args(0))

		// Split the data into train and test
    val splits = data.randomSplit(Array(0.75, 0.25))
		val (train, test) = (splits(0), splits(1))

		// VALUES OF MODEL
		val numClasses = 3
		val categoricalFeaturesInfo = Map[Int, Int]()
		val impurity = "gini"
		val maxDepth = params.maxDepth
		val maxBins = params.maxBins

		// Call DecisionTree trainClassifier with the train data , which returns the model
		val model = DecisionTree.trainClassifier(train, numClasses,
			categoricalFeaturesInfo, impurity, maxDepth, maxBins)
		val accuracy = getAccuracy(model, test)
		println("Acurracy: " + accuracy)

		// Save model
		// we delete the file where we are going to save to avoid conflicts
		FileUtils.deleteQuietly(new File("target/tmp/DTM"))
		model.save(sc,path="target/tmp/DTM")

		sc.stop()
  	}

		def getOptions(list: List[String], params: Params){
      def isSwitch(s : String) = (s(0) == '-')
      list match {
        case Nil =>
        case "-input"  :: value :: tail => params.input = value.toString
                                           getOptions(tail, params)
        case "-output" :: value :: tail => params.output = value.toString
                                           getOptions(tail, params)
        case "-maxd"    :: value :: tail => params.maxDepth = value.toInt
                                           getOptions(tail, params)
				case "-maxb"    :: value :: tail => params.maxBins = value.toInt
																	         getOptions(tail, params)
        case option :: tail => println("Unknown option " + option)
                               sys.exit(1)
      }

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
