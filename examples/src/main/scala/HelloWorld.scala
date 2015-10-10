package io.grpc.scala
package examples

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.google.common.util.concurrent.{FutureCallback, Futures}
import io.grpc.examples.helloworld.{GreeterGrpc, HelloRequest, HelloResponse}
import io.grpc.stub.StreamObserver

import scala.concurrent.Await
import scala.concurrent.duration._

object HelloWorldClient {
  def main(args: Array[String]) {
    val n = args.headOption.fold(1)(_.toInt)
    val channel = Channel("localhost", 50051)
    val stub = GreeterGrpc.newFutureStub(channel)
    val request = HelloRequest.newBuilder().setName(args.headOption.getOrElse("Giskard")).build()

    Await.result(stub.sayHello(request).asFuture, 3.seconds)

    val latch = new CountDownLatch(n)
    val failures = new AtomicInteger()

    val start = System.currentTimeMillis

    var i = 0
    while(i < n){
      Futures.addCallback(stub.sayHello(request), new FutureCallback[HelloResponse] {
        override def onFailure(t: Throwable): Unit = {
          failures.incrementAndGet()
          latch.countDown()
        }
        override def onSuccess(result: HelloResponse): Unit = {
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
    channel.shutdown()
    channel.awaitTerminated(10, TimeUnit.SECONDS)
  }
}

object HelloWorldServer {
  object Greeter extends GreeterGrpc.Greeter {
    override def sayHello(request: HelloRequest, observer: StreamObserver[HelloResponse]) {
      val response = HelloResponse.newBuilder().setMessage(s"Hello ${request.getName}").build()
      observer.onValue(response)
      observer.onCompleted
    }
  }

  def main(args: Array[String]) {
    val server = Server(50051, List(GreeterGrpc.bindService(Greeter)))
    server.start
  }
}
