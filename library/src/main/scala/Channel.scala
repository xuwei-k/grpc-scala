package io.grpc
package scala

import io.grpc.netty.{NegotiationType, NettyChannelBuilder}
import io.grpc.internal.ManagedChannelImpl

object Channel {
  def apply(host: String, port: Int): ManagedChannelImpl =
    NettyChannelBuilder
      .forAddress(host, port)
      .negotiationType(NegotiationType.PLAINTEXT)
      .build()
}
