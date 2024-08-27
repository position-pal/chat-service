package messages

import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import akka.serialization.jackson.CborSerializable

/** Input command for the Group entity */
enum Command extends CborSerializable:
  case UserEnterToGroup(user: String, replyTo: ActorRef[StatusReply[?]])
  case UserLeaveFromGroup(user: String, replyTo: ActorRef[StatusReply[?]])

/** Event triggered inside the Group entity */
enum Event:
  case UserEnteredToGroupEvent(user: String)

class Group():
  /** Current State maintained on the entity
    * @param users the users that are currently in the group
    */
  class State(users: Seq[String]) extends CborSerializable:
    def addUser(user: String) = State(user +: users)
    def removeUser(user: String) = State(users.filterNot(_ == user))

  private object State:
    def empty: State = State(Seq.empty)
