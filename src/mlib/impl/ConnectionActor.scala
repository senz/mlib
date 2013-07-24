package mlib.impl

import mlib.api.Message._
import akka.actor.{ActorLogging, Actor}
import collection._
import mlib.api.ChannelEvent
import scala.concurrent.Promise
import protocol.FieldValues._
import protocol.{ApplicationMessage => Protocol}

class ConnectionActor extends Actor with ActorLogging {
  import ConnectionActor._
  import akka.actor.Status

  private val connectionPool = mutable.HashMap[ConnectionId, ConnectionJson]()

  def receive = {
    case c: NewConnection => {
      connectionPool += (c.connection.id -> c.connection)
      ChannelEvent.event(Promise.successful(c.connection).future, Protocol.ConnectionEvent(NEW, c.connection.id))
      log.debug("current pool: " + connectionPool.keySet.mkString(", "))
    }
    case c: CloseConnection => {
      val conn = connectionPool.get(c.id)
      connectionPool -= c.id

      if (conn.isDefined) {
        conn.get.close()
        ChannelEvent.event(Promise.successful(conn.get).future, Protocol.ConnectionEvent(CLOSED, c.id))
      } else log.error("closed connection is not in pool")
      log.debug("current pool: " + connectionPool.keySet.mkString(", "))
    }
    case GetConnection(id) => {
      val c = connectionPool.get(id)
      if (c.isDefined) {
        sender ! c.get
      } else sender ! Status.Failure(new Exception(s"No such connection $id"))
    }
  }
}

object ConnectionActor {
  case class GetConnection(id: ConnectionId)
  case class NewConnection(connection: ConnectionJson)
  case class CloseConnection(id: ConnectionId)
}
