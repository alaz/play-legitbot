package com.osinka.play.legitbot

import play.api.inject.{Binding, Module}
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.{Configuration, Environment}

class LegitbotModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] =
    Seq(
      bind[LegitbotSettings].to(Defaults)
    )
}

object Defaults extends LegitbotSettings {
  val errorHandler: RequestHeader => Result = _ => Results.Forbidden

  val bots = Seq(
    Googlebot, Google_AdsBot, Google_AdSense,
    Yandexbot, YandexDirect, YandexAntivirus, YandexOther,
    Bingbot,
    DuckDuckGo,
    Baiduspider
  )
}
