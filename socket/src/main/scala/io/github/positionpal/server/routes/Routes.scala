package io.github.positionpal.server.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Route
import io.github.positionpal.server.ws.WebSocketHandlers.websocketHandler
import io.github.positionpal.entity.Handler.{Commands, incomingHandler}


object Routes:
  
  def webSocketFlowRoute(using system: ActorSystem[?]): Route =
    val actorName = s"websocket-${java.util.UUID.randomUUID().toString}"
    val incomingActorReference = system.systemActorOf(incomingHandler, actorName)
    
    path("affirm"):
      handleWebSocketMessages:
        websocketHandler(incomingActorReference)
  
  def defaultRoute: Route =
    path("hello") :
      get :
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
    
  
