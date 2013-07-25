package mlib.api

import akka.actor.ActorRef
import scala.concurrent.Future
import mlib.api.Message._
import mlib.impl.ConnectionJson

abstract class ModuleSystem {
  def subscribe(channel: Message.ChannelType, module: ActorRef)
  def event(connection: Future[ConnectionJson], message: Message)

  def getConnection(id: Message.ConnectionId): Future[ConnectionJson]
  def connected(connection: ConnectionJson)
  def disconnected(connectionId: ConnectionId)
}
