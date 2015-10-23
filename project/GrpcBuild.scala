import sbt._
import Keys._

import com.trueaccord.scalapb.ScalaPbPlugin._

object ProjectBuild extends Build {

  val grpcVersion = "0.9.0"

  val myProtocSettings = {
    inConfig(protobufConfig)(Seq(
      (managedClasspath in protobufConfig) := Classpaths.managedJars(Compile, classpathTypes.value, update.value),
      runProtoc := { args =>
        val path = Attributed.data((fullClasspath in (forkProj, Compile)).value)
        forkRun(args.toList, path, streams.value.log)
      },
      //scalapbVersion := "0.5.14",
      version := "3.0.0-beta-1"
    ))
  }

  def mkProject(module: String) =
    Project(module, file(module)).settings(
      organization  := "io.grpc",
      scalaVersion  := "2.11.6",
      fullResolvers ~= {_.filterNot(_.name == "jcenter")}, // https://github.com/sbt/sbt/issues/2217
      scalacOptions ++= Seq("-feature","-deprecation", "-Xlint")
    )

  lazy val library = mkProject("library").settings(
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
    "fork"
  ).settings(
    resolvers += Resolver.mavenLocal,
    libraryDependencies += "com.github.os72" % "protoc-jar" % "3.0.0-b1"
//    libraryDependencies += "com.github.os72" % "protoc-jar" % "3.0.0-b2-SNAPSHOT"
  )

  def forkRun(args: List[String], path: Seq[File], log: Logger) = IO.withTemporaryDirectory{ dir =>
    println(s"runProtoc ${args.mkString(" ")}")
    val opt = ForkOptions(
      workingDirectory = Some(dir),
      runJVMOptions = scala.sys.process.javaVmArguments
    )
    new sbt.ForkRun(opt).run(
      "com.github.os72.protocjar.Protoc",
      path,
      "-v300" :: args.toList,
      log
    ) match {
      case Some(e) => sys.error(e)
      case None => 0
    }
  }

  private val grpcCodeDir = SettingKey[File]("grpc_compiled_protobuf")

  lazy val examples: Project = mkProject("examples").dependsOn(library)
    .settings(
      sbtprotobuf.ProtobufPlugin.protobufSettings,
      myProtocSettings,
      grpcCodeDir := {
        (sourceManaged in Compile).value / "grpc_code"
      },
      (sbtprotobuf.ProtobufPlugin.runProtoc in protobufConfig) := { args0 =>
        IO.withTemporaryDirectory{ dir =>
          val exeURL = grpcExe()
          val exeName = exeURL.split('/').last
          val exe = dir / exeName
          // TODO cache file
          IO.download(url(exeURL), exe)
          exe.setExecutable(true)
          val args = args0 ++ Array(
            s"--plugin=protoc-gen-java_rpc=${exe.getAbsolutePath}",
            s"--java_rpc_out=${((sourceManaged in Compile).value / "compiled_protobuf").getAbsolutePath}"
          )
          val path = Attributed.data((fullClasspath in (forkProj, Compile)).value)
          forkRun(args.toList, path, streams.value.log)
        }
      },
      includePaths in protobufConfig += {
        (sourceDirectory in (library, protobufConfig)).value
      }
    )
}

