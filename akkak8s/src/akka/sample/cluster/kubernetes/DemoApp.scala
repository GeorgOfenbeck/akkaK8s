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

import akka.http.scaladsl.Http

object HelloWorld {}

object DemoApp extends App {


  val actorSystem = ActorSystem[Nothing](Guardian(), "appka")

}
