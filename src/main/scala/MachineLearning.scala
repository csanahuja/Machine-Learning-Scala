import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import scala.collection.mutable.ListBuffer

object MachineLearning{
  def main(args: Array[String]) {
    val ss = SparkSession
      .builder
      .appName("Machine Learning")
      .getOrCreate()

    //Execute methods
    val classifier = new MPCC(ss, args(0), args(1), args(2))

    var lb = new ListBuffer[classifier.TweetPair]
    val tp1 = classifier.TweetPair("841272947461509120","841288423684075520")
    val tp2 = classifier.TweetPair("841272947461509120","841288423684075524")
    val tp3 = classifier.TweetPair("841272947461509120","841288423684075525")
    val tp4 = classifier.TweetPair("841272947461509120","841288423684075521")
    val tp5 = classifier.TweetPair("841272947461509120","841288423684075522")
    val tp6 = classifier.TweetPair("841272947461509120","841288423684075520")
    lb += tp1
    lb += tp2
    lb += tp3
    lb += tp4
    lb += tp5
    lb += tp6
    val listPairs = lb.toList

    var list = classifier.getRelationship(listPairs)
    println("List Of Model Classifications " + list.mkString(" "))

    list = classifier.getRelationship(listPairs)
    println("List Of Saved File Classifcations " + list.mkString(" "))

    ss.stop()
  }
  case class TweetPair(id1: String, id2: String)
}
