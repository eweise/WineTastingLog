package controllers

import org.apache.commons.codec.binary.Base64
import anorm.NotAssigned
import play.api.data._
import play.api.data.Forms._


import models._
import play.api.Logger
import play.api.mvc._


object Rest extends Controller {

  type Token = String

  val idForm = Form(
    tuple(
      "token" -> text,
      "id" -> text
    )
  )

  def delete(implicit token:Token) = tokenAction {
    implicit request =>
      Logger.info("delete called")
      request.authToken
//      Tasting.delete(id)
      Ok("")
  }


  def tokenAction[A](f: User => Result)(implicit token: Token) = Action {
    implicit request =>
      println("tokenAction")
      Logger.debug("tokenAction")
      User.findByToken(token) match {
        case None => BadRequest("No user found")
        case Some(user) => {
          f(user)
        }
      }
  }

  def tastings(implicit token:Token) = tokenAction(user => Ok(Tasting.listAsJson(user.id.get)))


  def getimage = Action {
    request =>
      Logger.debug("getImage")
      val token = request.queryString.get("token").get.headOption.getOrElse("")
      Logger.debug("getImage token = " + token)
      User.findByToken(token) match {
        case None => BadRequest("no user found")
        case _ => {
          Logger.debug("getImage found user")

          val tastingId = request.queryString.get("tastingid").get.headOption.getOrElse("")
          Logger.debug("found tasting " + tastingId)
          val image = Image.read(tastingId)
          image match {
            case None => Ok("")
            case _ => val base64Image = Base64.encodeBase64(image.get)
            Ok(base64Image)
          }
        }
      }
  }

  def getUser(implicit request: Request[AnyContent]): Option[User] = {
    val token = formValue("token").getOrElse("")
    User.findByToken(token)
  }


  def formValue(key: String)(implicit request: Request[AnyContent]): Option[String] = {
    val form = request.body.asFormUrlEncoded.get
    Logger.debug("formvalue key " + key)
    val value = form.get(key)
    Logger.debug("formvalue value = " + value)
    value match {
      case s: Some[Seq[String]] => {
        Logger.debug("formvalue some = " + value.get.headOption)
        value.get.headOption
      }
      case _ => {
        Logger.debug("formvalue None")
        None
      }
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
          Tasting.update(id.get.toInt,  tasting)
      }
      Ok("")
  }

  def uploadphoto = Action {
    request =>
      val message = request.body.asFormUrlEncoded.get.asInstanceOf[Map[String, List[String]]]
      Logger.debug("message = " + message)
      val token = message.get("token").get.headOption.get
      val tastingId = message.get("tastingid").get.headOption.get
      User.findByToken(token) match {
        case None => BadRequest("no user found")
        case _ => {
          val imageValue = message.get("image").get.headOption.get
          Logger.debug("getting bytes...")
          val bytes = Base64.decodeBase64(imageValue.getBytes)
          Logger.info("finished getting bytes")
          Image.write(tastingId, bytes)
        }
      }
      Ok(Tasting.listAsJson(1))
  }


}