package com.github.fedragon.protobuffying

import akka.actor._
import scala.concurrent.Future
import spray.http._
import spray.httpx.unmarshalling._
import spray.client.pipelining._

import protocol.Protocol._

object Client extends App {
  import ProtobufProtocols._
  import Deserializer._

  implicit val system = ActorSystem()
  import system.dispatcher

  val getPipeline: HttpRequest => Future[Session] = (
    addHeader("Protobuf", "x-protobuf")
      ~> sendReceive
      ~> unmarshal[Session]
    )

  val postPipeline: HttpRequest => Future[HttpResponse] = sendReceive

  val session =
      Session.
        newBuilder().
        setId(1000).
        setStart("2015-10-24T17:44:00Z").
        setEnd("2015-10-24T18:11:00Z").build

  postPipeline(Post("http://localhost:8080/sessions", session))

  getPipeline(Get("http://localhost:8080/sessions")).foreach(println)
}
