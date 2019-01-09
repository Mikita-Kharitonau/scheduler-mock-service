package com.nd.schedulermockservice

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._

object Main extends App {

  val endpoints = new Endpoints("scheduler-mock-service")

  val api = endpoints.addReport :+: endpoints.getJob

  def service: Service[Request, Response] = Bootstrap
    .serve[Application.Json](api)
    .toService

  Await.ready(Http.server.serve(":8081", service))
}