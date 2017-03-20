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
class MPCC(ss: SparkSession, indexes: String,
                             input: String = "",
                             model: String = "",
                             saved: String = ""){

  def classify(pairs: List[TweetPair], classify: Boolean = true,
                                       save: Boolean = false
                                       ): List[Int] = {
    // Load indexes
    val text = ss.read.textFile(indexes)
    val pair_indexes = text.collect().map(parseTweetPair).toList

    var predictions = Array[Int]()

    // If classify flag -> use the model to classify the input file
    if (classify){
      // Classifying
      if(input != "" && model != ""){
        // Load Data to Classify
        val data = ss.read.format("libsvm").load(input)
        // Load Model
        val mp_model = MultilayerPerceptronClassificationModel.load(model)

        val result = mp_model.transform(data)
        val prediction_column = result.select("prediction")
        predictions = prediction_column.collect.map(_.getDouble(0).toInt)
      }
      // Missing Arguments to classify (input_file || model_file)
      else
        return Nil
    // If not classify we read classififications from saved file
    } else {
      // Obtain classifications
      if (saved != ""){
        val text_predictions = ss.read.textFile(saved)
        predictions = text_predictions.collect().map(_.toInt)
      // Mising Arguments to obtain saved results (save_file)
      } else
        return Nil
    }

    // If save flag we override saved file with results
    if (save){
      if (saved != ""){
        printToFile(new File(saved)){ row =>
           predictions.foreach(row.println)
        }
      }
    }

    // Here we get classification to only the desired pairs
    return getValues(pairs, pair_indexes, predictions)

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
