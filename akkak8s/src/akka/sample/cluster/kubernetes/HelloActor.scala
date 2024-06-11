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

  // setup for using HelloActor through Akka Cluster Sharding
  // these could also live elsewhere and the HelloActor class be completely
  // oblivious to being used in sharding
  val TypeKey: EntityTypeKey[HelloActor.Command] =
    EntityTypeKey[HelloActor.Command]("HelloActor")
 
  import akka.cluster.sharding.typed._

  final class MyMessageExtractor[M](val numberOfShards: Int) extends ShardingMessageExtractor[ShardingEnvelope[M],M]{

  override def entityId(envelope: ShardingEnvelope[M]): String = envelope.entityId
  override def shardId(entityId: String): String = "0" //(math.abs(entityId.hashCode) % numberOfShards).toString
  override def unwrapMessage(envelope: ShardingEnvelope[M]): M = envelope.message
  } 


  def initSharding(system: ActorSystem[_])  =
    ClusterSharding(system).init(Entity(TypeKey) { entityContext =>
      HelloActor(entityContext.entityId)
    }.withMessageExtractor(new MyMessageExtractor[HelloActor.Command](100))

    )
    


  // actor commands and responses
  sealed trait Command extends CborSerializable

  final case class SayHello(name: String, fwsid: Long, replyTo: ActorRef[HelloGreeting]) extends Command
  final case class HelloGreeting(message: String, fwsid: Long) extends Command
  final case class ForwardHallo(
      name: String,
      replyTo: ActorRef[HelloActor.Command]
  ) extends Command

  def apply(wsid: String): Behavior[Command] = Behaviors.setup { context =>
    context.log.info(s"HelloActor started with wsid: $wsid")
    running(context, ClusterSharding(context.system), wsid)
  }

  private def running(
      context: ActorContext[Command],
      sharding: ClusterSharding,
      wsid: String
  ): Behavior[Command] = {
    Behaviors.receiveMessage[Command] { 
      case SayHello(name, fwsid, replyTo) => {
        context.log.info(s"HelloActor $wsid received a SayHello $fwsid message")
        val ref = sharding.entityRefFor(HelloActor.TypeKey, (fwsid-1).toString() )
        if (fwsid > 1) ref ! HelloGreeting(s"from $wsid", fwsid-1)
        replyTo ! HelloGreeting(s"Hello from $wsid", fwsid)
        Behaviors.same
      }
      case HelloGreeting(message, fwsid ) => {
        context.log.info(s"HelloActor $wsid received a HelloGreeting message: $message")
        val ref = sharding.entityRefFor(HelloActor.TypeKey, (fwsid-1).toString() )
        context.log.info(s"${ref.dataCenter} ${ref.entityId}")
        if (fwsid > 1) ref ! HelloGreeting(s"from $wsid", fwsid-1)
        Behaviors.same
      } 

    }
  }
}
