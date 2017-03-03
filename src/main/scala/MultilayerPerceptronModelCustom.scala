import org.apache.spark.{SparkConf, SparkContext}

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import scala.util.Random


// Import classes for MLLib
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;

// @Annotation
// TODO: rework program to just generate 1 model by the parameters entered
// as arguments. Make a script to call the program with different parameters.
// Study the influence of different parameters and see which one are more succes.

// MultilayerPerceptronModelCustom
object MPMC {

  class Params(var input : String, var output : String, var maxIters : Int,
               var block: Int, var seed: Long, var layers : List[Int]) {
    def this() {
        this("", "", 100, 128, 1234L, Nil);
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
    val spark = SparkSession
      .builder
      .appName("MPMC")
      .getOrCreate()

    val arglist = args.toList
    var params = new Params()

    getOptions(arglist, params)

    printf(params.toString)
    val input = params.input
    val layers2 = params.layers
    val maxIters = params.maxIters
    println("Input:" + input + " " + input.getClass)
    println("layers:" + layers2 + " " + layers2.getClass)
    println("maxIter:" + maxIters + " " + maxIters.getClass)

    if(input == ""){
      val msg = """Usage -> arguments: -input file <optinal parameters>
                  |Enter --help to see a full list of options""".stripMargin
      println(msg)
      sys.exit(1)
    }

    // Load the data stored in LIBSVM format as a DataFrame.
    val data = spark.read.format("libsvm").load(input.toString)

    // Split the data into train and test
    val splits = data.randomSplit(Array(0.75, 0.25))
    val (train, test) = (splits(0), splits(1))

    // Generate Model
    val numFeatures = data.first().getAs[Vector](1).size
    val classes = 3
    val layers = getRandomLayer(numFeatures, classes)

    // Generate Model
    val trainer = new MultilayerPerceptronClassifier()
      .setLayers(layers)
      .setBlockSize(128)
      .setSeed(1234L)
      .setMaxIter(100)
    val model = trainer.fit(train)

    // Get Accuracy
    val accuracy = getAccuracy(model, test)
    println("Results = " + accuracy)

    // Save model
    model.write.overwrite().save("target/tmp/MPM")

    spark.stop()
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
        case "-layers" :: value :: tail => params.layers = value.split(",").toList.map(_.toString.toInt)
                                           getOptions(tail, params)

        case option :: tail => println("Unknown option " + option)
                               sys.exit(1)
      }

    }

    def getRandomLayer(num_features : Int, output_classes: Int): Array[Int] = {
      val r = Random
      var num_layers = r.nextInt(5)
      if (num_layers != 0)
        num_layers += 1
      val layers = new Array[Int](num_layers + 2)

      //Initial Features
      layers(0) = num_features

      //Output classes
      layers(num_layers+1) = output_classes

      for(i <- 1 to num_layers)
        layers(i) = r.nextInt(50) + 10

      return layers
    }

    //Auxiliar Function: returns accuracy of a given model with a data set
    def getAccuracy(model: MultilayerPerceptronClassificationModel,
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
