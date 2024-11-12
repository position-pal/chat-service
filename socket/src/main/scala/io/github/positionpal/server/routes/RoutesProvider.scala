package io.github.positionpal.server.routes

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route

/** Trait representing a RoutesProvider: an entity that contains the versioned endpoints for a webserver */
trait RoutesProvider:
  /** A [[String]] representing the version of the routes */
  def version: String

  /** Return the routes for the following provider */
  def routes: Route

  /** Return the versioned routes for the following provider */
  def versionedRoutes: Route =
    pathPrefix(version):
      routes
