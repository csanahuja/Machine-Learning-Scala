import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame

// Import classes for ML
import org.apache.spark.ml.{Pipeline,PipelineModel}
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorIndexer}

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
		val sc = SparkSession
      .builder
      .appName("Decision Tree Model")
      .getOrCreate()

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
    val data = sc.read.format("libsvm").load(params.input)

		// Split the data into train and test
    val splits = data.randomSplit(Array(0.75, 0.25))
    val (train, test) = (splits(0), splits(1))

		// Index labels, adding metadata to the label column.
		// Fit on whole dataset to include all labels in index.
		val labelIndexer = new StringIndexer()
		  .setInputCol("label")
		  .setOutputCol("indexedLabel")
		  .fit(data)
		// Automatically identify categorical features, and index them.
		val featureIndexer = new VectorIndexer()
		  .setInputCol("features")
		  .setOutputCol("indexedFeatures")
		  .setMaxCategories(4) // features with > 4 distinct values are treated as continuous.
		  .fit(data)

		// Train a DecisionTree model.
		val dt = new DecisionTreeClassifier()
		  .setLabelCol("indexedLabel")
		  .setFeaturesCol("indexedFeatures")

		// Convert indexed labels back to original labels.
		val labelConverter = new IndexToString()
		  .setInputCol("prediction")
		  .setOutputCol("predictedLabel")
		  .setLabels(labelIndexer.labels)

		// Chain indexers and tree in a Pipeline.
		val pipeline = new Pipeline()
		  .setStages(Array(labelIndexer, featureIndexer, dt, labelConverter))

		// Train model. This also runs the indexers.
		val model = pipeline.fit(train)

    // Get Accuracy
		val accuracy = getAccuracy(model, test)
		println("Acurracy: " + accuracy)

		// Save model
    if(params.output == "")
      model.write.overwrite().save("target/tmp/DTM")
    else
      model.write.overwrite().save(params.output)

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

		//Auxiliar Function: returns accuracy of a given model with a data set
    def getAccuracy(model: PipelineModel,
                      test: DataFrame): Double = {
      //compute accuracy on the test set
      val result = model.transform(test)
      val predictionAndLabels = result.select("prediction", "label")
      val evaluator = new MulticlassClassificationEvaluator()
        .setMetricName("accuracy")

      val accuracy = evaluator.evaluate(predictionAndLabels)
      return accuracy
    }

}
