package mlib.api

import akka.actor.ActorRef
import scala.concurrent.Future
import mlib.api.Message._
import mlib.impl.ConnectionJson

abstract class ModuleSystem extends ChannelHandler with ConnectionHandler {
  def unsubscribe(channel: Message.ChannelType, module: ActorRef) = ???
}

trait ChannelHandler {
  def subscribe(channel: Message.ChannelType, module: ActorRef)
  def unsubscribe(channel: Message.ChannelType, module: ActorRef)
  def event(connection: Future[ConnectionJson], message: Message)
}

trait ConnectionHandler {
  def getConnection(id: Message.ConnectionId): Future[ConnectionJson]
  def connected(connection: ConnectionJson)
  def disconnected(connectionId: ConnectionId)
}
