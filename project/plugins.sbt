
resolvers += Resolver.url("sbt-plugin-releases",
  new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

resolvers += Classpaths.typesafeResolver

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.9.1")

// addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6")
