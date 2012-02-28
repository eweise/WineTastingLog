package controllers


import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views._
import models._


object Tastings extends Controller  {

  val tastingForm:Form[Tasting] = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
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

  def tastings = {
    Action {
      implicit request =>
        Ok(html.tastings(Tasting.list(request.session.get(User.USER_ID).get.toInt), tastingForm)(request.session))
    }
  }

  def delete(id: Long) = Action {
    Tasting.delete(id)
    Redirect(routes.Tastings.tastings)
  }

  def save = Action {
    implicit request =>
      tastingForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.tastings(Tasting.list(request.session.get(User.USER_ID).get.toInt), formWithErrors)),
        tasting => {
          val id = request.body.asFormUrlEncoded.get("id")
          id.head match {
            case "" => {
              println("tasting id = "+ tasting.id.getClass.getName)
              tasting.userId = Some(request.session.get(User.USER_ID).get.toInt)
              Tasting.insert(tasting)
            }
            case _ =>
              val userId = request.session.get(User.USER_ID).get.toInt
              Tasting.update(id.head.toInt, userId, tasting)
          }
          Redirect(routes.Application.login)
        }
      )
  }


}