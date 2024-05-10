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
     clusterActor: ActorRef
)(implicit ec: ExecutionContext) {

  implicit val timeout: Timeout = Timeout(5.seconds)

  private def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case ex =>
      complete(
        HttpResponse(StatusCodes.InternalServerError, entity = ex.getMessage)
      )
  }

  val route: Route = {
    handleExceptions(exceptionHandler) {
      pathPrefix("hello") {
        get {
          complete((clusterActor ? HelloActor.SayHello("world")).mapTo[HelloActor.HelloGreeting].map(_.message))
         // complete("Hello world")
        }
      }
    }
  }

}
