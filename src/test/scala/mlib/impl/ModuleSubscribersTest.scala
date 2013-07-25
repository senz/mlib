package test.mlib.impl
import org.junit._
import play.api.test._
import play.api.test.Helpers._
import mlib.api.{ModuleSystem, Mlib, Module, SystemChannels}

import org.mockito.Mockito
import org.mockito.Mockito._
import mlib.impl.ChannelEventsActor.Event
import protocol.ApplicationMessage.{ConnectionEvent, Untyped}
import play.api.libs.json.Json

import akka.testkit.TestActorRef
import akka.actor.{ActorRef, Props}

class ModuleSubscribersTest {
  var conn: Option[ConnectionJson] = None
  var module: Option[ModuleSubcribersStub] = None
  var app: Option[FakeApplication] = None
  var moduleSystem: Option[ModuleSystem] = None

  val chan = "test"

  @Before
  def before() {
    app = Some(getApp)
    conn = Some(mock(classOf[ConnectionJson]))
  }

  private def setModule() {
    implicit val sys = play.api.libs.concurrent.Akka.system(app.get)
    val moduleSystem = mock(classOf[ModuleSystem])
    this.moduleSystem = Some(moduleSystem)
    Mlib._setSystem(moduleSystem)
    module = Some(spy(TestActorRef(Props[ModuleSubcribersStub], "mstub").underlyingActor))
  }

  @Test
  def receive_subscriptionSubscribe_subscriberCheckCalled() {
    running(app.get) {
      setModule()
      val event = Event(conn.get, Untyped(SystemChannels.SUBSCRIPTION, Json.obj("subscribe" -> chan)))

      module.get.receive(event)
      verify(module.get).subscriberCheck(event)
    }
  }

  @Test
  def receive_subscriptionSubscribe_1Subscriber() {
    running(app.get) {
      setModule()
      val event = Event(conn.get, Untyped(SystemChannels.SUBSCRIPTION, Json.obj("subscribe" -> chan)))

      module.get.receive(event)
      Assert.assertEquals(1, module.get.subscribersSize)
    }
  }

  @Test
  def receive_subscriptionSubscribeAndDisconnect_noSubscribers() {
    running(app.get) {
      setModule()
      val subscribeEvent = Event(conn.get, Untyped(SystemChannels.SUBSCRIPTION, Json.obj("subscribe" -> chan)))
      val connId = 1L
      when(conn.get.id).thenReturn(connId)
      val connectionCloseEvent = Event(conn.get, ConnectionEvent("closed", connId))

      module.get.receive(subscribeEvent)
      module.get.receive(connectionCloseEvent)

      Assert.assertEquals(0, module.get.subscribersSize)
    }
  }

  @Test
  def receive_subscriptionSubscribeTrueCheck_onSubscribeCalled() {
    running(app.get) {
      setModule()
      val event = Event(conn.get, Untyped(SystemChannels.SUBSCRIPTION, Json.obj("subscribe" -> chan)))
      when(module.get.subscriberCheck(event)).thenReturn(true)

      module.get.receive(event)
      verify(module.get).onSubscribe(event)
    }
  }

  @Test
  def receive_subscriptionSubscribeFalseCheck_onSubscribeNotCalled() {
    running(app.get) {
      setModule()
      val event = Event(conn.get, Untyped(SystemChannels.SUBSCRIPTION, Json.obj("subscribe" -> chan)))
      when(module.get.subscriberCheck(event)).thenReturn(false)

      module.get.receive(event)
      verify(module.get, never()).onSubscribe(event)
    }
  }

  @Test
  def receive_subscriptionDoubleSubscribe_onSubscribeCalledOnce() {
    running(app.get) {
      setModule()
      val event = Event(conn.get, Untyped(SystemChannels.SUBSCRIPTION, Json.obj("subscribe" -> chan)))
      when(module.get.subscriberCheck(event)).thenReturn(true)

      module.get.receive(event)
      module.get.receive(event)

      verify(module.get, atMost(1)).onSubscribe(event)
    }
  }

  @Test
  def receive_subscriptionUnsubscribeAndNotSubscribed_onUnsubscribeNotCalled() {
    running(app.get) {
      setModule()
      val event = Event(conn.get, Untyped(SystemChannels.SUBSCRIPTION, Json.obj("unsubscribe" -> chan)))
      when(module.get.subscriberCheck(event)).thenReturn(true)

      module.get.receive(event)

      verify(module.get, never()).onUnsubscribe(event)
    }
  }

  @Test
  def receive_subscriptionDoubleSubscribe_size1() {
    running(app.get) {
      setModule()
      val event = Event(conn.get, Untyped(SystemChannels.SUBSCRIPTION, Json.obj("subscribe" -> chan)))
      when(module.get.subscriberCheck(event)).thenReturn(true)

      module.get.receive(event)
      module.get.receive(event)

      Assert.assertEquals(1, module.get.subscribersSize)
      verify(module.get, never()).onUnsubscribe(event)
    }
  }

  @Test
  def receive_subscriptionUnsubscribeAndSubscribed_onUnsubscribeCalled() {
    running(app.get) {
      setModule()
      val subscribe = Event(conn.get, Untyped(SystemChannels.SUBSCRIPTION, Json.obj("subscribe" -> chan)))
      when(module.get.subscriberCheck(subscribe)).thenReturn(true)
      module.get.receive(subscribe)

      val unsubscribe = Event(conn.get, Untyped(SystemChannels.SUBSCRIPTION, Json.obj("unsubscribe" -> chan)))
      when(module.get.subscriberCheck(unsubscribe)).thenReturn(true)
      module.get.receive(unsubscribe)

      verify(module.get).onUnsubscribe(unsubscribe)
    }
  }

  private def getApp = FakeApplication()
}

class ModuleSubcribersStub extends Module with ModuleSubscribers {
  val channelFilter = "test"

  def receive = {
    case e: ChannelEventsActor.Event => handleSubscriptionEvents(e)
  }

  /**
   * Here actor can filter subscriptions.
   * @param event for checking
   * @return
   */
  def subscriberCheck(event: Event) = true

  /**
   * Subscription hook, called if subscription was successful.
   * @param event
   */
  override def onSubscribe(event: Event) {}
}
