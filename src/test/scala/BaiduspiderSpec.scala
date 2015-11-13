package com.osinka.play.legitbot

import org.scalatest.{FunSpec, Matchers}
import play.api.http.HeaderNames
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeHeaders, FakeRequest}

class BaiduspiderSpec extends FunSpec with Matchers {
  describe("Baiduspider checker") {
    it("doesn't like reverse-forward match") {
      VerifiableBot.reverseForwardMatch(Seq("baidu.com"))("123.125.71.116") should equal(false)
    }
    it("likes reverse-only match") {
      VerifiableBot.reverseMatch(Seq("baidu.com"))("123.125.71.116") should equal(true)
    }
    it("recognizes as invalid") {
      val request = FakeRequest("GET", "/", FakeHeaders(Seq(HeaderNames.USER_AGENT -> Seq("""Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)"""))), AnyContentAsEmpty, "123.125.7.116")

      Baiduspider.userAgent.findFirstIn(request.headers(HeaderNames.USER_AGENT)) should be('defined)
      Baiduspider.validate(request) should equal(false)
    }
    it("recognizes as valid") {
      val request = FakeRequest("GET", "/", FakeHeaders(Seq(HeaderNames.USER_AGENT -> Seq("""Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)"""))), AnyContentAsEmpty, "123.125.71.116")

      Baiduspider.userAgent.findFirstIn(request.headers(HeaderNames.USER_AGENT)) should be('defined)
      Baiduspider.validate(request) should equal(true)
    }
  }
}
