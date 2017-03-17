import org.apache.spark.{SparkConf, SparkContext}

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import java.io._
import scala.io._
import scala.util.matching.Regex

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
      .appName("Multilayer Perceptron Classifier")
      .getOrCreate()


    if (args.size < 1){
      println("Usage -> args: input_file MPmodel_file")
      System.exit(1)
    }

    // Load Data to Classify
    val data = spark.read.format("libsvm").load(args(0))

    // Load Model
    // Change the route by args(1)
    val model = MultilayerPerceptronClassificationModel.load(args(2))

    val result = model.transform(data)
    val prediction_column = result.select("prediction")
    val prediction_rows = prediction_column.collect.map(_.getDouble(0).toInt)

    printToFile(new File(args(1))) { row =>
      prediction_rows.foreach(row.println)
    }
  }

  def printToFile(f: File)(op: PrintWriter => Unit) {
    val p = new PrintWriter(f)
    try { op(p) } finally { p.close() }
  }
}
