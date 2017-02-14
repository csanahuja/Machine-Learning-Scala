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

object ClassifierModelMP {
  final val num_models = 10

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("ClassifierModelMP")
      .getOrCreate()

    // Load Model
    val model = MultilayerPerceptronClassificationModel.load("target/tmp/MultilayerPerceptronTweet")

    // Load Data
    val data = spark.read.format("libsvm")
      .load("tweets.txt")

    // val result = model.transform(data)
    // val predictionAndLabels = result.select("prediction")

    // accuracy
    val accuracy = getAccuracy(model, data)
    println("Result = " + accuracy)

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

}
