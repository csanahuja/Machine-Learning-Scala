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
    val accuracy = model.generateModel(new model.Params(), "target/tmp/MPM")
    println("DEBUG : ACCURACY => " + accuracy)

    val classifier = new MPC(ss, args(0), args(1), "target/tmp/MPM")
    var relationShip = classifier.getRelationship(588748014438764544L, 588727284061863936L)
    println("DEBUG : LABEL => " + relationShip)

    //Test to do
    // val file = scala.io.Source.fromFile("/parser/files/conversation.json")
    
    ss.stop()
  }

}
