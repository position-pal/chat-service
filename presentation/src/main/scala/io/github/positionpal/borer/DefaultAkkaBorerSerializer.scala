package io.github.positionpal.borer

import akka.actor.ExtendedActorSystem
import akka.actor.typed.scaladsl.adapter.*
import akka.actor.typed.{ActorRef, ActorRefResolver}
import io.bullet.borer.{Codec, Decoder, Encoder}

/** Class that refine the [[Serializer]] implementing all the codec that uses the [[ExtendedActorSystem]] as
  * support for the conversion operations
  * @param system The [[ExtendedActorSystem]]
  */
class DefaultAkkaBorerSerializer(system: ExtendedActorSystem) extends CborAkkaSerializer with ModelCodecs:
  override def identifier: Int = 10001

  private val actorRefResolver = ActorRefResolver(system.toTyped)

  given actorRefCodec[T]: Codec[ActorRef[T]] = Codec[ActorRef[T]](
    Encoder[ActorRef[T]]: (writer, outputActorRef) =>
      val serializedRef = actorRefResolver.toSerializationFormat(outputActorRef)
      writer.writeString(serializedRef)
    ,
    Decoder[ActorRef[T]]: reader =>
      val serializedRef = reader.readString()
      actorRefResolver.resolveActorRef(serializedRef).asInstanceOf[ActorRef[T]],
  )
