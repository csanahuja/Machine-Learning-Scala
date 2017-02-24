import org.apache.spark.{SparkConf, SparkContext}

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession

// Import classes for MLLib
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;

// @Annotation
// TODO: Re-Think how to classify tweets passed by parameter and how to return
// the result. Modify directly the file (?)

// MultilayerPerceptronClassifierCustom
object MPCC{
  final val num_models = 10

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("MPCC")
      .getOrCreate()


    if (args.size < 2){
      println("Usage -> args: input_file MPmodel_file")
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

  }

}
