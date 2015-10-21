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
      runProtoc := { args =>
        println(s"runProtoc ${args.mkString(" ")}")
        synchronized {
          com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray)
        }
      },
      version := "3.0.0-beta-1"
    )))

  lazy val library = mkProject("library", "grpc-scala").settings(
    libraryDependencies += "io.grpc" % "grpc-all" % grpcVersion
  ).settings(inConfig(protobufConfig)(Seq(
    javaConversions := true
  )))

  private[this] def grpcExe() = {
    val os = if(scala.util.Properties.isMac){
      "osx-x86_64"
    }else if(scala.util.Properties.isWin){
      "windows-x86_64"
    }else{
      "linux-x86_64"
    }
    val artifactId = "protoc-gen-grpc-java"
    s"http://repo1.maven.org/maven2/io/grpc/${artifactId}/${grpcVersion}/${artifactId}-${grpcVersion}-${os}.exe"
  }

  lazy val examples = mkProject("examples", "grpc-scala-examples").dependsOn(library)
    .settings(
      (runProtoc in protobufConfig) := { args0 =>
        IO.withTemporaryDirectory{ dir =>
          val exeURL = grpcExe()
          val exeName = exeURL.split('/').last
          val exe = dir / exeName
          // TODO cache file
          IO.download(url(exeURL), exe)
          exe.setExecutable(true)
          val args = args0 ++ Array(
            s"--plugin=protoc-gen-java_rpc=${exe.getAbsolutePath}",
            s"--java_rpc_out=${(sourceManaged in Compile).value}/compiled_protobuf"
          )
          println(s"runProtoc ${args.mkString(" ")}")
          synchronized{
            com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray)
          }
        }
      },
      (generatedTargets in protobufConfig) += {
        ((javaSource in protobufConfig).value, "*.java")
      },
      includePaths in protobufConfig += { 
        (sourceDirectory in (library, protobufConfig)).value
      }
    )
}

