package mlib.api

import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue
import mlib.impl.ConnectionJson

trait ConnectionFactory {
  def create(channel: Concurrent.Channel[JsValue], connectionId: Message.ConnectionId, ip: String): ConnectionJson
}
