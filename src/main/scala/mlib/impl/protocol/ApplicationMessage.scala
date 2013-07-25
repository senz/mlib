package mlib.impl.protocol

import mlib.api.Message
import play.api.libs.json._
import mlib.api.{SystemChannels => Chan}
import mlib.impl.protocol.MessageFields._

object ApplicationMessage {
  import java.util.UUID
  abstract class Bridge(val channel: Message.ChannelType, val content: JsValue, val msgId: String = UUID.randomUUID().toString)
    extends Message

  // input
  case class Untyped(channel: Message.ChannelType, content: JsValue, msgId: String = UUID.randomUUID().toString)
    extends Message
  val UntypedFormat = Json.format[Untyped]

  // system
  case class SystemError(code: Int, message: Option[String])
    extends Bridge(Chan.SYSTEM, Json.toJson(Content.SystemError(code, message)))
  implicit object SystemErrorFormat extends Writes[SystemError] {
    def writes(o: SystemError) = Json.obj(
      CHANNEL -> o.channel,
      MSG_ID -> o.msgId,
      CONTENT -> Json.obj(
        "code" -> o.code,
        "message" -> o.message
      )
    )
  }

  case class ConnectionEvent(event: String, connectionId: Long)
    extends Bridge(Chan.CONNECTION, Json.toJson(Content.ConnectionEvent(event, connectionId)))
  implicit object ConnectionEventFormat extends Writes[ConnectionEvent] {
    def writes(o: ConnectionEvent) = Json.obj(
      CHANNEL -> o.channel,
      MSG_ID -> o.msgId,
      CONTENT -> Json.obj(
        "event" -> o.event,
        "connectionId" -> o.connectionId
      )
    )
  }
}
