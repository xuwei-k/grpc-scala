import sbt._
import Keys._

import com.trueaccord.scalapb.ScalaPbPlugin._

object ProjectBuild extends Build {

  val grpcVersion = "0.9.0"

  def mkProject(module: String, name: String) =
    Project(name, file(module)).settings(
      organization  := "io.grpc",
      scalaVersion  := "2.11.6",
      scalacOptions ++= Seq("-feature","-deprecation", "-Xlint"),
      protobufSettings
    ).settings(inConfig(protobufConfig)(Seq(
      runProtoc := synchronized{ args =>
        println(s"runProtoc ${args.mkString(" ")}")
        com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray)
      },
      version := "3.0.0-beta-1"
    )))

  lazy val library = mkProject("library", "grpc-scala").settings(
    libraryDependencies += "io.grpc" % "grpc-all" % grpcVersion
  ).settings(inConfig(protobufConfig)(Seq(
    javaConversions := true
  )))

  lazy val examples = mkProject("examples", "grpc-scala-examples").dependsOn(library)
    .settings(
      /*
      (managedSources in Compile) ++= {
        ((sourceManaged in Compile).value ** "*.java").get
      },
*/
      (generatedTargets in protobufConfig) += {
        ((javaSource in protobufConfig).value, "*.java")
      },
      includePaths in protobufConfig += { 
        (sourceDirectory in (library, protobufConfig)).value
      },
      protocOptions in protobufConfig ++= Seq(
        s"--plugin=protoc-gen-java_rpc=protoc-gen-grpc-java-${grpcVersion}.exe",
        s"--java_rpc_out=${(sourceManaged in Compile).value}/compiled_protobuf"
      )
    )
}

