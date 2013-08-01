package mlib.api

import play.api.libs.json.{Json, JsValue}
import Message._

// TODO make content generic
trait Message {
  val channel: ChannelType
  val content: JsValue
  val msgId: MsgId
}
object Message {
  type ChannelType = String
  type ConnectionId = String
  type MsgId = String

  def unapply(m: Message): Option[(ChannelType, JsValue, MsgId)] = Some(m.channel, m.content, m.msgId)

  implicit def message2JsValue(message: Message): JsValue =
    Json.obj("channel" -> message.channel, "msgId" -> message.msgId, "content" -> message.content)
}
