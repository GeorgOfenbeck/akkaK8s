package akka.sample.cluster.kubernetes

import akka.actor.{Actor, ActorLogging, Props, Stash, Status}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import akka.pattern.pipe
import akka.actor.typed.Behavior

object HelloActor {
  // actor commands and responses
  sealed trait Command extends CborSerializable

  final case class SayHello(name: String) extends Command 
  final case class HelloGreeting(message: String) extends Command 

  def apply(wsid: String): Behavior[Command] = Behaviors.setup { context =>
    context.log.info(s"HelloActor started with wsid: $wsid")
  }

  private def running(context: ActorContext[Command], wsid: String): Behavior[Command] = {
    Behaviors.receiveMessage {
      case SayHello(name) =>
        context.log.info("HelloActor received a SayHello message")
        context.spawn(HelloActor(), "helloactor")
        Behaviors.same
    }
  }
}

class HelloActor extends Actor with ActorLogging with Stash {
  import HelloActor._

  override def receive: Receive = {
    case SayHello(name) =>
      log.info("HelloActor received a SayHello message")
      sender() ! HelloGreeting(s"Hello, $name")
  }
}