package mlib.api

import akka.actor.ActorRef
import mlib.impl.{MlibLogger, ConnectionJson}
import scala.concurrent.Future

/**
 * Channel events public api. Layer between mlib and application.
 */
object ChannelEvent extends MlibLogger {
  /**
   * Subscribe module on channel events.
   * @param channel
   * @param module
   */
  def subscribe(channel: Message.ChannelType, module: ActorRef) = Mlib.system.subscribe(channel, module)

  /**
   * Signals new event with message from connection.
   * @param connection
   * @param message
   */
  def event(connection: Future[ConnectionJson], message: Message) = Mlib.system.event(connection, message)
}
