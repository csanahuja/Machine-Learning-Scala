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

// @Annotation
// TODO: Re-Think how to classify tweets passed by parameter and how to return
// the result. Modify directly the file (?)
object MultilayerPerceptronClassifier{
  final val num_models = 10

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("MPC")
      .getOrCreate()


    if (args.size < 2){
      println("Usage: arg1: input_file multilayer_perceptron_model")
      System.exit(1)
    }

    // Load Data to Classify
    val data = spark.read.format("libsvm").load(args(0))

    // Load Model
    // Change the route by args(1)
    val model = MultilayerPerceptronClassificationModel.load("target/tmp/MultilayerPerceptronTweet")

    val result = model.transform(data)
    val prediction = result.select("prediction")
    prediction.show
    return prediction

  }

}
