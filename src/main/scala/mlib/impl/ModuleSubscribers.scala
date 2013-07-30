package mlib.impl

import mlib.api._
import collection._
import play.api.libs.json.JsValue
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
  private val _subscribers = mutable.HashMap[Message.ChannelType, mutable.Set[ConnectionJson]]()

  ChannelEvent.subscribe(SystemChannels.SUBSCRIPTION, self)
  ChannelEvent.subscribe(SystemChannels.CONNECTION, self)
  log.info("inited for " + this.getClass)

  def subscribers: immutable.Map[Message.ChannelType, immutable.Set[ConnectionJson]] =
    subscribers.toMap map { case (channel, conns) => (channel, conns.toSet) }

  /**
   * Broadcast message to subscribers
   * @param message which send to listeners
   */
  def broadcast(message: JsValue) {
    val connections = _subscribers.get(channelFilter)
    if (connections.isDefined)
    {
      connections.get.foreach(_.send(message))
    }
  }

  /**
   * Must be last case in actor's receive, will manage subscribers through subscription, connection events.
   * @return
   */
  final def handleSubscriptionEvents: PartialFunction[Event, Unit] = {
    case Event(c, m @ Message(SystemChannels.CONNECTION, cnt, _)) if (cnt \ "event").asOpt[String].isDefined && (cnt \ "event").as[String] == "closed" => {
      val cid = (m.content \ "connectionId").as[Message.ConnectionId]
      _subscribers foreach { s =>
        _subscribers(s._1) = s._2.filterNot(_.id == cid)
        if (_subscribers(s._1).size == 0) {
          _subscribers -= s._1
          log.debug(s"connection '${c.id}' (closed) unsubscribed from $channelFilter")
        }
      }
    }
    case e @ Event(c, m @ Message(SystemChannels.SUBSCRIPTION, cnt, _)) if (cnt \ "subscribe").asOpt[String].isDefined => {
      val channel = (m.content \ "subscribe").as[String]
      // TODO check if already subscribed
      if (filterSubscriptions(channelFilter, e) && subscriberCheck(e)) {
        val conns = _subscribers.getOrElseUpdate(channel, mutable.HashSet())
        conns += c
        log.debug(s"connection '${c.id}' subscribed on $channelFilter")
        onSubscribe(e)
      }
    }
    case e @ Event(c, m @ Message(SystemChannels.SUBSCRIPTION, cnt, _)) if (cnt \ "unsubscribe").asOpt[String].isDefined => {
      val channel = (m.content \ "unsubscribe").as[String]
      if (channel == channelFilter) {
        val conns = _subscribers.get(channel)
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
  def subscribersSize = _subscribers.getOrElse(channelFilter, Set()).size

  private def filterSubscriptions(channel: String, e: Event): Boolean = {
    if (channel != channelFilter) false
    else if (_subscribers.contains(channel)) {
      !_subscribers(channel).contains(e.connection)
    }
    else true
  }
}
