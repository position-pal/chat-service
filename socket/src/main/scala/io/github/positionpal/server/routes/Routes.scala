package io.github.positionpal.server.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Flow
import io.github.positionpal.server.ws.Handlers


object Routes:
  
  def webSocketFlowRoute(using system: ActorSystem[?]): Route = path("affirm"):
      handleWebSocketMessages:
        Handlers.websocketHandler
  
  def defaultRoute: Route =
    path("hello") :
      get :
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
    
  
