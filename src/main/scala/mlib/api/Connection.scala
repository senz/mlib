package mlib.api

import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue

/**
 * Shareable entity for async communicating with client.
 * @tparam A channel type
 */
trait Connection[A] {
  /**
   * Connection id, unique to application instance.
   */
  val id: Message.ConnectionId
  /**
   * Client host address.
   */
  val ip: String

  /**
   * Enumerator channel used for sending data to client.
   */
  protected val messageChannel: Concurrent.Channel[A]
  private var closed = false

  /**
   * Send message to client.
   * @param message
   */
  def send(message: A): Unit = if (!closed) messageChannel.push(message)

  /**
   * Indicates connection liveness.
   * @return
   */
  def isClosed = closed

  /**
   * Close connection.
   */
  private[mlib] def close() {
    if (!closed) messageChannel.eofAndEnd()
    closed = true
  }
}
