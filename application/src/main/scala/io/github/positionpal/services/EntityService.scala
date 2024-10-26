package io.github.positionpal.services

import io.github.positionpal.entity.{EntityCommand, EntityID, EntityResponse}

trait EntityService[F[_], ID <: EntityID, CMD <: EntityCommand[ID], RES <: EntityResponse]:
  def process(cmd: CMD): F[RES]
  def find(id: ID): F[Option[RES]]
  def remove(id: ID): F[Unit]
  def getAll: F[List[ID]]
