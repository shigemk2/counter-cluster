package buffercluster

import akka.actor._
import akka.cluster._
import akka.persistence._
import ClusterEvent._

class Buffer extends PersistentActor with ActorLogging {
  val cluster = Cluster.get(context.system)
  override def preStart: Unit = {
    println(s"preStart ${self}============================================================")
    cluster.subscribe(self, ClusterEvent.initialStateAsEvents, classOf[MemberUp])
    super.preStart
  }
  override def postStop: Unit = {
    cluster.unsubscribe(self)
    super.postStop
  }

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  val receiveCommand: Receive = {
    case Buffer.Post(key, value) => log.info(s"Post $key=$value @@@@@@@@@@@@@@@@@@@@@@")
    case MemberUp(member) =>
      log.info(s"MemberUp ${member}")
    case msg => log.error(s"Unhandled $msg")
  }

  val receiveRecover: Receive = {
    case msg => log.info(s"receiveRecover $msg")
  }
}

object Buffer {
  val shardingName = "Buffer"
  case class Post(key: String, value: String)
}
