package controllers

import play.api.mvc.Controller
import java.util.concurrent.atomic.AtomicLong
import mlib.api._
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue
import mlib.impl.ConnectionJson

object Default extends Controller {
  private implicit val connectionFactory = new ConnectionFactory
  private val connectionId = new AtomicLong(0)

  def connect() = WebSocketAction(generateConnectionId())

  def cometReceive(connectionId: Message.ConnectionId, data: String) = CometAction.input(connectionId, data)

  def cometSend(callback: String) = CometAction.output(callback, generateConnectionId())

  private def generateConnectionId() = connectionId.incrementAndGet()
}

class ConnectionFactory extends mlib.api.ConnectionFactory {
  def create(channel: Concurrent.Channel[JsValue], connectionId: Message.ConnectionId, ip: String) =
    new ConnectionJson(channel, connectionId, ip) // traits with application-specific client state mixed in here (i.e.: auth data)
}
