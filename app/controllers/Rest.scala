package controllers

import play.api.mvc._
import play.api.mvc.Controller
import org.apache.commons.codec.binary.Base64
import java.io.{FileOutputStream, File}
import models.{User, Tasting}
import anorm.{NotAssigned, Pk}
import play.api.data.Form
import java.util.Date


object Rest extends Controller {

  //  val tastingForm:Form[Tasting] = Form(
  //    mapping(
  //      "id" -> ignored(NotAssigned:Pk[Long]),
  //      "userId" -> optional(longNumber),
  //      "rating" -> optional(number),
  //      "notes" -> optional(text),
  //      "brand" -> optional(text),
  //      "style" -> optional(text),
  //      "region" -> optional(text),
  //      "year" -> optional(number),
  //      "updateDate" -> optional(date)
  //    )(Tasting.apply)(Tasting.unapply)
  //  )


  def tastings = Action {
    request =>
      println("tastings")
      val token = request.queryString.get("token").get.headOption.getOrElse("")
      User.findByToken(token) match {
        case None => BadRequest("no user found")
        case user: Some[User] => {
          println("found user " + user.get.id + " with token " + token)
          Ok(Tasting.listAsJson(user.get.id.get))
        }
      }
  }

  def getUser(implicit request: Request[AnyContent]): Option[User] = {
    val token = formValue("token").getOrElse("")
    println("token = " + token)
    User.findByToken(token)
  }


  def formValue(key: String)(implicit request: Request[AnyContent]): Option[String] = {
    val form = request.body.asFormUrlEncoded.get
    val value = form.get(key)
    value match {
      case s: Some[Seq[String]] => value.get.headOption
      case _ => None
    }
  }

  def saveTasting = Action {
    implicit request =>
      println("save tasting called")
      val user = getUser

      if (user == None) {
        BadRequest("no user found")
      }

      val userId = Some(user.get.id.get)
      val brand = formValue("brand")
      val style = formValue("style")
      val region = formValue("region")
      val year = Some(formValue("year").getOrElse("2000").toInt)
      val notes = formValue("notes")
      val rating = Some(formValue("rating").getOrElse("1").toInt);


      val tasting = new Tasting(NotAssigned, userId, rating, notes, brand, style, region, year, None)
      val id = formValue("id")

      id match {
        case None => {
          Tasting.insert(tasting)
        }
        case _ =>
          val userId = request.session.get(User.USER_ID).get.toInt
          Tasting.update(id.get.toInt, userId, tasting)
      }
      Ok("")
  }

  def uploadphoto = Action {
    request =>
      val message = request.body.asFormUrlEncoded.get.asInstanceOf[Map[String, List[String]]]
      println("message = " + message)
      val token = message.get("token").get.headOption.get
      User.findByToken(token) match {
        case None => BadRequest("no user found")
        case _ => {
          val imageValue = message.get("image").get.headOption.get
          println("getting bytes...")
          val bytes = Base64.decodeBase64(imageValue.getBytes)
          println("finished getting bytes")
          writeToFile(bytes)
        }
      }
      Ok(Tasting.listAsJson(1))
  }

  def writeToFile(bytes: Array[Byte]) {
    val fos = new FileOutputStream(new File("test.jpg"))
    fos.write(bytes)
    fos.close()
  }

  //  def save = Action {
  //    implicit request =>
  //      tastingForm.bindFromRequest.fold(
  //        formWithErrors => BadRequest(html.tastings(Tasting.list(request.session.get(User.USER_ID).get.toInt), formWithErrors)),
  //        tasting => {
  //          val id = request.body.asFormUrlEncoded.get("id")
  //          id.head match {
  //            case "" => {
  //              tasting.userId = Some(request.session.get(User.USER_ID).get.toInt)
  //              Tasting.insert(tasting)
  //            }
  //            case _ =>
  //              val userId = request.session.get(User.USER_ID).get.toInt
  //              Tasting.update(id.head.toInt, userId, tasting)
  //          }
  //          Redirect(routes.Application.login)
  //        }
  //      )
  //  }


}