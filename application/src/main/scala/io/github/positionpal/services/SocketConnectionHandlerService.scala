package io.github.positionpal.services

/** Service that hande the socket connection */
trait SocketConnectionHandlerService[I, C]:
  /** Create a websocket connection to the server
    * @param clientID The id of the client that tries to connect to the websocket server
    * @param groupName The reference of the group
    * @return The object that represent the connection
    */
  def connect(clientID: I, groupName: String): C
