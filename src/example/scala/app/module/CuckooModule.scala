package module

import mlib.api.{Message, Module}
import play.libs.Akka._
import mlib.impl.ChannelEventsActor.Event
import concurrent.duration._
import akka.actor.{ActorLogging, Cancellable}
import akka.event.LoggingReceive
import play.api.libs.json.Json
import mlib.impl.ModuleSubscribers
import protocol.Cuckoo._

/**
 * Example module that will tell you what time is it if asked, or you can subscribe on continuous notifications.
 *
 * Module mixes in ModuleSubscribers that helps track subscribers and ActorLogging facility from Akka that enables logging
 * of actor events.
 * Module trait is a marker interface needed for ModuleManagerComponent.
 */
class CuckooModule extends Module with ModuleSubscribers with ActorLogging {
  // channel on which clients may subscribe to receive async messages from this module
  val channelFilter = "cuckoo"
  private var event: Option[Cancellable] = None

  // Our timed action that makes cuckoo do what…well, what cuckoo's do.
  private val doBirdThingy = new Runnable {
    // Using broadcast to subscribers helper method
    def run() {broadcast(Json.toJson(Cuckoo())(CuckooFormat))}
  }

  // managing timer resources here
  override def preStart() = setScheduler()
  override def postStop() = unsetScheduler()

  // Those hooks called on successful subscribe and unsubscribe commands from clients
  override def onSubscribe(e: Event) = if (subscribersSize == 1) setScheduler()
  override def onUnsubscribe(e: Event) = if (subscribersSize == 0) unsetScheduler()

  // Module's business logic
  def receive = LoggingReceive {
    // Listening on any message on cuckoo channel, and replying with our special message
    case Event(c, Message(CUCKOO_CHANNEL, _, _)) => c.send(Json.toJson(Cuckoo())(CuckooFormat))
    // you must handle all other events through ModuleSubscriber's handler
    // this enables handling of subscription commands and connection tracking
    case m: Event => handleSubscriptionEvents(m)
  }

  // here you can impose additional check before allowing user to subscribe
  def subscriberCheck(e: Event) = true

  // Scheduler management
  import concurrent.ExecutionContext.Implicits.global
  private def setScheduler(): Unit = event = Some(system.scheduler.schedule(5 seconds, 5 seconds, doBirdThingy))
  private def unsetScheduler(): Unit = {
    if (event.isDefined && !event.get.isCancelled) {
      event.get.cancel()
      event = None
    }
  }
}

package protocol {

import play.api.libs.json.{Writes, JsString}
import org.joda.time.DateTime
import java.util.UUID

  // You can specify your type-safe protocol for modules with case classes and Json Format trait
  object Cuckoo {
    val CUCKOO_CHANNEL = "cuckoo"

    case class Cuckoo()
      extends Message {
      val channel = CUCKOO_CHANNEL
      val content = JsString(DateTime.now.toString)
      val msgId = UUID.randomUUID().toString
    }

    implicit object CuckooFormat extends Writes[Cuckoo] {
      def writes(o: Cuckoo) = Json.obj("channel" -> o.channel, "msgId" -> o.msgId, "content" -> o.content)
    }

  }
}
