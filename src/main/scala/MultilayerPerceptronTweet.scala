import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import org.apache.spark._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.linalg.Vector

import scala.util.Random

// Import classes for MLLib
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;

object MultilayerPerceptronTweet {
  final val num_models = 10

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("MultilayerPerceptronTweet")
      .getOrCreate()

    if (args.size < 1){
      println("Usage: arg1: input_file")
      System.exit(1)
    }
    // Load the data stored in LIBSVM format as a DataFrame.
    val data = spark.read.format("libsvm").load(args(0))

    // Split the data into train and test
    val splits = data.randomSplit(Array(0.75, 0.25))
    val train = splits(0)
    val test = splits(1)

    var models = new Array[MultilayerPerceptronClassificationModel](num_models)
    var accuracies = new Array[Double](num_models)

    // specify layers for the neural network: input layer of size 2
    // Intermediate layers and output of size 3 (classes)
    //val layers = Array[Int](2, 200, 200, 3)

    // Generate 10 models with different layers
    val vector = data.first().getAs[Vector](1)
    val numFeatures = vector.size
    val classes = 3

    for(i <- 1 to num_models){
      val layers = getRandomLayer(numFeatures,classes)
      models(i-1) = generateModel(train, layers)
      accuracies(i-1) = getAccuracy(models(i-1), test)
    }

    // Get best model of 10
    val best_model = getBestModel(accuracies)
    debugModels(best_model,accuracies)

    // Save model
    models(best_model).write.overwrite().save("target/tmp/MultilayerPerceptronTweet")

    spark.stop()
    }

    def debugModels(best_model : Int, accuracies: Array[Double]){
      for(i <- 0 to num_models-1)
        println("Results =" + i + " :" + accuracies(i))

      println("Results best" + best_model)
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

    def generateModel(train: DataFrame, layers: Array[Int]):
                MultilayerPerceptronClassificationModel = {

      // create the trainer and set its parameters
      val trainer = new MultilayerPerceptronClassifier()
        .setLayers(layers)
        .setBlockSize(128)
        .setSeed(1234L)
        .setMaxIter(100)

      // train the model
      val model = trainer.fit(train)

      return model
    }

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

    def getBestModel(accuracies: Array[Double]): Int = {
      var best = 0
      for (i <- 1 to num_models-1)
        if (accuracies(i) > accuracies(best))
          best = i
      return best
    }

}
