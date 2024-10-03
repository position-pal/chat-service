package io.github.positionpal.server.routes

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Flow


object Routes:
  
  def webSocketFlowRoute: Route = path("affirm"):
      handleWebSocketMessages:
        Flow[Message].collect:
          case TextMessage.Strict(text) => TextMessage(s"Received Text > ${text}")
  
  def defaultRoute: Route =
    path("hello") :
      get :
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
    
  
