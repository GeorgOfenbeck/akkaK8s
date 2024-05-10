package akka.sample.cluster.kubernetes

import akka.actor.{Actor, ActorLogging, Props, Stash, Status}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import akka.pattern.pipe

object HelloActor {
  def props: Props = Props(new HelloActor)

  final case class SayHello(name: String)
  final case class HelloGreeting(message: String)
}


class HelloActor extends Actor with ActorLogging with Stash {
  import HelloActor._

  override def receive: Receive = {
    case SayHello(name) =>
      log.info("HelloActor received a SayHello message")
      sender() ! HelloGreeting(s"Hello, $name")
  }


}