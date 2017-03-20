import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import java.io._
import scala.io._
import scala.util.matching.Regex
import scala.collection.mutable.ListBuffer

// Import classes for MLLib
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;

// @Annotation
// TODO: Re-Think how to classify tweets passed by parameter and how to return
// the result. Modify directly the file (?)

// MultilayerPerceptronClassifierCustom
class MPCC(ss: SparkSession, input: String, indexes: String, model: String){

  def classify(pairs: List[TweetPair]): List[Int] = {
    // Load Data to Classify
    val data = ss.read.format("libsvm").load(input)

    // Load indexes
    val text = ss.read.textFile(indexes)
    val pair_indexes = text.collect().map(parseTweetPair).toList

    // Load Model
    val mp_model = MultilayerPerceptronClassificationModel.load(model)

    val result = mp_model.transform(data)
    val prediction_column = result.select("prediction")
    val prediction_rows = prediction_column.collect.map(_.getDouble(0).toInt)

    println(prediction_rows.mkString(" "))
    println(pair_indexes.getClass)
    return getValues(pairs, pair_indexes, prediction_rows)
    return Nil

    // printToFile(new File("prueba.txt")){ row =>
    //   prediction_rows.foreach(row.println)
    // }
  }

  def getValues(pairs: List[TweetPair], indexes: List[TweetPair], classifications: Array[Int]): List[Int] = {
    var index = - 1
    var lb = new ListBuffer[Int]
    for(pair <- pairs){
      index = getIndexes(pair,indexes)
      if (index != -1)
        lb += classifications(index)
      else
        lb += 0
    }
    return lb.toList
  }

  def getIndexes(pair: TweetPair, indexes: List[TweetPair]): Int = {
    var i = 0
    for(index <- indexes){
      if (index.equals(pair))
        return i
      i += 1
    }
    return -1
  }

  def printToFile(f: File)(op: PrintWriter => Unit) {
    val p = new PrintWriter(f)
    try { op(p) } finally { p.close() }
  }

  case class TweetPair(id1: String, id2: String)

  def parseTweetPair(str: String): TweetPair = {
    val line = str.split(" ")
    TweetPair(line(0), line(1))
  }

}
