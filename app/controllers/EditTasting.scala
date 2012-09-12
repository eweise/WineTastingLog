package controllers

import play.api.mvc.{Action, Controller}
import play.api.data.Form
import models.{User, Tasting}
import play.api.data.Forms._
import anorm.{Pk, NotAssigned}
import views.html

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

  def saveNew = Action {
    implicit request =>
      tastingForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.editTastings(None, formWithErrors)),
        tasting => {
          println("tasting id = " + tasting.id.getClass.getName)
          tasting.userId = Some(request.session.get(User.USER_ID).get.toInt)
          Tasting.insert(tasting)
          Redirect(routes.Application.login)
        }
      )
  }

  def update(id:Long) = Action {
    implicit request =>
      tastingForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.editTastings(Some(id), formWithErrors)),
        tasting => {
          tasting.userId = Some(request.session.get(User.USER_ID).get.toInt)
          Tasting.update(id, tasting)
          Redirect(routes.Application.login)
        }
      )
  }

}
