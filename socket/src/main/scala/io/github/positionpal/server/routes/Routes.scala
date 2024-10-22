package io.github.positionpal.server.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import io.github.positionpal.server.ws.WebSocketHandlers

/** Object that contains the routes definition for the websocket server */
object Routes:

  private case class ApplicationV1Routes(system: ActorSystem[?]) extends RoutesProvider:
    override def routes: Route = webSocketFlowRoute
    override def version: String = "v1"

    /** Routes used for handling the websocket
      * @return The route where the clients connect to the server and exchanges messages using websocket
      */
    private def webSocketFlowRoute: Route =
      // val actorName = s"websocket-${java.util.UUID.randomUUID().toString}"
      // val incomingActorReference = system.systemActorOf(incomingHandler, actorName)
      pathPrefix("messages" / Segment): groupId =>
        parameter("user"): userId =>
          handleWebSocketMessages(WebSocketHandlers.testingWsHandler)

  /** Return the routes for the v1 api version
    * @param system the implicit actor system
    * @return A [[Route]] object containing the routes of the server.
    */
  def v1Routes(using system: ActorSystem[?]): Route = ApplicationV1Routes(system).versionedRoutes
