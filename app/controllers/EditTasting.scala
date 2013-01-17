package controllers

import play.api.mvc.{MultipartFormData, Request, Action, Controller}
import play.api.data.Form
import models.{S3File, User, Tasting}
import play.api.data.Forms._
import anorm.{Pk, NotAssigned}
import views.html
import play.api.libs.Files

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
    request =>
      val userId = Some(request.session.get(User.USER_ID).get.toLong)
      new S3File(userId.get, id.toString).delete()
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

      val userId = request.session.get(User.USER_ID).get.toLong
      tastingForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(html.editTastings(None, formWithErrors))
        },
        tasting => {
          tasting.userId = Some(userId)
          saveFile(request, userId, Tasting.insert(tasting))
          Redirect(routes.Tastings.tastings)
        }
      )
  }

  def update(id: Long) = Action(parse.multipartFormData) {
    implicit request =>
      val userId = request.session.get(User.USER_ID).get.toLong
      tastingForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.editTastings(Some(id), formWithErrors)),
        tasting => {
          Tasting.update(id, tasting)
          saveFile(request, userId, id)
          Redirect(routes.Tastings.tastings)
        }
      )
  }

  def saveFile(request: Request[MultipartFormData[Files.TemporaryFile]], userId: Long, tastingId: Long) {
    request.body.file("image").foreach(f => new S3File(userId, tastingId.toString).save(f.ref.file))
  }


}
