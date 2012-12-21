package controllers

import play.api.mvc.{Action, Controller}
import play.api.data.Form
import models.{S3File, Image, User, Tasting}
import play.api.data.Forms._
import anorm.{Pk, NotAssigned}
import views.html
import java.io.File
import play.Logger
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart

object EditTasting extends Controller {

  val tastingForm: Form[Tasting] = Form(
    mapping(
      "id" -> ignored(NotAssigned: Pk[Long]),
      "userId" -> optional(longNumber),
      "rating" -> optional(number),
      "notes" -> optional(text),
      "brand" -> optional(text),
      "style" -> optional(text),
      "region" -> optional(text),
      "year" -> optional(number),
      "filename" -> optional(text),
      "updateDate" -> optional(date)
    )(Tasting.apply)(Tasting.unapply)
  )

  def delete(id: Long) = Action {
    Tasting.delete(id)
    Redirect(routes.Tastings.tastings)
  }

  def edit(id: Long) = Action {
    implicit request =>
      Ok(html.editTastings(Some(id), tastingForm.fill(Tasting.load(id).get)))
  }

  def newTasting() = Action {
    implicit request =>
      Ok(html.editTastings(None, tastingForm))
  }

  def saveNew = Action(parse.maxLength(300000, parse.multipartFormData)) {
    implicit request =>
      var tastingId = 0L
      val userId = Some(request.session.get(User.USER_ID).get.toLong)
      tastingForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.editTastings(None, formWithErrors)),
        tasting => {
          tasting.userId = userId
          tastingId = Tasting.insert(tasting)
        }
      )
      request.body match {
        case Left(error) => EntityTooLarge("Request to big")
        case Right(body) => {
          val fileOption = body.file("image")
          fileOption.map {
            file =>
              new S3File(userId.get, tastingId.toString).save(file.ref.file)
          }
        }
      }
      Redirect(routes.Tastings.tastings)
  }

  def update(id: Long) = Action(parse.multipartFormData) {
    implicit request =>
      request.body.file("image").map {
        file =>
          val userId = request.session.get(User.USER_ID)
          file.ref.moveTo(new File(Image.location(userId.get, id.toString)), true)
      }

      tastingForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.editTastings(Some(id), formWithErrors)),
        tasting => {
          tasting.userId = Some(request.session.get(User.USER_ID).get.toLong)
          Tasting.update(id, tasting)
          Ok(html.tastings(Tasting.list(request.session.get(User.USER_ID).get.toInt))(request.session))
          Redirect(routes.Tastings.tastings)
        }

      )
  }

}
