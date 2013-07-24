package mlib.impl

import mlib.api.{Message, ModuleSystem}
import scala.concurrent.Future
import akka.actor.{Props, ActorRef}
import play.api.libs.concurrent.Akka._
import mlib.impl.ChannelEventsActor.{Event, Subscribe}
import concurrent.ExecutionContext.Implicits.global
import play.api.Play.current
import akka.util.Timeout
import mlib.impl.ConnectionActor.{CloseConnection, NewConnection, GetConnection}

class JsonModuleSystem extends ModuleSystem with MlibLogger {
  private val mqActor = system.actorOf(Props[ChannelEventsActor], "mq")
  private val connActor = system.actorOf(Props[ConnectionActor], "conn")

  def subscribe(channel: Message.ChannelType, module: ActorRef) = mqActor ! Subscribe(channel, module)

  def event(connection: Future[ConnectionJson], message: Message) {
    connection.onSuccess{ case c: ConnectionJson =>
      mqActor ! Event(c, message)
      log.debug(s"incoming(${c.id}}) - $message")
    }
  }

  def getConnection(id: Message.ConnectionId) = {
    import akka.pattern.ask
    import concurrent.duration._
    implicit val to = Timeout(5 seconds)
    (connActor ? GetConnection(id)).mapTo[ConnectionJson]
  }

  def connected(connection: ConnectionJson) {
    connActor ! NewConnection(connection)
    log.trace(s"new connection $connection")
  }

  def disconnected(connectionId: Message.ConnectionId) {
    connActor ! CloseConnection(connectionId)
    log.trace(s"connection closed $connectionId")
  }
}
