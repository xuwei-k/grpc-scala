grpc scala sample

mac

```
wget http://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/0.9.0/protoc-gen-grpc-java-0.9.0-osx-x86_64.exe &&
mv protoc-gen-grpc-java-0.9.0-osx-x86_64.exe protoc-gen-grpc-java-0.9.0.exe &&
chmod +x protoc-gen-grpc-java-0.9.0.exe
```

(another OS <http://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/0.9.0/>)



run server

```
sbt "grpc-scala-examples/runMain io.grpc.scala.examples.HelloWorldServer"
```


run client

```
sbt "grpc-scala-examples/runMain io.grpc.scala.examples.HelloWorldClient"
```
