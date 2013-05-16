package com.wordnik.swagger.sample
package model

import spray.json.DefaultJsonProtocol

case class Pet(id: Long, category: Category, name: String, urls: List[String], tags: List[Tag], status: String)
case class Tag(id: Long, name: String)
case class Category(id: Long, name: String)

case class ApiResponse(code: String, msg: String)

object ApiResponseType {
  val ERROR = "error"
  val WARNING = "warning"
  val INFO = "info"
  val OK = "ok"
  val TOO_BUSY = "too busy"
}

object PetJsonProtocol extends DefaultJsonProtocol {
  implicit val tagModel = jsonFormat(Tag, "id", "name")
  implicit val categoryModel = jsonFormat(Category, "id", "name")
  implicit val petModel = jsonFormat(Pet, "id", "category", "name", "urls", "tags", "status")

  implicit val responseModel = jsonFormat(ApiResponse, "code", "msg")
}
