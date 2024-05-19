package akka.sample.cluster.kubernetes

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.cluster.typed.{Cluster, Subscribe}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.javadsl.AkkaManagement
import akka.actor.typed.Behavior
import com.typesafe.config.Config
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._
import akka.actor.AddressFromURIString
object HelloWorld {}

object DemoApp {

  def main(args: Array[String]): Unit = {
    val seedNodePorts = Option(
      ConfigFactory
        .load()
        .getStringList("akka.cluster.seed-nodes")
    )
      .map(_.asScala.flatMap { case AddressFromURIString(s) => s.port })
      .getOrElse(Seq.empty[Int])

    if (seedNodePorts.isEmpty) {
      // if no seed nodes are defined, we are in the kubernetes environment
      val actorSystem = ActorSystem[Nothing](Guardian(8080), "appka")
    } else {

      // Either use a single port provided by the user
      // Or start each listed seed nodes port plus one node on a random port in this single JVM if the user
      // didn't provide args for the app
      // In a production application you wouldn't start multiple ActorSystem instances in the
      // same JVM, here we do it to simplify running a sample cluster from a single main method.
      val ports = args.headOption match {
        case Some(port) => Seq(port.toInt)
        case None       => seedNodePorts ++ Seq(0)
      }

      ports.foreach { port =>
        val httpPort =
          if (port > 0) 10000 + port // offset from akka port
          else 0 // let OS decide

        val config = configWithPort(port)
        ActorSystem[Nothing](Guardian(httpPort), "appka", config)
      }
    }
  }

  private def configWithPort(port: Int): Config =
    ConfigFactory
      .parseString(s"""
       akka.remote.artery.canonical.port = $port
        """)
      .withFallback(ConfigFactory.load())

}
