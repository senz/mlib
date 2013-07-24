package mlib.api

import play.api.Play.current
import play.api.libs.concurrent.Akka._
import akka.actor.Props
import mlib.impl.{MlibLogger, ConnectionJson, ConnectionActor}
import scala.concurrent.Future
import akka.util.Timeout
import mlib.api.Message._
import mlib.impl.ConnectionActor._
import play.api.Logger

/**
 * Connection management api. Layer between application's clients and modules.
 */
object Connections extends MlibLogger {
  def getConnection(id: Message.ConnectionId): Future[ConnectionJson] = Mlib.system.getConnection(id)

  /**
   * Registers new connection.
   * @param connection
   */
  def connected(connection: ConnectionJson) = Mlib.system.connected(connection)

  /**
   * Marks connection as closed, and frees resources.
   * @param connectionId
   */
  def disconnected(connectionId: ConnectionId) = Mlib.system.disconnected(connectionId)
}


