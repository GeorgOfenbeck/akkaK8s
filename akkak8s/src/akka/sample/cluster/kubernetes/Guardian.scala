package akka.sample.cluster.kubernetes

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.management.scaladsl.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap

/** Root actor bootstrapping the application
  */
object Guardian {

  def apply(httpPort: Int): Behavior[Nothing] = Behaviors.setup[Nothing] {
    context =>

      HelloActor.initSharding(context.system)

      /*     context.log.info(
        "Started [" + context.system + "], cluster.selfAddress = " + cluster.selfMember.address + ")"
      )
       */
      val helloRoute = new BasicRoute(context.system)
      HelloHttpServer.start(helloRoute.route, httpPort, context.system)
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
      if (httpPort == 8080) {
        AkkaManagement.get(context.system).start()
        ClusterBootstrap.get(context.system).start()
      }
      Behaviors.empty

    // WeatherStation.initSharding(context.system)

///    val routes = new WeatherRoutes(context.system)
    //   WeatherHttpServer.start(routes.weather, httpPort, context.system)

//    Behaviors.empty
  }

}
