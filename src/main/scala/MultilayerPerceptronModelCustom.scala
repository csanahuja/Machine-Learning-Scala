import org.apache.spark.{SparkConf, SparkContext}

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import scala.util.Random
import scopt.OptionParser

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

  case class Params(
        input: String = null,
        maxDepth: Int = 5,
        maxBins: Int = 32,
        minInfoGain: Double = 0.0)

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("MPMC")
      .getOrCreate()

    val defaultParams = Params()

    val parser = new OptionParser[Params]("MPMC") {
      head("MPMC Params.")
      opt[Int]("maxDepth")
        .text(s"max depth of the tree, default: ${defaultParams.maxDepth}")
        .action((x, c) => c.copy(maxDepth = x))
      opt[Int]("maxBins")
        .text(s"max number of bins, default: ${defaultParams.maxBins}")
        .action((x, c) => c.copy(maxBins = x))
      opt[Double]("minInfoGain")
        .text(s"min info gain required to create a split, default: ${defaultParams.minInfoGain}")
        .action((x, c) => c.copy(minInfoGain = x))
      arg[String]("<input>")
        .text("input path to labeled examples")
        .required()
        .action((x, c) => c.copy(input = x))

    }




    if (args.size < 1){
      println("Usage -> arguments: input_file <save_model_file> <model parameters>")
      System.exit(1)
    }
    // Load the data stored in LIBSVM format as a DataFrame.
    val data = spark.read.format("libsvm").load(args(0))

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

    def printParams(params: Params): Unit = {
      print(params)
    }
}
