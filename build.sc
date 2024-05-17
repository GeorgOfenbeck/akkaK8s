import mill._
import mill.scalalib._

import coursier.maven.MavenRepository
import contrib.docker.DockerModule
import $ivy.`com.lihaoyi::mill-contrib-docker:$MILL_VERSION`
import scala.io.Source

object akkak8s extends ScalaModule {

  //def scalaVersion = "2.13.13"
  def scalaVersion = "3.3.3"
  def ammoniteVersion = "3.0.0-M1"
  def akkaManagementVersion = "1.5.1"
  def akkaVersion = "2.9.3"
  def akkaHttpVersion = "10.5.3"

  def mainClass = Some("akka.sample.cluster.kubernetes.DemoApp")

  def loadToken: T[String] = T {
    val source = Source.fromFile("token.txt")
    val token: String =
      try source.mkString
      finally source.close()

    s"https://repo.lightbend.com/pass/${token}/commercial-releases"
  }

  def repositoriesTask = T.task {
    super.repositoriesTask() ++ Seq(
      MavenRepository("https://repo.akka.io/maven"),
      MavenRepository(loadToken())
    )
  }

  def downloadCinnamonAgent = T {
    import coursier._
    // val fetch: Seq[java.io.File] = coursier.Fetch()
    //  .addDependencies(dep"com.lightbend.cinnamon:sbt-cinnamon-agent_1.0:2.20.0")
    //  .run()

    // val result = fetch.runResult()
    // result.getFiles().toArray
    // T.dest / "cinnamon-agent.jar"
    // fetch.head.getAbsolutePath
    // https://repo1.maven.org/maven2/com/lightbend/cinnamon/sbt-cinnamon_2.12_1.0/2.20.0/sbt-cinnamon-2.20.0.jar
    os.proc(
      "curl",
      "-L",
      "-o",
      T.dest / "cinnamon-agent.jar",
      "https://repo1.maven.org/maven2/com/lightbend/cinnamon/sbt-cinnamon_2.12_1.0/2.20.0/sbt-cinnamon-2.20.0.jar"
    ).call()
    T.dest / "cinnamon-agent.jar"
  }

  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-http:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-http-spray-json:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-cluster-typed:${akkaVersion}",
    ivy"com.typesafe.akka::akka-cluster-sharding-typed:${akkaVersion}",
    ivy"com.typesafe.akka::akka-stream-typed:${akkaVersion}",
    ivy"com.typesafe.akka::akka-discovery:${akkaVersion}",
    ivy"com.typesafe.akka::akka-serialization-jackson:${akkaVersion}",
    ivy"ch.qos.logback:logback-classic:1.2.3",
    ivy"com.lightbend.akka.discovery::akka-discovery-kubernetes-api:${akkaManagementVersion}",
    ivy"com.lightbend.akka.management::akka-management-cluster-bootstrap:${akkaManagementVersion}",
    ivy"com.lightbend.akka.management::akka-management-cluster-http:${akkaManagementVersion}",
    ivy"com.lightbend.cinnamon:cinnamon-agent:2.20.0",

    // "com.typesafe.akka::akka-testkit" % akkaVersion % "test",
    // "com.typesafe.akka::akka-actor-testkit-typed" % akkaVersion % Test,
    // "com.typesafe.akka::akka-http-testkit" % akkaHttpVersion % Test,
    // "com.typesafe.akka::akka-testkit" % akkaVersion % Test,
    // "com.typesafe.akka::akka-stream-testkit" % akkaVersion % Test)
    ivy"com.lightbend.cinnamon::cinnamon-akka:2.20.0",
    ivy"com.lightbend.cinnamon::cinnamon-akka-http:2.20.0",
    ivy"com.lightbend.cinnamon:cinnamon-jvm-metrics-producer:2.20.0",
    ivy"com.lightbend.cinnamon:cinnamon-opentelemetry:2.20.0",
  )

  def runIvyDeps = Agg(
    ivy"com.lightbend.cinnamon::cinnamon-agent:2.20.0"
  )

  // Define the first target with its own resources folder
  object kube extends ScalaModule with DockerModule {
    def scalaVersion = akkak8s.scalaVersion
    def sources = akkak8s.sources
    def ivyDeps = akkak8s.ivyDeps
    def repositoriesTask = akkak8s.repositoriesTask

    object docker extends DockerConfig {
      def tags = List("ofenbeck/akkak8s:serial3")
      // def baseImage = "adoptopenjdk:11-jre-hotspot"
      def baseImage = "eclipse-temurin:21-jre-alpine"
      def exposedPorts = Seq(8080, 8558, 25520)
      // def executable = "docker buildx --platform linux/arm64"
      // def executable = "docker"
      def platform = "linux/arm64"
    }
  }

  // Define the second target with its own resources folder
  object local extends ScalaModule {
    def scalaVersion = akkak8s.scalaVersion
    def sources = akkak8s.sources
    def ivyDeps = akkak8s.ivyDeps
    def repositoriesTask = akkak8s.repositoriesTask
    // def forkArgs = Seq(s"-javaagent:${akkak8s.downloadCinnamonAgent()}")
    def forkArgs = Seq(s"-javaagent:/home/rayd/cinnamon-agent-2.20.0.jar")
  }

  object test extends ScalaTests with TestModule.Munit {
    def ivyDeps = Agg(
      ivy"org.scalameta::munit::0.7.29",
      ivy"com.lihaoyi::utest:0.8.3",
      ivy"com.lihaoyi::requests:0.8.2"
    )
  }
}
