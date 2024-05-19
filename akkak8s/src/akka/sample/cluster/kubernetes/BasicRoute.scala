package akka.sample.cluster.kubernetes

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.util.Timeout
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.model.HttpResponse
import akka.actor.typed.ActorRef
import akka.cluster.sharding.typed.ShardingEnvelope

class BasicRoute(
    system: ActorSystem[_],
    shardingRegion: ActorRef[ShardingEnvelope[HelloActor.Command]]
) {
  private val sharding = ClusterSharding(system)
  implicit val timeout: Timeout = Timeout(5.seconds)

  // imports needed for the routes and entity json marshalling
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import akka.http.scaladsl.server.Directives._

  private def exceptionHandler: ExceptionHandler = ExceptionHandler { case ex =>
    complete(
      HttpResponse(StatusCodes.InternalServerError, entity = ex.getMessage)
    )
  }

  private def helloInteraction(wsid: Long): Future[HelloActor.HelloGreeting] = {
    val ref = sharding.entityRefFor(HelloActor.TypeKey, wsid.toString() )
    ref.ask(HelloActor.SayHello("hello", wsid, _))
  }


  val route: Route = {
    handleExceptions(exceptionHandler) {
      path("hello" / LongNumber) { wsid =>
          get {
            //ref ! HelloActor.SayHello(s"hello", wsid)
            onSuccess(helloInteraction(wsid)) { response =>
              complete(response.message)
            }
            // complete("Hello world")
        }
      }
    }
  }

}
