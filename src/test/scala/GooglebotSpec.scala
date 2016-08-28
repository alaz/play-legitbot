package com.osinka.play.legitbot

import play.api.http.HeaderNames
import play.api.test.FakeRequest
import org.scalatest.{FunSpec, Matchers}

class GooglebotSpec extends FunSpec with Matchers {
  describe("Googlebot check") {
    // 149.210.164.47 [25/Oct/2015:08:48:51 +0400] 200 "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
    it("recognizes 149.210.164.47 as malicious") {
      val request = FakeRequest("GET", "/").withHeaders(HeaderNames.USER_AGENT -> """Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)""").copy(remoteAddress = "149.210.164.47")

      Googlebot.isDefinedAt(request) should equal(true)
      Googlebot.apply(request) should equal(false)
    }

    // 66.249.78.6 [25/Oct/2015:00:58:32 +0400] 200 "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
    it("recognizes 66.249.78.6 as valid") {
      val request = FakeRequest("GET", "/").withHeaders(HeaderNames.USER_AGENT -> """Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)""").copy(remoteAddress = "66.249.78.6")

      Googlebot.isDefinedAt(request) should equal(true)
      Googlebot.apply(request) should equal(true)
    }
  }
}
