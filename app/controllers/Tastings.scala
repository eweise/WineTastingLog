package controllers


import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views._
import html.index
import models._
import java.io.File


object Tastings extends Controller {


  def tastings = {
    Action {
      implicit request =>
        request.session.get(User.USER_ID) match {
          case None => Redirect(routes.Application.login())
          case _ =>   Ok(html.tastings(Tasting.list(request.session.get(User.USER_ID).get.toInt))(request.session))
        }
    }
  }

  //  def upload = Action(parse.multipartFormData) { request =>
  //    val id = request.queryString.get("id").get.headOption.getOrElse("")
  //    request.body.file("picture").map { picture =>
  //      import java.io.File
  //      println("id = " + id)
  //      val contentType = picture.contentType
  //      picture.ref.moveTo(new File("image_ " + id.toString), true)
  //      Ok("File uploaded")
  //    }.getOrElse {
  //      Redirect(routes.Tastings.tastings).flashing(
  //        "error" -> "Missing file"
  //      )
  //    }
  //  }


  def upload() = Action(parse.multipartFormData) {
    implicit request =>
      val form = Form(tuple(
        "id" -> nonEmptyText,
        "image" -> ignored(request.body.file("image")).verifying("File missing", _.isDefined)))

      form.bindFromRequest.fold(formWithErrors => {
        Ok(html.tastings(Tasting.list(request.session.get(User.USER_ID).get.toInt))(request.session))
      },
        value => {
          request.body.file("image").map {
            file =>
              file.ref.moveTo(new File("public/images/user/image_" + value._1), true)
              Ok(html.tastings(Tasting.list(request.session.get(User.USER_ID).get.toInt))(request.session))
          }.getOrElse(BadRequest("File missing!"))
        })
  }


}