package io.github.positionpal.serializer

import akka.actor.ExtendedActorSystem
import akka.actor.typed.ActorRef
import io.bullet.borer.derivation.ArrayBasedCodecs.{deriveAllCodecs, deriveCodec}
import io.bullet.borer.{Codec, Decoder, Encoder}
import io.github.positionpal.borer.DefaultAkkaBorerSerializer
import io.github.positionpal.client.ClientADT.{ClientStatus, OutputReference}
import io.github.positionpal.client.{ClientID, ClientStatusHandler, CommunicationProtocol}
import io.github.positionpal.group.{GroupCommand, GroupEvent, GroupEventSourceHandler}

/** Serializer used for register object that should be used inside the entity of the system.
  * @param system The [[ExtendedActorSystem]]
  */
class AkkaSerializer(system: ExtendedActorSystem)
    extends DefaultAkkaBorerSerializer(system)
    with CommunicationSerializers:

  /* Client */
  given clientStatusHandlerCodec: Codec[ClientStatusHandler] = Codec(
    Encoder[ClientStatusHandler]: (writer, value) =>
      writer.writeArrayOpen(3).write(value.id).write(value.outputRef).write(value.status).writeArrayClose(),
    Decoder[ClientStatusHandler]: reader =>
      val unbounded = reader.readArrayOpen(3)
      val id = reader.read[ClientID]()
      val outputRef = reader.read[OutputReference[ActorRef[CommunicationProtocol]]]()
      val status = reader.read[ClientStatus]()
      val output = ClientStatusHandler(id, outputRef, status)
      reader.readArrayClose(unbounded, output),
  )

  register[ClientStatusHandler]()

  /* Group */
  given groupEventSourceStateCodec: Codec[GroupEventSourceHandler.State] = deriveCodec[GroupEventSourceHandler.State]
  given eventCommandCodec: Codec[GroupEvent] = deriveAllCodecs[GroupEvent]
  given groupCommandCodec: Codec[GroupCommand] = deriveAllCodecs[GroupCommand]

  register[GroupEventSourceHandler.State]()
  register[GroupCommand]()
  register[GroupEvent]()

  /* Client Communications */
  register[CommunicationProtocol]()
