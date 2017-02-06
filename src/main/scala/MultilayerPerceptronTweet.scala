import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import org.apache.spark._
import org.apache.spark.rdd.RDD
// Import classes for MLLib
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator

object MultilayeredPerceptronTweet {
  val conf = new SparkConf().setAppName("MultilayeredPerceptronTweet")
  val sc = new SparkContext(conf)


  //START OF - TO BE MODIFIED TOMORROW TUESDAY
  // Load the data stored in LIBSVM format as a DataFrame.
  val data = spark.read.format("libsvm")
    .load("data/mllib/sample_multiclass_classification_data.txt")

  // Split the data into train and test
  val splits = data.randomSplit(Array(0.6, 0.4), seed = 1234L)
  val train = splits(0)
  val test = splits(1)

  // specify layers for the neural network:
  // input layer of size 4 (features), two intermediate of size 5 and 4
  // and output of size 3 (classes)
  val layers = Array[Int](4, 5, 4, 3)

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
  //END OF - TO BE MODIFIED


  sc.stop()
  }
}
