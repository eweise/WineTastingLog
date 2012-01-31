package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.validation.Constraints._
import format.Formats._

import views._
import models._
import java.util.UUID

object Application extends Controller {

  val loginForm = {
    println("loginForm text = " + text)
    Form(
      of(
        "username" -> text,
        "password" -> text
      ) verifying("Invalid username or password", result => result match {
        case (username, password) => {
          println("loginForm. authenticate")
          User.authenticate(username, password).isDefined
        }
      })
    )
  }

  val registerForm = Form(
    of(
      "username" -> of[String].verifying(required),
      "email" -> email.verifying(required),
      "password" -> of[String].verifying(required),
      "password2" -> of[String].verifying(required)
    ) verifying("Not all parameters supplied", result => result match {
      case (username, email, password, password2) => {

        true
      }
    })
  )


  def login = Action {
    implicit request =>
      request.session.get(User.USER_ID) match {
        case None => Ok(html.login(loginForm))
        case _ => {
          Redirect(routes.Tastings.tastings)
        }
      }
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.login(formWithErrors)),
        user => {
          val dbUser = User.findByUsername(user._1).get
          Redirect(routes.Tastings.tastings).withSession(User.USERNAME -> dbUser.username, User.USER_ID -> dbUser.id.get.toString)
        }
      )
  }


  def nativeAuthenticate = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest("Invalid email or password")
        }, userForm => {
          var dbUser = User.findByUsername(userForm._1)
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
          val newUser = User.findByUsername(user._1)
          (Redirect(routes.Tastings.tastings).withSession(
            User.USER_ID -> newUser.get.id.toString,
            User.USERNAME -> newUser.get.username))
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

