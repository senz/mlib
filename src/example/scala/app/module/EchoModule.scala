package module

import mlib.api._
import mlib.impl.ChannelEventsActor.Event
import Message.message2JsValue

class EchoModule extends Module {
  def receive = {
    case Event(connection, message @ Message("echo", _, _)) => connection.send(message)
  }
}
