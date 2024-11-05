package io.github.positionpal.group

import io.github.positionpal.client.ClientID

private trait FormatCodes(value: String):
  infix def withClientId(id: ClientID): String = s"[$id]: $value"

enum ErrorValues(value: String) extends FormatCodes(value: String):
  case CLIENT_ALREADY_JOINED extends ErrorValues("client already joined")
  case CLIENT_DOESNT_BELONGS_TO_GROUP extends ErrorValues("client doesn't belongs to the group")

enum InformationValues(value: String) extends FormatCodes(value: String):
  case CLIENT_JOINED extends InformationValues("client joined the group")
  case CLIENT_LEAVED extends InformationValues("client leaved the group")
  case CLIENT_CONNECTED extends InformationValues("client connected to the group")
  case CLIENT_DISCONNECTED extends InformationValues("client disconnected from the group")
