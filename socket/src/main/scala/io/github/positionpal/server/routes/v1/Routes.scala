package io.github.positionpal.server.routes.v1

import scala.concurrent.{ExecutionContextExecutor, Future}

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import io.github.positionpal.client.{ClientID, CommunicationProtocol}
import io.github.positionpal.server.routes.RoutesProvider
import io.github.positionpal.server.ws.v1.WebSocketHandlers
import io.github.positionpal.services.GroupHandlerService

/** Object that contains the routes definition for the websocket server */
object Routes:

  private case class ApplicationV1Routes(
      system: ActorSystem[?],
      service: GroupHandlerService[Future, CommunicationProtocol],
  ) extends RoutesProvider:

    override def routes: Route = webSocketFlowRoute
    override def version: String = "v1"

    given executionContext: ExecutionContextExecutor = system.executionContext

    /** Routes used for handling the websocket
      * @return The route where the clients connect to the server and exchanges messages using websocket
      */
    private def webSocketFlowRoute: Route =
      pathPrefix("messages" / Segment): groupID =>
        parameter("user"): idString =>
          handleWebSocketMessages(WebSocketHandlers.connect(ClientID(idString), groupID, service))

  /** Return the routes for the v1 api version
    * @param system the implicit actor system
    * @return A [[Route]] object containing the routes of the server.
    */
  def v1Routes(using system: ActorSystem[?], service: GroupHandlerService[Future, CommunicationProtocol]): Route =
    ApplicationV1Routes(system, service).versionedRoutes
