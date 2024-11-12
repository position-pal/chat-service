package io.github.positionpal.group

import io.github.positionpal.group.GroupADT.{ErrorCode, GroupOps}

object GroupDSL:
  extension [I, O](group: GroupOps[I, O])
    /** Add a client to the group
      * @param client A Pair that consist of an identifier and the client
      * @return an [[Either]] object containing an error code of the operation or the updated group
      */
    def +(client: (I, O)): Either[ErrorCode[I], GroupOps[I, O]] =
      group.addClient(client._1, client._2)

    /** Remove a clients from the group
      * @param id The identifier of the client
      * @return an [[Either]] object containing an error code of the operation or the updated group
      */
    def -(id: I): Either[ErrorCode[I], GroupOps[I, O]] =
      group.removeClient(id)

    /** Return a client reference that is currently inside the group
      * @param id the id of the client
      * @return An [[Either]] object indicating an error for the retrieval or the actual reference
      */
    def ?(id: I): Either[ErrorCode[I], O] =
      group.getClient(id)

    /** Update a client inside the group
      * @param id the id of the client
      * @param f the update function for the
      * @return
      */
    infix def ~>(id: I)(f: O => O): Either[ErrorCode[I], GroupOps[I, O]] =
      group.updateClient(id)(f)

    /** Executes a function on a client if it exists in the group
      * @param id The client identifier
      * @param f The function that is executed if the client exists
      * @tparam R The expected return value type
      * @return An [[Either]] object with an error occurred or the result of the function
      */
    def |>[R](id: I)(f: O => R): Either[ErrorCode[I], R] =
      group.executeOnClient(id)(f)

    /** Execute an action on a client if it belongs to the group and a specific condition is met
      * @param id        The client identifier
      * @param condition A function that takes a client as input and return a Boolean
      * @param ok        The function that is executed if the condition returns true
      * @param ko        The function that is executed if the condition returns false
      * @tparam R The expected return value type
      * @return An [[Either]] object with an error occurred or the result of the function
      */
    def ?>[R](id: I)(cond: O => Boolean)(ifTrue: O => R)(ifFalse: O => R): Either[ErrorCode[I], R] =
      group.executeOnCondition(id)(cond)(ifTrue, ifFalse)

    /** Execute an action on the output reference for a client
      * @param f The function that must be executed for each client
      */
    def |>>(f: O => Unit): Unit =
      group.executeOnClients(f)
