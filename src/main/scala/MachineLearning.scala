import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame


object MachineLearning{
  def main(args: Array[String]) {
    val ss = SparkSession
      .builder
      .appName("Machine Learning")
      .getOrCreate()

    //Execute methods
    val classifier = new MPCC(ss, args(0), args(1), args(2))
    classifier.classify(Nil)

    ss.stop()
  }
}
