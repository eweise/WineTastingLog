package controllers

import play.api.mvc.{Security, Controller}
import play.api.data._
import anorm.NotAssigned
import views.html
import models.{User, Tasting}


object Tastings extends Controller with Security.AllAuthenticated {

  val tastingForm = Form(
    of(Tasting.apply _)(
      "id" -> ignored(NotAssigned),
      "userId" -> optional(number),
      "rating" -> optional(number),
      "notes" -> optional(text),
      "brand" -> optional(text),
      "style" -> optional(text),
      "region" -> optional(text),
      "year" -> optional(number),
      "updateDate" -> optional(date)
    )
  )

  def tastings = {
    Action {
      implicit request =>
        Ok(html.tastings(Tasting.list(request.session.get(User.USER_ID).get.toLong), tastingForm)(request.session))
    }
  }

  def delete(id: Long) = Action {
    Tasting.delete(id)
    Redirect(routes.Tastings.tastings)

  }

  def save = Action {
    implicit request =>
      tastingForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.tastings(Tasting.list(request.session.get(User.USER_ID).get.toLong), formWithErrors)),
        tasting => {
          val id = request.body.urlFormEncoded.get("id")
          id.get(0) match {
            case "" => {
              tasting.userId = Some(request.session.get(User.USER_ID).get.toLong)
              Tasting.insert(tasting)
            }
            case _ =>
              val userid = request.session.get(User.USER_ID).get.toLong
              Tasting.update(id.get(0).toLong, userid, tasting)
          }
          Redirect(routes.Application.login)
        }
      )
  }


}