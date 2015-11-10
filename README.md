Play Framework 2.3.x plugin to make sure a request pretending to be from a search bot
is really coming from that bot.

This filter will not call your controller if the request is malicious, thus reducing
unnecessary load. It returns 403 by default, but you may supply your own error handler.

# Installation

In SBT:

```
libararyDependencies += "com.osinka.play" %% "play-legitbot" % "1.0.0-SNAPSHOT"
```

Install the filter in `Global.scala`:

```
object Global extends WithFilters(LegitbotFilter)
```

# Other projects

* [webcrawler-verifier](https://github.com/optimaize/webcrawler-verifier)
