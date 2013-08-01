package mlib.api

import play.api.mvc.WebSocket
import play.api.libs.json.JsValue
import mlib.impl.ActionInternals
import play.api.libs.iteratee.{Enumerator, Iteratee}

object WebSocketAction {
  def apply(idGenerator: IdGenerator)(implicit f: ConnectionFactory) = {
    WebSocket.using[JsValue] { req =>
        val (en, conn) = ActionInternals.createEnumChannel(idGenerator.generate(req), req.remoteAddress)
        val it = ActionInternals.createIt(conn)
        (it, en)
      }
  }
}
