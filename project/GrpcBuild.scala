import sbt._
import Keys._

import com.trueaccord.scalapb.ScalaPbPlugin._

object ProjectBuild extends Build {

  val grpcVersion = "0.9.0"

  val myProtocSettings = {
    inConfig(protobufConfig)(Seq(
      runProtoc := { args =>
        val path = Attributed.data((fullClasspath in (forkProj, Compile)).value)
        forkRun(args.toList, path, streams.value.log)
      },
      version := "3.0.0-beta-1"
    ))
  }

  def mkProject(module: String, name: String) =
    Project(name, file(module)).settings(
      organization  := "io.grpc",
      scalaVersion  := "2.11.6",
      scalacOptions ++= Seq("-feature","-deprecation", "-Xlint")
    )

  lazy val library = mkProject("library", "grpc-scala").settings(
    libraryDependencies += "io.grpc" % "grpc-all" % grpcVersion,
    protobufSettings,
    myProtocSettings
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

  lazy val forkProj = mkProject(
    "fork", "fork"
  ).settings(
    resolvers += Resolver.mavenLocal,
    libraryDependencies += "com.github.os72" % "protoc-jar" % "3.0.0-b1"
      //libraryDependencies += "com.github.os72" % "protoc-jar" % "3.0.0-b2-SNAPSHOT"
  )

  def forkRun(args: List[String], path: Seq[File], log: Logger) = {
    println(s"runProtoc ${args.mkString(" ")}")
    new sbt.ForkRun(ForkOptions()).run(
      "com.github.os72.protocjar.Protoc",
      path,
      "-v300" :: args.toList,
      log
    ) match {
      case Some(e) => sys.error(e)
      case None => 0
    }
  }

  lazy val examples = mkProject("examples", "grpc-scala-examples").dependsOn(library)
    .settings(
      protobufSettings,
      myProtocSettings,
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
          val path = Attributed.data((fullClasspath in (forkProj, Compile)).value)
          forkRun(args.toList, path, streams.value.log)
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

