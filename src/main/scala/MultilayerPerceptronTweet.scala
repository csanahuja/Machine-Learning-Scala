import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import org.apache.spark._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession

// Import classes for MLLib
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;

object MultilayerPerceptronTweet {

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("MultilayerPerceptronTweet")
      .getOrCreate()

    // Load the data stored in LIBSVM format as a DataFrame.
    val data = spark.read.format("libsvm")
      .load("tweets.txt")

    // Split the data into train and test
    val splits = data.randomSplit(Array(0.3, 0.7))
    val train = splits(0)
    val test = splits(1)

    var models = new Array[MultilayerPerceptronClassificationModel](5)

    // specify layers for the neural network: input layer of size 2
    // Intermediate layers and output of size 3 (classes)
    val layers = Array[Int](2, 200, 200, 3)

    for(i <- 1 to 5){
      models(i-1) = generateModel(data, layers)
    }

    for(i <- 1 to 5)
       println("Result of Model " + i + " = " + checkAccuracy(models(i-1), test))


    spark.stop()
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

    def checkAccuracy(model: MultilayerPerceptronClassificationModel,
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
