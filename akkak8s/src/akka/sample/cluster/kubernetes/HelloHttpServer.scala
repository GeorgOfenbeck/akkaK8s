package akka.sample.cluster.kubernetes


import scala.util.{Failure, Success}
import scala.concurrent.duration._

import akka.actor.typed.ActorSystem
import akka.actor.CoordinatedShutdown
import akka.{Done, actor => classic}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

object HelloHttpServer {

  /**
   * Logic to bind the given routes to a HTTP port and add some logging around it
   */
  def start(routes: Route, port: Int, system: ActorSystem[_]): Unit = {
    import akka.actor.typed.scaladsl.adapter._
    implicit val classicSystem: classic.ActorSystem = system.toClassic
    val shutdown = CoordinatedShutdown(classicSystem)

    import system.executionContext

    Http().bindAndHandle(routes, "0.0.0.0", port).onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(
          "Webserver listening on http://{}:{}/",
          address.getHostString,
          address.getPort
        )

        shutdown.addTask(
          CoordinatedShutdown.PhaseServiceRequestsDone,
          "http-graceful-terminate"
        ) { () =>
          binding.terminate(10.seconds).map { _ =>
            system.log.info(
              "Webserver http://{}:{}/ graceful shutdown completed",
              address.getHostString,
              address.getPort
            )
            Done
          }
        }
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

}
