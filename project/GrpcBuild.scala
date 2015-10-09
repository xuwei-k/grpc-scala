import sbt._
import Keys._

import sbtprotobuf.ProtobufPlugin._

object ProjectBuild extends Build {

  val grpcVersion = "0.7.1"

  def mkProject(module: String, name: String) =
    Project(name, file(module), settings = Seq(
      organization  := "io.grpc",
      // TODO Enable after first stable release
      // version       := (s"git describe --long --tags --match v${grpcVersion}" !!).trim.drop(1),
      scalaVersion  := "2.11.6",
      scalacOptions ++= Seq("-feature","-deprecation", "-Xlint")
    ))

  lazy val root = Project("root", file("."))
    .aggregate(library, examples)

  lazy val library = mkProject("library", "grpc-scala")
    .settings(libraryDependencies += "io.grpc" % "grpc-all" % grpcVersion)

  lazy val examples = mkProject("examples", "grpc-scala-examples").dependsOn(library)
    .settings(protobufSettings:_*)
    .settings(
      version in protobufConfig := "3.0.0-alpha-3.1",
      runProtoc in protobufConfig := { args =>
        println(s"runProtoc ${args.mkString(" ")}")
        com.github.os72.protocjar.Protoc.runProtoc(args.toArray)
        0
      },
      protocOptions in protobufConfig ++= Seq(
        s"--plugin=protoc-gen-java_rpc=protoc-gen-grpc-java-${grpcVersion}.exe",
        "--java_rpc_out=examples/target/scala-2.11/src_managed/main/compiled_protobuf"
      )
    )
}

