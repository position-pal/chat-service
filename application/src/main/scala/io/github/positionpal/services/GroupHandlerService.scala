package io.github.positionpal.services

trait GroupHandlerService[GID, CID, F[_]]:
  def joinGroup(groupId: GID, clientId: CID): F[Unit]
  def leaveGroup(groupId: GID, clientId: CID): F[Unit]
