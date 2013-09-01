package mlib.impl

import org.junit._
import play.api.test._
import play.api.test.Helpers._

class IdGeneratorIpTimeSaltTest {
  val secret = "secret"
  val app = new FakeApplication(additionalConfiguration = Map("application.secret" -> secret))

  @Test
  def generate_1callConstTimeAndIp_equalsExpected() {
    val remoteAddress = "1.1.1.2"

    val req = FakeRequest("GET", "/test").
      copy(remoteAddress = remoteAddress)
    running(app) {
      val out = IdGeneratorIpTimeSalt
      out.timeSource = () => 1
      val res = out.generate(req)
      val expected = "403492f7a5dceaf57e18106c1b692f99ed8df519" // sha1(11.1.1.2secret)
      Assert.assertEquals(expected, res)
    }
  }

  @Test
  def generate_2callsIpAndTimeChanged_callsResultsNotEqual() {
    var remoteAddress = "1.1.1.1"
    var time = 2

    val req = FakeRequest("GET", "/test").
      copy(remoteAddress = remoteAddress)

    running(app) {
      val out = IdGeneratorIpTimeSalt
      out.timeSource = () => time
      val res1 = out.generate(req)

      time = 1
      remoteAddress = "1.2.2.2"
      val res2 = out.generate(req.copy(remoteAddress = remoteAddress))
      Assert.assertNotSame(res1, res2)
    }
  }
}
