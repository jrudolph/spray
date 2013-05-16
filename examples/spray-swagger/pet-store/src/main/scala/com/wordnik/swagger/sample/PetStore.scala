package com.wordnik.swagger.sample

import data.PetData
import model.ApiResponse
import model.ApiResponse
import model.ApiResponse
import model.ApiResponse
import model.Pet
import model.Pet
import model.Pet
import model.Pet
import model.{Pet, ApiResponseType, ApiResponse}
import spray.routing._
import akka.actor.{ActorLogging, ActorSystem}
import spray.routing.directives.BasicDirectives._
import spray.httpx.SprayJsonSupport._
import spray.http._
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpResponse
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpResponse
import spray.http.HttpHeaders.RawHeader
import annotation.{StaticAnnotation, Annotation}

case class APIInfo(
  description: String
) extends StaticAnnotation
case class ParamInfo(
  description: String
) extends Annotation with StaticAnnotation

object PetStore extends App with SimpleRoutingApp {
  implicit val system = ActorSystem("pet-store")

  import model.PetJsonProtocol._
  val data = new PetData

  startServer("localhost", port = 8085) {
    handleRejections(myHandler) {
      allowCrossDomain {
        options { complete("") } ~
        pathPrefix("pet") {
          get {
            complete(data.pets): @APIInfo(description = "List all pets")
          } ~
          post {
            entity(as[Pet]) { pet =>
              system.log.info(s"Added pet '${pet.name}'")
              complete(ApiResponse(ApiResponseType.OK, s"pet '${pet.name}' added to store")):
                @APIInfo(description = "Add pet to store")
            }
          } ~
          put {
            entity(as[Pet]) { pet =>
              system.log.info(s"Updated pet '${pet.name}'")
              complete(ApiResponse(ApiResponseType.OK, s"pet '${pet.name}' updated")):
                @APIInfo(description = "Update pet in store")
            }
          } ~
          path(IntNumber) { (petId: Int @ParamInfo("ID of pet to fetch")) =>
            rejectEmptyResponse {
              get {
                complete(data.getPetbyId(petId)): @APIInfo(description = "Fetch one pet")
              }
            }
          } ~
          path("api-docs.json") {
            getFromResource("web/api-docs.json")
          }
        }
      }
    }
  }

  def oldRh = implicitly[RejectionHandler]

  def myHandler = RejectionHandler.fromPF {
    case x => allowCrossDomain { oldRh(x) }
  }

  def allowCrossDomain =
    respondWithHeaders(
      RawHeader("Access-Control-Allow-Origin", "*"),
      RawHeader("Access-Control-Allow-Headers", "Content-Type"),
      RawHeader("Access-Control-Allow-Methods", "GET, PUT, POST"))
}
