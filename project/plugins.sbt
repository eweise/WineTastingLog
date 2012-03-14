logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % Option(System.getProperty("play.version")).getOrElse("2.0"))

resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

resolvers += "maven" at "http://repo.maven.apache.org/maven2/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.0.0")
