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
    val model = new MPT(ss, args(0))
    model.generateModel(new model.Params(), "target/tmp/MPM")
    val classifier = new MPC(ss, args(0), args(1), "target/tmp/MPM")

    //Test to do
    // val file = scala.io.Source.fromFile("/parser/files/conversation.json")

    // pairs {("841272947461509120","841288423684075520")
    //         ("841272947461509120","841288423684075524")
    //         ("841272947461509120","841288423684075521")
    //         ("841272947461509120","841288423684075522")
    //         ("841272947461509120","841288423684075520")}

    var relationShip = classifier.getRelationship(588748014438764544L, 588727284061863936L)
    println("Value" + relationShip)
    ss.stop()
  }

}
