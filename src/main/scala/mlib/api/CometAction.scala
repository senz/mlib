package mlib.api

import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json.{Json, JsValue}
import mlib.impl.ActionInternals
import play.api.libs.iteratee.{Enumerator, Input}
import scala.concurrent.Promise
import mlib.impl.protocol.{ApplicationMessage => Protocol}
import mlib.impl.protocol.FieldValues._
import play.api.libs.Comet

object CometAction {
  def input(connectionId: Message.ConnectionId, data: String) = Action { req=>
    Async {
      val conn = Connections.getConnection(connectionId)
      val it = ActionInternals.createIt(conn)
      it.feed(Input.El(Json.parse(data)))
      Promise.successful(Ok("ok")).future
    }
  }

  def output(callback: String, idGenerator: IdGenerator)(implicit f: ConnectionFactory) = Action { req =>
    val id = idGenerator.generate(req)
    val (en, _) = ActionInternals.createEnumChannel(id, req.remoteAddress)
    Ok.stream(Enumerator[JsValue](Json.toJson(Protocol.ConnectionEvent(NEW, id))(Protocol.ConnectionEventFormat))
      >>> en &> Comet(callback = callback))
  }
}


