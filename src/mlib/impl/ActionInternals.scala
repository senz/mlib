package mlib.impl

import concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.{Json, JsValue}
import scala.concurrent.{Promise, Future}
import play.api.libs.iteratee.{Input, Concurrent, Iteratee}
import scala.Some

import protocol.{ApplicationMessage => Protocol}
import Protocol._

import components.{ResultCode => Result}
import components.ResultCode.ResultCode2Int
import mlib.api._

protected[mlib] object ActionInternals extends MlibLogger {
  def createIt(conn: Future[ConnectionJson]) = {
    val it = Iteratee.foreach[JsValue] { json =>
      try {
        val messageF = Json.fromJson[Protocol.Untyped](json)(UntypedFormat)
        val message = messageF.asOpt
        if (message.isDefined) ChannelEvent.event(conn, message.get)
        else conn.foreach(_.send(
          Json.toJson(Protocol.SystemError(Result.PARSE_ERROR, Some(s"could not parse json: ${messageF.toString}")))(SystemErrorFormat)
        ))
      } catch {
        case e: Exception => {
          log.error("exception in iteratee", e)
          conn.foreach(_.send(
            Json.toJson(Protocol.SystemError(Result.INTERNAL_ERROR, None))(SystemErrorFormat)
          ))
        }
      }
    } map { v =>
      // freeing connection resources on enumerator close, to support comet (iterator will be one-off).
      conn.foreach{ conn => Connections.disconnected(conn.id) }
    }
    it
  }

  def createEnumChannel(connectionId: Message.ConnectionId, ip: String) = {
    val chPromise = Promise[Concurrent.Channel[JsValue]]()
    val channel = chPromise.future
    val conPromise = Promise[ConnectionJson]()

    channel foreach { channel =>
      val conn = new ConnectionJson(channel, connectionId, ip)
      conPromise.success(conn)
      Connections.connected(conn)
    }

    val en = Concurrent.unicast[JsValue](
      onStart = (ch) => {
        chPromise.success(new Concurrent.Channel[JsValue] {
          def push(chunk: Input[JsValue]) {
            log.debug(s"outgoing($connectionId) - $chunk")
            ch.push(chunk)
          }

          def end(e: Throwable) {ch.end(e)}

          def end() {ch.end()}
        })
      },
      onError = (error, input) => log.error(s"enumerator.onError for id $connectionId: $error")
    )

    (en, conPromise.future)
  }
}
