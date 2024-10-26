package io.github.positionpal.repository

import io.github.positionpal.entity.{EntityCommand, EntityID, EntityResponse}

/** Trait that represents a repository for a system's entity
  * @tparam F
  * @tparam ID
  * @tparam CMD
  * @tparam RES
  */
trait EntityRepository[F[_], ID <: EntityID, CMD <: EntityCommand[ID], RES <: EntityResponse]:
  def get(id: ID): F[Option[RES]]
  def store(cmd: CMD): F[RES]
  def delete(id: ID): F[Unit]
  def list: F[List[ID]]
