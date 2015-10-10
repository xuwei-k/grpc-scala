package examples

import io.grpc.internal.ManagedChannelImpl
import io.grpc.netty.{NettyChannelBuilder, NegotiationType}

object Channel {
  def apply(host: String, port: Int): ManagedChannelImpl =
    NettyChannelBuilder
      .forAddress(host, port)
      .negotiationType(NegotiationType.PLAINTEXT)
      .build()
}
