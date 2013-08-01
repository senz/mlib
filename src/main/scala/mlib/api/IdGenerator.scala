package mlib.api

import play.api.mvc.RequestHeader

/**
 *
 */
trait IdGenerator {
  def generate(request: RequestHeader): Message.ConnectionId
}
