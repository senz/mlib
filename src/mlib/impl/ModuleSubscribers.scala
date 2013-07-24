package mlib.impl

import mlib.api._
import collection._
import play.api.libs.json.JsValue
import scala.concurrent.{Promise, Future}
import mlib.impl.ChannelEventsActor.Event
import play.api.Logger

// TODO make message generic
/**
 * Adding subscribers handling features for module. Supports single channel management.
 * handleSubscriptionEvents must be set correctly in receive.
 * Note that you must not handle connection and subscription channels yourself.
 */
trait ModuleSubscribers {
  this: Module =>
  private val log = Logger("mlib_subscribers")

  /**
   * Reacting only on this channel subscription events
   */
  val channelFilter: Message.ChannelType
  // TODO support multiple channels
  private val subscribers = mutable.HashMap[Message.ChannelType, mutable.Set[ConnectionJson]]()

  ChannelEvent.subscribe(SystemChannels.SUBSCRIPTION, self)
  ChannelEvent.subscribe(SystemChannels.CONNECTION, self)
  log.info("inited for " + this.getClass)

  /**
   * Broadcast message to subscribers
   * @param message which send to listeners
   */
  def broadcast(message: JsValue) {
    val connections = subscribers.get(channelFilter)
    if (connections.isDefined)
    {
      connections.get.foreach(_.send(message))
    }
  }

  def getSubscriberUserConnections(userId: Long): Future[Set[ConnectionJson]] = {
    val connections = subscribers.get(channelFilter)
    if (connections.isDefined)
    {
      Promise.successful(connections.get filter { c => c.loginData.isDefined && c.loginData.get.userId == userId}).future
    } else Promise.successful(Set[ConnectionJson]()).future
  }

  /**
   * Must be last case in actor's receive, will manage subscribers through subscription, connection events.
   * @return
   */
  final def handleSubscriptionEvents: PartialFunction[Event, Unit] = {
    case Event(c, m @ Message(SystemChannels.CONNECTION, cnt, _)) if (cnt \ "event").asOpt[String].isDefined && (cnt \ "event").as[String] == "closed" => {
      val cid = (m.content \ "connectionId").as[Message.ConnectionId]
      subscribers foreach { s =>
        subscribers(s._1) = s._2.filterNot(_.id == cid)
        if (subscribers(s._1).size == 0) {
          subscribers -= s._1
          log.debug(s"connection '${c.id}' (closed) unsubscribed from $channelFilter")
        }
      }
    }
    case e @ Event(c, m @ Message(SystemChannels.SUBSCRIPTION, cnt, _)) if (cnt \ "subscribe").asOpt[String].isDefined => {
      val channel = (m.content \ "subscribe").as[String]
      // TODO check if already subscribed
      if (filterSubscriptions(channelFilter, e) && subscriberCheck(e)) {
        val conns = subscribers.getOrElseUpdate(channel, mutable.HashSet())
        conns += c
        log.debug(s"connection '${c.id}' subscribed on $channelFilter")
        onSubscribe(e)
      }
    }
    case e @ Event(c, m @ Message(SystemChannels.SUBSCRIPTION, cnt, _)) if (cnt \ "unsubscribe").asOpt[String].isDefined => {
      val channel = (m.content \ "unsubscribe").as[String]
      if (channel == channelFilter) {
        val conns = subscribers.get(channel)
        if (conns.isDefined) {
          conns.get -= c; onUnsubscribe(e)
          log.debug(s"connection '${c.id}' unsubscribed from $channelFilter")
        }
      }
    }
    case _ =>
  }

  /**
   * Here actor can filter subscriptions.
   * @param event for checking
   * @return
   */
  def subscriberCheck(event: Event): Boolean

  /**
   * Unsubscription hook, called after unsubscription was made.
   * @param event
   */
  def onUnsubscribe(event: Event) {}

  /**
   * Subscription hook, called if subscription was successful.
   * @param event
   */
  def onSubscribe(event: Event) {}

  /**
   * Returns current count of subscribers.
   * @return
   */
  def subscribersSize = subscribers.getOrElse(channelFilter, Set()).size

  private def filterSubscriptions(channel: String, e: Event): Boolean = {
    if (channel != channelFilter) false
    else if (subscribers.contains(channel)) {
      !subscribers(channel).contains(e.connection)
    }
    else true
  }
}
