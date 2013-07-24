package mlib.impl

import mlib.api.{Message, Connection}
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{Json, JsValue}
import module.{FriendState, AuthState}

class ConnectionJson(channel: Concurrent.Channel[JsValue], connectionId: Message.ConnectionId, val ip: String)
  extends Connection[JsValue] with AuthState with FriendState {
  val id = connectionId
  protected val messageChannel = channel
}
