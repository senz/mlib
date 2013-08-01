package controllers
import play.api.mvc._
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue
import mlib.api._
import mlib.impl.{IdGeneratorIpTimeSalt, ConnectionJson}
import java.util.concurrent.atomic.AtomicLong

object Application extends Controller {
  private implicit val connectionFactory = new AppConnectionFactory

  def ws = WebSocketAction(IdGeneratorIpTimeSalt)

  def cometReceive(connectionId: Message.ConnectionId, data: String) = CometAction.input(connectionId, data)

  def cometSend(callback: String) = CometAction.output(callback, IdGeneratorIpTimeSalt)
}

class AppConnectionFactory extends mlib.api.ConnectionFactory {
  def create(channel: Concurrent.Channel[JsValue], connectionId: Message.ConnectionId, ip: String) =
    new ConnectionJson(channel, connectionId, ip) // traits with application-specific client state mixed in here (i.e.: auth data)
}
