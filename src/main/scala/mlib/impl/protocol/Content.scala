package mlib.impl.protocol

import play.api.libs.json.Json
import mlib.api.Message

// ACHTUNG!BEWARE!ОСТОРОЖНО!: class fields name will be translated to and from json with format macro
// so watch for typos
// TODO make macro with field names as string literals

object Content {
  // system
  case class SystemError(code: Int, message: Option[String])
  implicit val SystemErrorFormat = Json.format[SystemError]

  case class ConnectionEvent(event: String, connectionId: Message.ConnectionId)
  implicit val ConnectionEventFormat = Json.format[ConnectionEvent]
}
