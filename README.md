# Machine Learning Tweet
Diferent Machine Learning Models to learn the relationship between 2 tweets

## Dependences
- Scala 2.10
- Apache Spark 2.1.0

## How to execute on Linux with SPARK and SBT
Execute the following cmds:
```
git clone https://github.com/csanahuja/Machine-Learning-Scala
sbt package
<spark_route>/spark-submit --class "Class" --m local[4] <project_route>/target/scala-2.10/mlapp-project_2.10-1.0.jar
```
Basically need to indicate where spark-submit is and where the project.jar compiled
by the sbt cmd is.
