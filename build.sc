import mill._
import mill.scalalib._

import coursier.maven.MavenRepository
import contrib.docker.DockerModule
import $ivy.`com.lihaoyi::mill-contrib-docker:$MILL_VERSION`


object akkak8s extends ScalaModule with DockerModule {

  //def scalaVersion = "2.13.13"
  def scalaVersion = "3.3.3"
  def ammoniteVersion = "3.0.0-M1"
  def akkaManagementVersion = "1.5.1"
  def akkaVersion = "2.9.2"
  def akkaHttpVersion = "10.5.3"


  def mainClass = Some("akka.sample.cluster.kubernetes.DemoApp")

  def repositoriesTask = T.task{
    super.repositoriesTask() ++ Seq(MavenRepository("https://repo.akka.io/maven"))
  }
  
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-http:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-http-spray-json:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-cluster-typed:${akkaVersion}", 
    ivy"com.typesafe.akka::akka-cluster-sharding-typed:${akkaVersion}",
    ivy"com.typesafe.akka::akka-stream-typed:${akkaVersion}",
    ivy"com.typesafe.akka::akka-discovery:${akkaVersion}",
    ivy"ch.qos.logback:logback-classic:1.2.3",
    ivy"com.lightbend.akka.discovery::akka-discovery-kubernetes-api:${akkaManagementVersion}",
    ivy"com.lightbend.akka.management::akka-management-cluster-bootstrap:${akkaManagementVersion}",
    ivy"com.lightbend.akka.management::akka-management-cluster-http:${akkaManagementVersion}",
    //"com.typesafe.akka::akka-testkit" % akkaVersion % "test",
    //"com.typesafe.akka::akka-actor-testkit-typed" % akkaVersion % Test,
    //"com.typesafe.akka::akka-http-testkit" % akkaHttpVersion % Test,
    //"com.typesafe.akka::akka-testkit" % akkaVersion % Test,
    //"com.typesafe.akka::akka-stream-testkit" % akkaVersion % Test) 
  )

  object docker extends DockerConfig {
    def tags = List("ofenbeck/akkak8s:demo4")

    //def baseImage = "adoptopenjdk:11-jre-hotspot"
    def baseImage = "eclipse-temurin:21-jre-alpine"
    def exposedPorts = Seq(8080,8558,25520)
    //def executable = "docker buildx --platform linux/arm64"
    //def executable = "docker"
    def platform = "linux/arm64"

  }


  object test extends ScalaTests with TestModule.Munit {
    def ivyDeps = Agg(
      ivy"org.scalameta::munit::0.7.29",
      ivy"com.lihaoyi::utest:0.8.3",
      ivy"com.lihaoyi::requests:0.8.2"
    )
  }
}
