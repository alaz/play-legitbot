package com.osinka.play.legitbot

import scala.concurrent.Future
import org.slf4j.LoggerFactory
import play.api.http.HeaderNames
import play.api.mvc._

case class LegitbotFilter(errorHandler: RequestHeader => Result = Defaults.errorHandler, bots: Seq[VerifiableBot] = Defaults.bots) extends Filter {
  private val logger = LoggerFactory.getLogger(getClass)

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] =
    requestHeader.headers.get(HeaderNames.USER_AGENT) flatMap { userAgent =>
      val malicious =
        bots filter { _.isDefinedAt(requestHeader) } collectFirst {
          case bot if bot.apply(requestHeader) =>
            logger.debug(s"${requestHeader.remoteAddress} $requestHeader matches ${bot.getBotName}, and it's valid.")
            false
          case bot =>
            logger.info(s"${requestHeader.remoteAddress} $requestHeader pretends to be ${bot.getBotName}, but it's not.")
            true
        }

      if (malicious.exists(true.==)) Some(Future successful errorHandler(requestHeader))
      else None
    } getOrElse nextFilter(requestHeader)
}

object LegitbotFilter extends LegitbotFilter(Defaults.errorHandler, Defaults.bots)

object Defaults {
  val errorHandler: RequestHeader => Result = _ => Results.Forbidden

  val bots = Seq(
    Googlebot, Google_AdsBot, Google_AdSense,
    Yandexbot, YandexDirect, YandexAntivirus, YandexOther,
    Bingbot,
    DuckDuckGo,
    Baiduspider
  )
}
