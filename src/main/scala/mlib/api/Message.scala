package mlib.api

import play.api.libs.json.JsValue
import Message._

// TODO make content generic
trait Message {
  val channel: ChannelType
  val content: JsValue
  val msgId: MsgId
}
object Message {
  type ChannelType = String
  type ConnectionId = Long
  type MsgId = String

  def unapply(m: Message): Option[(ChannelType, JsValue, MsgId)] = Some(m.channel, m.content, m.msgId)
}
