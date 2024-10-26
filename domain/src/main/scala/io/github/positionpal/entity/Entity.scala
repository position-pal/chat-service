package io.github.positionpal.entity

trait EntityID:
  def value: String

trait EntityCommand[ID <: EntityID]:
  def id: ID

trait EntityResponse

trait ServiceError:
  def message: String
