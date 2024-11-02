package io.github.positionpal.borer

import java.time.Instant

import io.bullet.borer.derivation.ArrayBasedCodecs.deriveCodec
import io.bullet.borer.{Codec, Decoder, Encoder}
import io.github.positionpal.client.ClientADT.{ClientStatus, OutputReference}
import io.github.positionpal.client.ClientID
import io.github.positionpal.message.ChatMessageADT.ChatMessage

/** Here are implemented the default [[Codec]]s used for serializing object inside the system */
trait ModelCodecs:

  given instantCodec: Codec[Instant] = Codec.bimap(_.toString, Instant.parse)

  given clientIdCodec: Codec[ClientID] = deriveCodec[ClientID]
  given clientStatusCodec: Codec[ClientStatus] = deriveCodec[ClientStatus]
  given clientOutputReferenceCodec[O: Encoder: Decoder]: Codec[OutputReference[O]] =
    Codec(
      Encoder[OutputReference[O]]: (writer, outputReference) =>
        outputReference match
          case OutputReference.OUT(value) =>
            writer.writeArrayOpen(2).writeString("type").writeString("OUT").writeString("value").write(value)
              .writeArrayClose()

          case OutputReference.EMPTY =>
            writer.writeArrayOpen(2).writeString("type").writeString("EMPTY").writeString("value").writeString("")
              .writeArrayClose()
      ,
      Decoder[OutputReference[O]]: reader =>
        val unbounded = reader.readArrayOpen(2)
        reader.readString()
        val output = reader.readString() match
          case "OUT" =>
            reader.readString()
            val value = reader.read[O]()
            OutputReference.OUT(value)
          case "EMPTY" =>
            reader.readString()
            reader.readString()
            OutputReference.EMPTY
        reader.readArrayClose(unbounded, output),
    )

  given chatMessageCodec[I: Encoder: Decoder, T: Encoder: Decoder]: Codec[ChatMessage[I, T]] =
    deriveCodec[ChatMessage[I, T]]
