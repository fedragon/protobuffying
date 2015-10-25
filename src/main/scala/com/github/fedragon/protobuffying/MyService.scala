package com.github.fedragon.protobuffying

import akka.actor.Actor
import spray.routing._
import spray.http._
import spray.httpx._
import spray.httpx.marshalling._
import spray.httpx.unmarshalling._
import MediaTypes._

import protocol.Protocol._

class MyServiceActor extends Actor with MyService {

  def actorRefFactory = context

  def receive = runRoute(myRoute)
}

trait MyService extends HttpService {
  import ProtobufProtocols._

  val myRoute =
    path("sessions") {
      get {
        respondWithMediaType(ProtobufMediaType) {
          complete {
            val builder =
              Session.
                newBuilder().
                setId(1000).
                setStart("2015-10-24T17:44:00Z").
                setEnd("2015-10-24T18:11:00Z")

            builder.build()
          }
        }
      } ~
      post {
        entity(as[Session]) { session =>
          println(s"received session $session")
          complete(session.toString)
        }
      }
    }
}

object ProtobufProtocols {
  val ProtobufMediaType = MediaTypes.register(MediaType.custom("application", "x-protobuf", compressible = false, binary = true))
  val Protobuf = ContentType(ProtobufMediaType)

  implicit val sessionMarshaller: Marshaller[Session] =
    Marshaller.of(ProtobufMediaType) {
      (value, contentType, ctx) ⇒
        ctx.marshalTo(HttpEntity(contentType, value.toByteArray))
    }

  implicit val sessionUnmarshaller: Unmarshaller[Session] =
    Unmarshaller[Session](ProtobufMediaType) {
      case HttpEntity.NonEmpty(contentType, data) =>
        Session.parseFrom(data.toByteArray)
    }
}
