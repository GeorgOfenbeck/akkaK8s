import mill._
import mill.scalalib._

import coursier.maven.MavenRepository
import contrib.docker.DockerModule
import $ivy.`com.lihaoyi::mill-contrib-docker:$MILL_VERSION`
import scala.io.Source


object akkak8s extends ScalaModule {

  def scalaVersion = "2.13.13"
  //def scalaVersion = "3.3.3"
  def ammoniteVersion = "3.0.0-M1"
  def akkaManagementVersion = "1.5.1"
  def akkaVersion = "2.9.3"
  def akkaHttpVersion = "10.5.3"

  def ivyDeps = Agg(
    ivy"com.lihaoyi::mill-main:0.11.12".withDottyCompat(scalaVersion()),
    //ivy"com.lihaoyi::mill-main-api:0.11.12".withDottyCompat(scalaVersion()),
    ivy"com.lihaoyi::mill-scalalib:0.11.12".withDottyCompat(scalaVersion()),
                    ivy"com.lihaoyi::os-lib:0.10.7"
  )

  def mainClass = Some("akka.sample.cluster.kubernetes.Whatever")

  // Define the first target with its own resources folder
  object kube extends ScalaModule with DockerModule { outer =>
    def scalaVersion = akkak8s.scalaVersion
    def sources = akkak8s.sources
    def ivyDeps = akkak8s.ivyDeps
    def repositoriesTask = akkak8s.repositoriesTask

    object docker extends DockerConfig {
      import os.Shellable.IterableShellable
      def tags = List("ofenbeck/akkak8s:serial7")
      // def baseImage = "adoptopenjdk:11-jre-hotspot"
      def baseImage = "eclipse-temurin:21-jre-alpine" 
      def exposedPorts = Seq(8080, 8558, 25520)
      // def executable = "docker buildx --platform linux/arm64"
      // def executable = "docker"
      def platform: T[String] = "linux/arm64"
      //def run = Seq(
        //s"echo 'hello world'\nADD cinnamon-agent.jar /opt/cinnamon-agent.jar\n"
      //)
      def jvmOptions = Seq("-javaagent:/opt/cinnamon-agent.jar")
      private def pullAndHash = T.input {
        def imageHash() =
          os.proc(executable(), "images", "--no-trunc", "--quiet", baseImage())
            .call(stderr = os.Inherit)
            .out
            .text()
            .trim

        if (pullBaseImage() || imageHash().isEmpty)
          os.proc(executable(), "image", "pull", baseImage())
            .call(stdout = os.Inherit, stderr = os.Inherit)

        (pullBaseImage(), imageHash())
      }
      def buildX = T {
        val dest = T.dest

        val asmPath = outer.assembly().path
        os.copy(asmPath, dest / asmPath.last)
//        os.copy(
//          os.Path(akkak8s.downloadCinnamonAgent()),
 //         dest / "cinnamon-agent.jar"
 //       )
        os.write(dest / "Dockerfile", dockerfile())

        val log = T.log

        val tagArgs = tags().flatMap(t => List("-t", t))

        val (pull, _) = pullAndHash()
        val pullLatestBase =
          IterableShellable(if (pull) Some("--pull") else None)

        val result = if (platform().isEmpty || executable() != "docker") {
          if (platform().nonEmpty)
            log.info(
              "Platform parameter is ignored when using non-docker executable"
            )
          os.proc(executable(), "build", tagArgs, pullLatestBase, dest)
            .call(stdout = os.Inherit, stderr = os.Inherit)
        } else {
          os.proc(
            executable(),
            "buildx",
            "build",
            tagArgs,
            pullLatestBase,
            "--platform",
            platform(),
            dest
          ).call(stdout = os.Inherit, stderr = os.Inherit)
        }
        log.info(s"Docker build completed ${
            if (result.exitCode == 0) "successfully"
            else "unsuccessfully"
          } with ${result.exitCode}")
        tags()
      }
    }
  }

  // Define the second target with its own resources folder
  object local extends ScalaModule {
    def scalaVersion = akkak8s.scalaVersion
    def sources = akkak8s.sources
    def ivyDeps = akkak8s.ivyDeps
    def repositoriesTask = akkak8s.repositoriesTask
    //def forkArgs = Seq(s"-javaagent:${akkak8s.downloadCinnamonAgent()}")
    // def forkArgs = Seq(s"-javaagent:/home/rayd/cinnamon-agent-2.20.0.jar")
  }

  object test extends ScalaTests with TestModule.Munit {
    def ivyDeps = Agg(
      ivy"org.scalameta::munit::0.7.29",
      ivy"com.lihaoyi::utest:0.8.3",
      ivy"com.lihaoyi::requests:0.8.2"
    )
  }
}
