package io.github.positionpal.services

import io.github.positionpal.client.ClientID

/** Service that hande the socket connection */
trait SocketConnectionHandlerService[O]:
  /** Create a websocket connection to the server
    * @param clientID The id of the client that tries to connect to the websocket server
    * @param groupName The reference of the group
    * @return The object that represent the connection
    */
  def connect(clientID: ClientID, groupName: String): O
