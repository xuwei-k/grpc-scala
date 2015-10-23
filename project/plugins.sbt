//addSbtPlugin("com.github.gseitz" % "sbt-protobuf" % "0.4.0")

//resolvers += Resolver.mavenLocal

addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.14")

val plugins = Project("plugins", file(".")).dependsOn(
  ProjectRef(uri("git://github.com/xuwei-k/ScalaPB.git#5e3917113d4ae45c49a862f99d56189d102020e6"), "compilerPlugin")
)

fullResolvers ~= {_.filterNot(_.name == "jcenter")} // https://github.com/sbt/sbt/issues/2217

//libraryDependencies += "com.github.os72" % "protoc-jar" % "3.0.0-b2-SNAPSHOT"
