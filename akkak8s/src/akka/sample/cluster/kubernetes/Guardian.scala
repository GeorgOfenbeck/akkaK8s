package akka.sample.cluster.kubernetes


import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

/**
 * Root actor bootstrapping the application
 */
object Guardian {

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
    
    
    
      import akka.actor.typed.scaladsl.adapter._

      implicit val system: ActorSystem[Nothing] = context.system
      implicit val ec = context.system.executionContext

      val cluster = Cluster(context.system)
      context.log.info(
        "Started [" + context.system + "], cluster.selfAddress = " + cluster.selfMember.address + ")"
      )

      val hallos = ClusterSharding(system).start(
        typeName = "HelloActor",
        entityProps = HelloActor.props,
        settings = ClusterShardingSettings(system.toClassic),
        extractEntityId = HelloActor.extractEntityId,
        extractShardId = HelloActor.shardIdDxtractor
      )

      val helloRoute = new BasicRoute(hallos)
      Http().newServerAt("0.0.0.0", 8080).bind(helloRoute.route)

/*
        //Debugging cluster events
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
*/


      AkkaManagement.get(system).start()
      ClusterBootstrap.get(system).start()
      Behaviors.empty
    
   // WeatherStation.initSharding(context.system)

///    val routes = new WeatherRoutes(context.system)
 //   WeatherHttpServer.start(routes.weather, httpPort, context.system)

//    Behaviors.empty
  }

}
