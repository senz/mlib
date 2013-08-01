package mlib.impl

import org.joda.time.DateTime
import mlib.api.IdGenerator
import play.api.Play._
import play.api.mvc.RequestHeader
import play.api.libs.Codecs

/**
 * Creates unique client id based on host address, current time and secret key.
 */
object IdGeneratorIpTimeSalt extends IdGenerator {
  private val secret = configuration.getString("application.secret").getOrElse(sys.error("application.secret is not set"))

  def generate(req: RequestHeader) = {
    val now = DateTime.now.getMillis
    Codecs.sha1(now + req.remoteAddress + secret)
  }
}
