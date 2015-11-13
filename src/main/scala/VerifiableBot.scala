package com.osinka.play.legitbot

import scala.util.matching.Regex
import play.api.http.HeaderNames
import play.api.mvc.RequestHeader

import com.osinka.play.legitbot.BotMatches._

object VerifiableBot {
  def apply(name: String, pf: PartialFunction[RequestHeader, Boolean]) =
    new VerifiableBot {
      override def isDefinedAt(requestHeader: RequestHeader) = pf.isDefinedAt(requestHeader)
      override def apply(requestHeader: RequestHeader) = pf.apply(requestHeader)
      override val getBotName = name
    }
}

trait VerifiableBot extends PartialFunction[RequestHeader, Boolean] {
  def getBotName = {
    def removeScalaParts(s: String) = s.
      replaceAllLiterally("$$anonfun", ".").
      replaceAllLiterally("$apply", ".").
      replaceAllLiterally(".package", "").
      replaceAll("""\$\d*""", ".").
      split('.')

    removeScalaParts(getClass.getSimpleName).last
  }
}

trait UserAgentVerifiableBot extends VerifiableBot {
  def userAgent: Regex

  override def isDefinedAt(requestHeader: RequestHeader) =
    requestHeader.headers.get(HeaderNames.USER_AGENT).flatMap(userAgent.findFirstIn).isDefined
}

// https://support.google.com/webmasters/answer/1061943
// NOTE: NOT distinguishing between Video / Mobile / other variations
trait Googlebot extends VerifiableBot {
  override def apply(req: RequestHeader) =
    reverseForwardMatch("google.com", "googlebot.com")(req.remoteAddress).isDefined
}

object Googlebot extends Googlebot with UserAgentVerifiableBot {
  override val userAgent = """Googlebot""".r
}

object Google_AdSense extends Googlebot with UserAgentVerifiableBot {
  override val userAgent = """Mediapartners-Google""".r
}

object Google_AdsBot extends Googlebot with UserAgentVerifiableBot {
  override val userAgent = """AdsBot-Google""".r
}

// https://yandex.com/support/webmaster/robot-workings/check-yandex-robots.xml
trait Yandexbot extends VerifiableBot {
  override def apply(req: RequestHeader) =
    reverseForwardMatch("yandex.ru", "yandex.net", "yandex.com")(req.remoteAddress).isDefined
}

object Yandexbot extends Yandexbot with UserAgentVerifiableBot {
  override val userAgent = """YandexBot|YandexMobileBot|YandexImages|YandexVideo|YandexMedia|YandexBlogs|YandexFavicons|YandexWebmaster|YandexPagechecker|YandexImageResizer|YandexSitelinks|YandexMetrika""".r
}

object YandexDirect extends Yandexbot with UserAgentVerifiableBot {
  override val userAgent = """YandexDirectDyn|YandexRCA|YaDirectFetcher""".r
}

object YandexAntivirus extends Yandexbot with UserAgentVerifiableBot {
  override val userAgent = """YandexAntivirus""".r
}

object YandexOther extends Yandexbot with UserAgentVerifiableBot {
  override val userAgent = """YandexVertis|YandexCalendar""".r
}

// https://blogs.bing.com/webmaster/2012/08/31/how-to-verify-that-bingbot-is-bingbot/
object Bingbot extends UserAgentVerifiableBot {
  override val userAgent = """Bingbot|bingbot""".r
  override def apply(req: RequestHeader) =
    reverseForwardMatch("search.msn.com")(req.remoteAddress).isDefined
}

// http://help.baidu.com/question?prod_en=master&class=498&id=1000973
object Baiduspider extends UserAgentVerifiableBot {
  override val userAgent = """Baiduspider""".r
  override def apply(req: RequestHeader) =
    reverseMatch("baidu.com", "baidu.jp")(req.remoteAddress).isDefined
}

// https://duckduckgo.com/duckduckbot
object DuckDuckGo extends UserAgentVerifiableBot {
  val IPs = Seq("72.94.249.34", "72.94.249.35", "72.94.249.36", "72.94.249.37", "72.94.249.38")

  override val userAgent = """DuckDuckGo""".r
  override def apply(req: RequestHeader) = IPs.exists(req.remoteAddress.==)
}

// TODO: Facebook
// https://developers.facebook.com/docs/sharing/webmasters/crawler

// TODO: Twitter
// http://stackoverflow.com/questions/32484202/list-of-ip-addresses-used-by-twitterbot
