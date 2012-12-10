package controllers

import play.api.mvc.{Action, Controller}
import play.api.data.Form
import models.{Image, User, Tasting}
import play.api.data.Forms._
import anorm.{Pk, NotAssigned}
import views.html
import java.io.File

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

  def saveNew = Action(parse.multipartFormData) {
    implicit request =>
      var tastingId = 0L
      tastingForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.editTastings(None, formWithErrors)),
        tasting => {
          tasting.userId = Some(request.session.get(User.USER_ID).get.toInt)
          tastingId = Tasting.insert(tasting)
        }
      )
      request.body.file("image").map {
        file =>
          val userId = request.session.get(User.USER_ID).get
          file.ref.moveTo(new File(Image.location(userId, tastingId.toString)), true)
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
