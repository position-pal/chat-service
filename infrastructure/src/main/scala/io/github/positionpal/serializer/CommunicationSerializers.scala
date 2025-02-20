package io.github.positionpal.serializer

import io.bullet.borer.Codec
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveAllCodecs
import io.github.positionpal.borer.ModelCodecs
import io.github.positionpal.client.CommunicationProtocol

trait CommunicationSerializers extends ModelCodecs:
  given communicationProtocolCodec: Codec[CommunicationProtocol] = deriveAllCodecs[CommunicationProtocol]

object CommunicationSerializersImporter extends CommunicationSerializers
