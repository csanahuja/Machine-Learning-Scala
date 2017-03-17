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
class MPCC(ss: SparkSession, input: String, indexes: String, model: String){

  def classify(pairs: List[(String,String)]): Int = {
    // Load Data to Classify
    val data = ss.read.format("libsvm").load(input)

    // Load indexes
    val tweets_ids = ss.read.option("header","true").csv(indexes)
    print(indexes)
    // Load Model
    val mp_model = MultilayerPerceptronClassificationModel.load(model)

    val result = mp_model.transform(data)
    val prediction_column = result.select("prediction")
    val prediction_rows = prediction_column.collect.map(_.getDouble(0).toInt)

    // printToFile(new File("prueba.txt")){ row =>
    //   prediction_rows.foreach(row.println)
    // }
    return 0
  }

  def printToFile(f: File)(op: PrintWriter => Unit) {
    val p = new PrintWriter(f)
    try { op(p) } finally { p.close() }
  }
}
