package com.osinka.play.legitbot

import java.net.InetAddress
import scala.util.matching.Regex
import play.api.mvc.RequestHeader

trait VerifiableBot {
  val userAgent: Regex

  def validate(req: RequestHeader): Boolean

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

object VerifiableBot {
  def domainMatch(domains: Seq[String])(ip: String) = {
    val addr = InetAddress.getByName(ip)
    val reverse = addr.getCanonicalHostName
    if (domains.exists(reverse.endsWith)) {
      val forward = InetAddress.getByName(reverse)
      addr == forward
    } else
      false
  }
}

// https://support.google.com/webmasters/answer/1061943
// NOTE: NOT distinguishing between Video / Mobile / other variations
trait Googlebot extends VerifiableBot {
  override def validate(req: RequestHeader) =
    VerifiableBot.domainMatch(Seq("google.com", "googlebot.com"))(req.remoteAddress)
}

object Googlebot extends Googlebot {
  override val userAgent = """Googlebot""".r
}

object Google_AdSense extends Googlebot {
  override val userAgent = """Mediapartners-Google""".r
}

object Google_AdsBot extends Googlebot {
  override val userAgent = """AdsBot-Google""".r
}

// https://yandex.com/support/webmaster/robot-workings/check-yandex-robots.xml
trait Yandexbot extends VerifiableBot {
  override def validate(req: RequestHeader) =
    VerifiableBot.domainMatch(Seq("yandex.ru", "yandex.net", "yandex.com"))(req.remoteAddress)
}

object Yandexbot extends Yandexbot {
  override val userAgent = """YandexBot|YandexMobileBot|YandexImages|YandexVideo|YandexMedia|YandexBlogs|YandexFavicons|YandexWebmaster|YandexPagechecker|YandexImageResizer|YandexSitelinks|YandexMetrika""".r
}

object YandexDirect extends Yandexbot {
  override val userAgent = """YandexDirectDyn|YandexRCA|YaDirectFetcher""".r
}

object YandexAntivirus extends Yandexbot {
  override val userAgent = """YandexAntivirus""".r
}

object YandexOther extends Yandexbot {
  override val userAgent = """YandexVertis|YandexCalendar""".r
}

// https://blogs.bing.com/webmaster/2012/08/31/how-to-verify-that-bingbot-is-bingbot/
object Bingbot extends VerifiableBot {
  override val userAgent = """Bingbot|bingbot""".r
  override def validate(req: RequestHeader) =
    VerifiableBot.domainMatch(Seq("search.msn.com"))(req.remoteAddress)
}

// http://help.baidu.com/question?prod_en=master&class=498&id=1000973
object Baiduspider extends VerifiableBot {
  override val userAgent = """Baiduspider""".r
  override def validate(req: RequestHeader) =
    VerifiableBot.domainMatch(Seq("baidu.com", "baidu.jp"))(req.remoteAddress)
}

// https://duckduckgo.com/duckduckbot
object DuckDuckGo extends VerifiableBot {
  val IPs = Seq("72.94.249.34", "72.94.249.35", "72.94.249.36", "72.94.249.37", "72.94.249.38")

  override val userAgent = """DuckDuckGo""".r
  override def validate(req: RequestHeader) = IPs.exists(req.remoteAddress.==)
}

// TODO: Facebook
// https://developers.facebook.com/docs/sharing/webmasters/crawler

// TODO: Twitter
// http://stackoverflow.com/questions/32484202/list-of-ip-addresses-used-by-twitterbot
