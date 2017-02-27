# Machine Learning Tweet
Diferent Machine Learning Models to learn the relationship between 2 tweets

## Dependences
With sbt, you don't need to download and install Scala.
Just set scalaVersion in your build definition,
and sbt will retrieve that version

- sbt 0.13.13 (check with sbt sbtVersion)
- Apache Spark 2.1.0
- Scala 2.11.x

## How to install sbt 0.13.13 on Fedora

- curl https://bintray.com/sbt/rpm/rpm > bintray-sbt-rpm.repo
- sudo mv bintray-sbt-rpm.repo /etc/yum.repos.d/
- sudo yum install sbt
- sbt sbtVersion (This cmd should update sbt, type again to check version)

## How to execute on Linux with SPARK and SBT
Execute the following cmds:
```
git clone https://github.com/csanahuja/Machine-Learning-Scala
sbt package (You need to be on the folder project where the .sbt is allocated)
<spark_route>/spark-submit --class "Class" --m local[4]
  <project_route>/target/<route_jar>

i.e:
../spark-2.1.0-bin-hadoop2.7/spark-2.1.0-bin-hadoop2.7/bin/spark-submit --class "MPMC" --master local[4]
  ./target/scala-2.11/mlapp-project_2.11-1.0.jar <parameters

```
Basically we need to execute spark-submit with the jar compiled by sbt package
