package io.github.positionpal.utils

trait ExternalRefOps:
  def containedString: String
  def executeCommand(command: String => String): ExternalRefOps

case class StringContainer(containedString: String) extends ExternalRefOps:
  override def executeCommand(command: String => String): ExternalRefOps = StringContainer(command(containedString))
