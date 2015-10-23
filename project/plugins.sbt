//addSbtPlugin("com.github.gseitz" % "sbt-protobuf" % "0.4.0")

//resolvers += Resolver.mavenLocal

addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.14")

val plugins = Project("plugins", file(".")).dependsOn(
  ProjectRef(uri("git://github.com/xuwei-k/ScalaPB.git#af6bb7ddef1c225edf59f6d36dba7190999cdf6a"), "compilerPlugin")
)

fullResolvers ~= {_.filterNot(_.name == "jcenter")} // https://github.com/sbt/sbt/issues/2217

//libraryDependencies += "com.github.os72" % "protoc-jar" % "3.0.0-b2-SNAPSHOT"
