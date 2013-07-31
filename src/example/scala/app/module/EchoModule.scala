package module

import mlib.api._
import mlib.impl.ChannelEventsActor.Event
import akka.event.LoggingReceive
import Message.message2JsValue

class EchoModule extends Module {
  def receive = LoggingReceive {
    // Handling any message on cuckoo channel, and sending it back to client
    case Event(connection, message @ Message("echo", _, _)) => connection.send(message)
  }
}
