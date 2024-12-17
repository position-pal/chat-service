package io.github.positionpal.server.routes

import scala.concurrent.ExecutionContextExecutor

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import io.github.positionpal.client.ClientID
import io.github.positionpal.server.Server.actorSystem
import io.github.positionpal.server.ws.WebSocketHandlers
import io.github.positionpal.service.GroupService
import io.github.positionpal.services.GroupHandlerService

/** Object that contains the routes definition for the websocket server */
object Routes:

  private case class ApplicationV1Routes(system: ActorSystem[?]) extends RoutesProvider:
    override def routes: Route = webSocketFlowRoute ~ joinRoute
    override def version: String = "v1"

    given executionContext: ExecutionContextExecutor = actorSystem.executionContext
    given service: GroupHandlerService = GroupService(actorSystem)

    /** Routes used for handling the websocket
      * @return The route where the clients connect to the server and exchanges messages using websocket
      */
    private def webSocketFlowRoute: Route =
      pathPrefix("messages" / Segment): groupID =>
        parameter("user"): clientID =>
          handleWebSocketMessages(WebSocketHandlers.connect(ClientID(clientID), groupID))

    private def joinRoute: Route =
      pathPrefix("join" / Segment): groupID =>
        parameter("user"): clientID =>
          post:
            service.join(groupID)(ClientID(clientID))
            complete(StatusCodes.OK, "nice")

  /** Return the routes for the v1 api version
    * @param system the implicit actor system
    * @return A [[Route]] object containing the routes of the server.
    */
  def v1Routes(using system: ActorSystem[?]): Route = ApplicationV1Routes(system).versionedRoutes
