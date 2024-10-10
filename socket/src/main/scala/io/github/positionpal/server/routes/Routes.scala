package io.github.positionpal.server.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Route
import io.github.positionpal.server.ws.WebSocketHandlers.websocketHandler
import io.github.positionpal.handler.Handler.{Commands, incomingHandler}

/**
 * Object that contains the routes definition for the websocket server
 */
object Routes:

  /**
   * Routes used for handling the websockett
   * @param system implicit system where the actor that handles connections are spawned
   * @return The route where the clients connect to the server and exchanges messages using websocket
   */
  def webSocketFlowRoute(using system: ActorSystem[?]): Route =
    val actorName = s"websocket-${java.util.UUID.randomUUID().toString}"
    val incomingActorReference = system.systemActorOf(incomingHandler, actorName)
    
    path("affirm"):
      handleWebSocketMessages:
        websocketHandler(incomingActorReference)

  /**
   * Default route for the server
   * @return The route
   */
  def defaultRoute: Route =
    path("hello") :
      get :
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
    
  
