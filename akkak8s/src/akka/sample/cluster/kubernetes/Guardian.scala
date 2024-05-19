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

      val shardRegion = HelloActor.initSharding(context.system)

      val helloRoute = new BasicRoute(context.system, shardRegion)
      HelloHttpServer.start(helloRoute.route, httpPort, context.system)
    
      if (httpPort == 8080) {
        AkkaManagement.get(context.system).start()
        ClusterBootstrap.get(context.system).start()
      }
      Behaviors.empty

  }
}
