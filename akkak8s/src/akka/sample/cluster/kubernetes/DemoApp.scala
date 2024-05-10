package akka.sample.cluster.kubernetes

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent
import akka.cluster.typed.{Cluster, Subscribe}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.javadsl.AkkaManagement
import akka.actor.typed.Behavior

import akka.http.scaladsl.Http

object HelloWorld {}

object DemoApp extends App {

  def setup(): Behavior[Nothing] = {

    Behaviors.setup[Nothing] { context =>
      import akka.actor.typed.scaladsl.adapter._

      implicit val system: ActorSystem[Nothing] = context.system
      implicit val ec = context.system.executionContext

      val cluster = Cluster(context.system)
      context.log.info(
        "Started [" + context.system + "], cluster.selfAddress = " + cluster.selfMember.address + ")"
      )

      val helloRoute = new BasicRoute()
      Http().newServerAt("0.0.0.0", 8080).bind(helloRoute.route)

      // Create an actor that handles cluster domain events
      val listener = context.spawn(
        Behaviors.receive[ClusterEvent.MemberEvent]((ctx, event) => {
          ctx.log.info("MemberEvent: {}", event)
          Behaviors.same
        }),
        "listener"
      )

      Cluster(context.system).subscriptions ! Subscribe(
        listener,
        classOf[ClusterEvent.MemberEvent]
      )

      AkkaManagement.get(system).start()
      ClusterBootstrap.get(system).start()
      Behaviors.empty
    }
  }

  val actorSystem = ActorSystem[Nothing](setup(), "appka")

}
