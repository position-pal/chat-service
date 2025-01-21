package io.github.positionpal.grpc

import com.google.protobuf.timestamp.Timestamp
import io.github.positionpal.client.ClientID
import io.github.positionpal.message.ChatMessageADT.Message
import io.github.positionpal.proto.Message as ProtoMessage

object Conversions:

  given messageProtoConversion: Conversion[Message[ClientID, String], ProtoMessage] = msg =>
    val protoTimestamp = Timestamp(msg.timestamp.getEpochSecond, msg.timestamp.getNano)
    ProtoMessage(msg.from.value, msg.text, Option(protoTimestamp))