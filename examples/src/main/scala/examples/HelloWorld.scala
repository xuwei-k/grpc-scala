package examples

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.google.common.util.concurrent.{FutureCallback, Futures}
import helloworld.{HelloWorld, GreeterGrpc}
import helloworld.hello_world.{HelloResponse, HelloRequest}
import io.grpc.stub.StreamObserver

import scala.concurrent.Await
import scala.concurrent.duration._

object HelloWorldClient {
  def main(args: Array[String]) {
    val n = args.headOption.fold(1)(_.toInt)
    val channel = Channel("localhost", 50051)
    val stub = GreeterGrpc.newFutureStub(channel)
    val request = HelloRequest.toJavaProto(HelloRequest(name = args.headOption.getOrElse("Giskard")))

    Await.result(stub.sayHello(request).asFuture, 3.seconds)

    val latch = new CountDownLatch(n)
    val failures = new AtomicInteger()

    val start = System.currentTimeMillis

    var i = 0
    while(i < n){
      Futures.addCallback(stub.sayHello(request), new FutureCallback[helloworld.HelloWorld.HelloResponse] {
        override def onFailure(t: Throwable): Unit = {
          failures.incrementAndGet()
          latch.countDown()
        }
        override def onSuccess(result: helloworld.HelloWorld.HelloResponse): Unit = {
          latch.countDown()
        }
      })
      i += 1
    }

    latch.await(5000, TimeUnit.MILLISECONDS)
    val time = System.currentTimeMillis() - start
    println("failures " + failures.get)
    println(time / 1000.0)
    println((n / (time / 1000.0)) + " QPS")
    channel.shutdown().awaitTermination(10, TimeUnit.SECONDS)
  }
}

object HelloWorldServer {
  object Greeter extends GreeterGrpc.Greeter {
    override def sayHello(request: HelloWorld.HelloRequest, observer: StreamObserver[HelloWorld.HelloResponse]): Unit = {
      val response = HelloResponse.toJavaProto(HelloResponse(message = s"Hello ${request.getName}"))
      observer.onNext(response)
      observer.onCompleted()
    }
  }

  def main(args: Array[String]) {
    val server = Server(50051, List(GreeterGrpc.bindService(Greeter)))
    server.start
  }
}
