package akka.sample.cluster.kubernetes


import akka.actor.ActorRef
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class BasicRoute(
    //clusterActor: ActorRef
     )(implicit ec: ExecutionContext) {

  implicit val timeout: Timeout = Timeout(5.seconds)

  val route: Route = {
    handleExceptions(exceptionHandler) {
      path("hello") {
        get {
          //complete((clusterActor ? "hello").mapTo[String])
          complete("Hello world")
        }
      }
    }
  }

  private val exceptionHandler = ExceptionHandler {
    case e: Exception =>
      extractUri { uri =>
        println(s"Request to $uri could not be handled normally")
        complete(HttpResponse(StatusCodes.InternalServerError, entity = e.getMessage))
      }
  }
}
