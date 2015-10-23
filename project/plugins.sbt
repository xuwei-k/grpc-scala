//addSbtPlugin("com.github.gseitz" % "sbt-protobuf" % "0.4.0")

//resolvers += Resolver.mavenLocal

addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.14")

val plugins = Project("plugins", file(".")).dependsOn(
  ProjectRef(uri("git://github.com/xuwei-k/ScalaPB.git#bb1098aaef64e8bd5d95d9f59bf019195d9349e8"), "compilerPlugin")
)

fullResolvers ~= {_.filterNot(_.name == "jcenter")} // https://github.com/sbt/sbt/issues/2217

//libraryDependencies += "com.github.os72" % "protoc-jar" % "3.0.0-b2-SNAPSHOT"
