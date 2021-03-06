libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v + "-0.2.11.1"))

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.2")

addSbtPlugin("cc.spray" % "sbt-revolver" % "0.6.1")

addSbtPlugin("cc.spray" % "sbt-twirl" % "0.5.4")

resolvers += "akka repo" at "http://repo.akka.io/releases/" // to be removed when upgrading to the next sbt-twirl version