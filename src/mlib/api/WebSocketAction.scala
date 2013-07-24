package mlib.api

import play.api.mvc.WebSocket
import play.api.libs.json.JsValue
import mlib.impl.ActionInternals

object WebSocketAction {
  def apply(idGenerator: => Message.ConnectionId) = {
    WebSocket.using[JsValue] { req =>
        val (en, conn) = ActionInternals.createEnumChannel(idGenerator, req.remoteAddress)
        val it = ActionInternals.createIt(conn)
        (it, en)
      }
  }
}
