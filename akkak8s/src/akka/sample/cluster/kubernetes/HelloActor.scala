package akka.sample.cluster.kubernetes

import akka.actor.{Actor, ActorLogging, Props, Stash, Status}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import akka.pattern.pipe
import akka.actor.typed.Behavior
import akka.actor.typed.ActorSystem
import akka.actor.typed.PostStop
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.LoggerOps
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize


object HelloActor {

  // setup for using WeatherStations through Akka Cluster Sharding
  // these could also live elsewhere and the WeatherStation class be completely
  // oblivious to being used in sharding
  val TypeKey: EntityTypeKey[HelloActor.Command] =
    EntityTypeKey[HelloActor.Command]("HelloActor")

  def initSharding(system: ActorSystem[_]): Unit =
    ClusterSharding(system).init(Entity(TypeKey) { entityContext =>
      HelloActor(entityContext.entityId)
    })

  // actor commands and responses
  sealed trait Command extends CborSerializable

  final case class SayHello(name: String) extends Command 
  final case class HelloGreeting(message: String) extends Command 

  def apply(wsid: String): Behavior[Command] = Behaviors.setup { context =>
    context.log.info(s"HelloActor started with wsid: $wsid")
    running(context,wsid)
  }

  private def running(context: ActorContext[Command], wsid: String): Behavior[Command] = {
    Behaviors.receiveMessage[Command] {
      case SayHello(name) =>
        context.log.info("HelloActor received a SayHello message")
        Behaviors.same
    }
  }
}