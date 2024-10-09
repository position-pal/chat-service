package io.github.positionpal.server.ws

import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{CompletionStrategy, OverflowStrategy}
import io.github.positionpal.entity.Handler.{Commands, WebSocketHandler}
import io.github.positionpal.message.ChatMessageADT.ChatMessage

object WebSocketHandlers:

  def websocketHandler(incomingActorRef: ActorRef[Commands]): Flow[Message, Message, WebSocketHandler] =

    import io.github.positionpal.entity.Handler.Commands.{
      IncomingMessage,
      OutgoingMessage,
      StreamCompletedSuccessfully,
      StreamCompletedWithException
    }
    import io.github.positionpal.entity.Handler.{Commands, WebSocketHandler}

    val incomingMessage: Sink[Message, NotUsed] =
      Flow[Message].map:
        case TextMessage.Strict(text) => IncomingMessage(text)
        case _ => StreamCompletedWithException(Exception("Not supported message"))
      .to:
        ActorSink.actorRef(
          incomingActorRef,
          onCompleteMessage = StreamCompletedSuccessfully,
          onFailureMessage = ex => StreamCompletedWithException(ex)
        )

    val outgoingMessage: Source[Message, ActorRef[Commands]] =
      ActorSource.actorRef[Commands](
        completionMatcher = {
          case StreamCompletedSuccessfully => CompletionStrategy.draining
        },
        failureMatcher = {
          case StreamCompletedWithException(ex: Throwable) => ex
        },
        bufferSize = 8,
        overflowStrategy = OverflowStrategy.fail
      )
      .map:
        (protocolMessage: Commands) =>
          protocolMessage match
            case OutgoingMessage(content: ChatMessage) => TextMessage.Strict(content.text)
            case _ => TextMessage.Strict("Error")
    
    Flow.fromSinkAndSourceMat(incomingMessage, outgoingMessage) :
      case (_, outgoingActorRef) =>
        WebSocketHandler(outgoingActorRef.narrow[Commands.OutgoingMessage])
    



