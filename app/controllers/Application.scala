package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._


import views._
import models._
import play.api.data._
import play.api.data.validation.Constraints._
import format.Formats._

import views._
import models._
import java.util.UUID

object Application extends Controller {

  val loginForm =
    Form(
      tuple(
        "email" -> email,
        "password" -> nonEmptyText
      ) verifying("Invalid username or password", t => User.authenticate(t._1, t._2).isDefined)
    )

  val registerForm = Form(
    tuple(
      "username" -> optional(text),
      "email" -> email.verifying("This email address already exists!", User.findByEmail(_).isEmpty),
      "password" -> nonEmptyText(8),
      "password2" -> nonEmptyText
    ) verifying("Password do not match", reg => reg._3 == reg._4)
  )


  def login = Action {
    implicit request =>
      request.session.get(User.EMAIL) match {
        case None => Ok(html.login(loginForm))
        case _ => Redirect(routes.Tastings.tastings)
      }
  }

  def authenticate = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.login(formWithErrors)),
        user => {
          val dbUser = User.findByEmail(user._1).get
          Redirect(routes.Tastings.tastings).withSession(User.USERNAME -> dbUser.username.getOrElse(""), User.USER_ID -> dbUser.id.get.toString)
        }
      )
  }


  def nativeAuthenticate = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest("Invalid email or password")
        }, userForm => {
          var dbUser = User.findByEmail(userForm._1)
          dbUser.get.authToken = UUID.randomUUID().toString
          User.update(dbUser.get)
          Ok(dbUser.get.authToken)
        }
      )
  }

  def register = Action {
    implicit request =>
      Ok(html.register(registerForm))
  }

  def createUser = Action {
    implicit request =>
      registerForm.bindFromRequest.fold(formWithErrors => {
        BadRequest(html.register(formWithErrors))
      },
        user => {
          User.insert(user._1, user._2, user._3)
          val newUser = User.findByEmail(user._2)
          (Redirect(routes.Tastings.tastings).withSession(
            User.USER_ID -> newUser.get.id.toString,
            User.USERNAME -> newUser.get.username.getOrElse("")))
        }
      )
  }


  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Application.login).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }


}

