import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame

// Import classes for ML
import org.apache.spark.ml.classification.NaiveBayes
import org.apache.spark.ml.classification.NaiveBayesModel
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator

object NBMC {

  class Params(var input : String, var output : String, var maxIters : Int,
               var block: Int, var seed: Long, var layers : Array[Int]) {
    def this() {
        this("", "", 100, 128, 1234L, Array[Int]());
    }
    override def toString : String =
      "Params: \n" +
      " - Input =    " + input + "\n" +
      " - Output =   " + output + "\n" +
      " - MaxIters = " + maxIters + "\n" +
      " - Block =    " + block + "\n" +
      " - Seed =     " + seed + "\n" +
      " - Layers =   " + layers + "\n"

  }

  def main(args: Array[String]) {
    val sc = SparkSession
      .builder
      .appName("NaiveBayes Model")
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

    // Parse params
    parseParams(params)

    // Train a NaiveBayes model.
    val model = new NaiveBayes()
      .fit(train)

    // Get Accuracy
    val accuracy = getAccuracy(model, test)
    printf("Accuracy: " + accuracy + "\n")

    // Save model
    if(params.output == "")
      model.write.overwrite().save("target/tmp/MPM")
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
        case "-max"    :: value :: tail => params.maxIters = value.toInt
                                           getOptions(tail, params)
        case "-block"  :: value :: tail => params.block = value.toInt
                                           getOptions(tail, params)
        case "-seed"   :: value :: tail => params.seed = value.toLong
                                           getOptions(tail, params)
        case option :: tail => println("Unknown option " + option)
                               sys.exit(1)
      }

    }

    def parseParams(params: Params){
      //seed
      params.seed = System.currentTimeMillis()
    }


    //Auxiliar Function: returns accuracy of a given model with a data set
    def getAccuracy(model: NaiveBayesModel,
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
