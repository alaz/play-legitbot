package com.osinka.play.legitbot

import java.net.InetAddress
import scala.util.control.Exception._
import com.typesafe.config.ConfigFactory

object BotMatches {
  lazy val config = ConfigFactory.load.getConfig("legitbot")

  val JavaxDnsEnv = {
    val env = new java.util.Hashtable[String, String]
    env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory")
    env.put("java.naming.provider.url", config.getString("provider-url"))
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

