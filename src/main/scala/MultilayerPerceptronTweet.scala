import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
// Import classes for MLLib
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator

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

    // specify layers for the neural network:
    // input layer of size 3 (features), two intermediate of size 5 and 4
    // and output of size 3 (classes)
    val layers = Array[Int](2,200,100, 3)

    // create the trainer and set its parameters
    val trainer = new MultilayerPerceptronClassifier()
      .setLayers(layers)
      .setBlockSize(128)
      .setSeed(1234L)
      .setMaxIter(100)

    // train the model
    val model = trainer.fit(train)

    // compute accuracy on the test set
    val result = model.transform(test)
    val predictionAndLabels = result.select("prediction", "label")
    val evaluator = new MulticlassClassificationEvaluator()
      .setMetricName("accuracy")

    println("Test set accuracy = " + evaluator.evaluate(predictionAndLabels))


    // val results = "Result: Good Prediction : " + good.toString + "\n" +
    //         "Result: Wrong Prediction: " + wrong.toString + "\n" +
    //         "Result: Ratio: " + ratio.toString + "\n"
    // cPrint(results)

    spark.stop()
    }

    def cPrint(str: String) {
      val output = "\n" + str + "\n\n"
      print(output)
    }

}
