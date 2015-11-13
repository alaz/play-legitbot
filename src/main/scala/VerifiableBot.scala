package com.osinka.play.legitbot

import java.net.InetAddress
import scala.util.control.Exception._
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
  val JavaxDnsEnv = {
    val env = new java.util.Hashtable[String, String]
    env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory")
    env
  }

  //
  // DNS via JNDI call
  // Credit: http://www.codingforums.com/java-and-jsp/182959-java-reverse-dns-lookup.html#post892349
  //
  // fallbacks to unreliable `InetAddress.getByName(ip).getCanonicalHostName` if unsuccessful
  //
  def reverseRecord(ip: String) = {
    import javax.naming.NamingException
    import javax.naming.directory._
    import scala.collection.convert.decorateAsScala._

    val context = new InitialDirContext(JavaxDnsEnv)
    val results =
      failAsValue(classOf[NamingException])(Iterator.empty).andFinally(context.close) {
        val octets = ip split """\."""
        val rev = octets.reverse.mkString(".") + ".in-addr.arpa"
        val attrs = context.getAttributes(rev, Array("PTR")).getAll.asScala

        attrs.filter(_.getID == "PTR").flatMap(_.getAll.asScala).map(_.toString).map { s =>
          if (s.endsWith(".")) s.dropRight(1)
          else s
        }
      }

    if (results.hasNext)
      Some(results.next())
    else {
      val addr = InetAddress.getByName(ip)
      val reverse = addr.getCanonicalHostName

      if (reverse == ip)
        None
      else
        Some(reverse)
    }
  }

  def matchesDomain(domains: String*)(record: String) = domains.exists(record.endsWith)

  def matchesForward(ip: String)(record: String) =
    failAsValue(classOf[java.net.UnknownHostException])(false) {
      InetAddress.getByName(record) == InetAddress.getByName(ip)
    }

  def reverseMatch(domains: String*)(ip: String) =
    reverseRecord(ip) filter matchesDomain(domains:_*)

  def reverseForwardMatch(domains: String*)(ip: String) =
    reverseMatch(domains:_*)(ip) filter matchesForward(ip)
}

// https://support.google.com/webmasters/answer/1061943
// NOTE: NOT distinguishing between Video / Mobile / other variations
trait Googlebot extends VerifiableBot {
  override def validate(req: RequestHeader) =
    VerifiableBot.reverseForwardMatch("google.com", "googlebot.com")(req.remoteAddress).isDefined
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
    VerifiableBot.reverseForwardMatch("yandex.ru", "yandex.net", "yandex.com")(req.remoteAddress).isDefined
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
    VerifiableBot.reverseForwardMatch("search.msn.com")(req.remoteAddress).isDefined
}

// http://help.baidu.com/question?prod_en=master&class=498&id=1000973
object Baiduspider extends VerifiableBot {
  override val userAgent = """Baiduspider""".r
  override def validate(req: RequestHeader) =
    VerifiableBot.reverseMatch("baidu.com", "baidu.jp")(req.remoteAddress).isDefined
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
