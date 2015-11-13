package com.osinka.play.legitbot

import org.scalatest.{FunSpec, Matchers}
import play.api.http.HeaderNames
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeHeaders, FakeRequest}

class GooglebotSpec extends FunSpec with Matchers {
  describe("Googlebot check") {
    // 149.210.164.47 [25/Oct/2015:08:48:51 +0400] 200 "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
    it("recognizes 149.210.164.47 as malicious") {
      val request = FakeRequest("GET", "/", FakeHeaders(Seq(HeaderNames.USER_AGENT -> Seq("""Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"""))), AnyContentAsEmpty, "149.210.164.47")

      Googlebot.userAgent.findFirstIn(request.headers(HeaderNames.USER_AGENT)) should be('defined)
      Googlebot.validate(request) should equal(false)
    }

    // 66.249.78.6 [25/Oct/2015:00:58:32 +0400] 200 "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
    it("recognizes 66.249.78.6 as valid") {
      val request = FakeRequest("GET", "/", FakeHeaders(Seq(HeaderNames.USER_AGENT -> Seq("""Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"""))), AnyContentAsEmpty, "66.249.78.6")

      Googlebot.userAgent.findFirstIn(request.headers(HeaderNames.USER_AGENT)) should be('defined)
      Googlebot.validate(request) should equal(true)
    }
  }
}
