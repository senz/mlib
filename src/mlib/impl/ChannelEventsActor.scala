package mlib.impl

import mlib.api.{SystemChannels, Message}
import mlib.api.Message._
import collection._
import akka.actor.{ActorRef, ActorLogging, Actor}

/**
 * Manages channel events and propagates them to actor modules through subscription..
 */
class ChannelEventsActor extends Actor with ActorLogging {
  import ChannelEventsActor._

  private val subscribers = mutable.HashMap[ChannelType, mutable.Set[ActorRef]]()
  implicit private val dispatcher = context.dispatcher

  def receive = {
    case s: Subscribe => {
      send(SystemChannels.SUBSCRIPTION, s)
      val actors = subscribers.getOrElseUpdate(s.channel, mutable.Set())
      actors += s.actor
      subscribers += s.channel -> actors
      log.debug(s"actor ${s.actor} subscribed on ${s.channel}")
    }
    case u: Unsubscribe => {
      val actors = subscribers.getOrElseUpdate(u.channel, mutable.Set())
      actors -= u.actor
      subscribers += u.channel -> actors
      send(SystemChannels.SUBSCRIPTION, u)
    }
    case e @ Event(c, m) => send(m.channel, e)
  }

  private def send(channel: ChannelType, m: AnyRef) {
    val handlers = subscribers.get(channel)
    if (handlers.isDefined)
    {
      handlers.get.foreach(_ ! m)
      log.debug(s"${handlers.get.size} subscribers notified on $channel")
    }
  }
}

object ChannelEventsActor {
  case class Subscribe(channel: ChannelType, actor: ActorRef)
  case class Unsubscribe(channel: ChannelType, actor: ActorRef)
  case class Event(connection: ConnectionJson, message: Message)
}


