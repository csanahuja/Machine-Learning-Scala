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

// MultilayerPerceptronClassifier
class MPC(ss: SparkSession, indexes: String,
                             input: String,
                             model: String){

    // Load indexes
    val text = ss.read.textFile(indexes)
    val pair_indexes = text.collect().map(parseTweetPair).toList
    // Load Data
    val data = ss.read.format("libsvm").load(input)
    // Load Model
    val mp_model = MultilayerPerceptronClassificationModel.load(model)
    // Classifies
    val results = mp_model.transform(data)
    // Get Prediction column
    val prediction_column = results.select("prediction")
    // Get Predictions
    var predictions = Array[Int]()
    predictions = prediction_column.collect.map(_.getDouble(0).toInt)

  def getRelationship(String id1, String id2): Int = {
    var index = - 1
    var pair = TweetPair(id1, id2)
    index = getIndexes(pair,indexes)
    return if (index != 1) predictions(index) else 0
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

  case class TweetPair(id1: String, id2: String)

  def parseTweetPair(str: String): TweetPair = {
    val line = str.split(" ")
    TweetPair(line(0), line(1))
  }

}
