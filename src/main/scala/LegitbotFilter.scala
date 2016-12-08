package com.osinka.play.legitbot

import javax.inject.Inject
import scala.concurrent.Future
import play.api.mvc._
import org.slf4j.LoggerFactory

class LegitbotFilter @Inject() (settings: LegitbotSettings) extends Filter {
  private val logger = LoggerFactory.getLogger(getClass)

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val malicious =
      settings.bots filter { _.isDefinedAt(requestHeader) } collectFirst {
        case bot if bot.apply(requestHeader) =>
          logger.debug(s"${requestHeader.remoteAddress} $requestHeader matches ${bot.getBotName}, and it's valid.")
          false
        case bot =>
          logger.info(s"${requestHeader.remoteAddress} $requestHeader pretends to be ${bot.getBotName}, but it's not.")
          true
      }

    if (malicious.exists(true.==)) Future successful settings.errorHandler(requestHeader)
    else nextFilter(requestHeader)
  }
}

trait LegitbotSettings {
  val errorHandler: RequestHeader => Result
  val bots: Seq[VerifiableBot]
}

