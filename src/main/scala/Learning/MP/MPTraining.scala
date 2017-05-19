import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import scala.util.Random

// Import classes for ML
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;

// MultilayerPerceptronTraining
class MPT(ss: SparkSession, input: String) {

  // Load the data stored in LIBSVM format as a DataFrame.
  val data = ss.read.format("libsvm").load(input)

  class Params(var training_size : Double = 0.75, var maxIters : Int = 100, var seed: Long = 1234L,
               var block: Int = 128, var layers : Array[Int] = Array.empty[Int])

  def generateModel(in_params: Params, output_file: String = "",
                    random: Boolean = false, accuracy_log : Boolean = false) {


    // Set Layers
    val numFeatures = data.first().getAs[Vector](1).size
    val classes = 3

    var params = in_params
    setLayer(params, numFeatures, classes)

    if (random)
      getRandomParams(params)


    // Split the data into train and test
    val splits = data.randomSplit(Array(params.training_size, 1-params.training_size))
    val (train, test) = (splits(0), splits(1))

    // Generate Model
    val trainer = new MultilayerPerceptronClassifier()
      .setLayers(params.layers)
      .setBlockSize(params.block)
      .setSeed(params.seed)
      .setMaxIter(params.maxIters)
    val model = trainer.fit(train)

    // Get Accuracy
    val accuracy = getAccuracy(model, test)
    if (accuracy_log)
       printf("Accuracy: " + accuracy + "\n")


    // Save model
    if(output_file == "")
      model.write.overwrite().save("target/tmp/MPM")
    else
      model.write.overwrite().save(output_file)

    ss.stop()
    }

    // Set to the layers the features and classes
    def setLayer(params: Params, num_features : Int, output_classes: Int){
      params.layers = Array(num_features) ++ params.layers ++ Array(output_classes)
    }

    // Dummy Sample
    def getRandomParams(params: Params){
      var i = 5
    }

    // Generates random layers
    def getRandomLayer(num_features : Int, output_classes: Int): Array[Int] = {
      val r = Random
      var num_layers = r.nextInt(5)
      if (num_layers != 0)
        num_layers += 1
      val layers = new Array[Int](num_layers + 2)

      //Initial Features
      layers(0) = num_features

      //Output classes
      layers(num_layers+1) = output_classes

      for(i <- 1 to num_layers)
        layers(i) = r.nextInt(50) + 10

      return layers
    }

    // Returns accuracy of a given model with a data set
    def getAccuracy(model: MultilayerPerceptronClassificationModel,
                      test: DataFrame): Double = {
      //compute accuracy on the test set
      val result = model.transform(test)
      val predictionAndLabels = result.select("prediction", "label")
      val evaluator = new MulticlassClassificationEvaluator()
        .setMetricName("accuracy")

      val accuracy = evaluator.evaluate(predictionAndLabels)
      return accuracy
    }
}
