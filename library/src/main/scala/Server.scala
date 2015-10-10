package io.grpc
package scala

import io.grpc.netty.NettyServerBuilder
import io.grpc.internal.ServerImpl

object Server {
  def apply(port: Int, services: List[ServerServiceDefinition]): ServerImpl =
    services.foldLeft(NettyServerBuilder.forPort(port))(_.addService(_)).build()
}

