package mlib.api

import play.api.Play.current
import play.api.libs.concurrent.Akka._
import akka.actor.{ActorRef, Props}
import mlib.impl.{MlibLogger, ConnectionJson, ChannelEventsActor}
import mlib.impl.ChannelEventsActor.{Event, Subscribe}
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global

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
