//addSbtPlugin("com.github.gseitz" % "sbt-protobuf" % "0.4.0")

//resolvers += Resolver.mavenLocal

addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.14")

fullResolvers ~= {_.filterNot(_.name == "jcenter")} // https://github.com/sbt/sbt/issues/2217

//libraryDependencies += "com.github.os72" % "protoc-jar" % "3.0.0-b2-SNAPSHOT"
