package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
import java.util.Date


case class Tasting(
                    id: Pk[Long],
                    var userId: Option[Long],
                    rating: Option[Int],
                    notes: Option[String],
                    brand: Option[String],
                    style: Option[String],
                    region: Option[String],
                    year: Option[Int],
                    updateDate: Option[Date]) {

  def notesSmall = {
    notes match {
      case None => ""
      case _ => if (notes.get.size > 15) notes.get.substring(0, 15) + "..." else notes.get
    }
  }
}

object Tasting {

  val simple = {
     get[Pk[Long]]("id") ~
      get[Option[Long]]("userId") ~
      get[Option[Int]]("rating") ~
      get[Option[String]]("notes") ~
      get[Option[String]]("brand") ~
      get[Option[String]]("style") ~
      get[Option[String]]("region") ~
      get[Option[Int]]("year") ~
      get[Option[Date]]("updateDate") map {
      case id ~ userId ~ rating ~ notes ~ brand ~ style ~ region ~ year ~ updateDate => Tasting(id, userId, rating, notes, brand, style, region, year, updateDate)
    }
  }

  def list(userId: Long): Seq[Tasting] = {
    DB.withConnection {
      implicit connection => {
        SQL(
          """
            select * from tasting where userId = {userId}
          """
        ).on('userId -> userId).as(Tasting.simple *)
      }
    }

  }

  // todo replace with Json library
  def listAsJson(userId: Long) = {
    val tastings = for (tasting <- list(userId)) yield {
      "{" +
        kv("id", tasting.id.getOrElse("")) + "," +
        kv("brand", tasting.brand.getOrElse("")) + "," +
        kv("rating", tasting.rating.getOrElse("")) + "," +
        kv("style", tasting.style.getOrElse("")) + "," +
        kv("region", tasting.region.getOrElse("")) + "," +
        kv("year", tasting.year.getOrElse("")) + "," +
        kv("notes", tasting.notes.getOrElse("")) +
        "}"
    }
    "{\"tastings\":[" + commaDelimit(tastings) + "]}"
  }

  def commaDelimit(seq: Seq[String]) = seq.reduceLeft((acc, result) => acc + "," + result)

  def kv(key: String, value: Any) = {
    quote(key) + ":" + quote(value)
  }

  def quote(value: Any) = {
    "\"" + value + "\""
  }

  def insert(tasting: Tasting) = {
    DB.withConnection {
      implicit connection => {
        println("insert user id = " + tasting.userId)
        SQL(
          """
            insert into tasting(id, userId, rating, notes, brand, style, region, year, updateDate) values (nextval('tasting_seq'),
              {userId}, {rating}, {notes}, {brand}, {style}, {region}, {year},{updateDate}
            )
          """
        ).on(
          'userId -> tasting.userId,
          'rating -> tasting.rating,
          'notes -> tasting.notes,
          'brand -> tasting.brand,
          'style -> tasting.style,
          'region -> tasting.region,
          'year -> tasting.year,
          'updateDate -> tasting.updateDate
        ).executeUpdate()
      }
    }
  }


  def update(id: Long, userId: Int, tasting: Tasting) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
            update tasting
            set rating = {rating}, notes = {notes}, brand = {brand}, style = {style}, region = {region}, year = {year}, updateDate = {updateDate}
            where id = {id} and userId = {userId}
          """
        ).on(
          'id -> id,
          'userId -> userId,
          'rating -> tasting.rating,
          'notes -> tasting.notes,
          'brand -> tasting.brand,
          'style -> tasting.style,
          'region -> tasting.region,
          'year -> tasting.year,
          'updateDate -> tasting.updateDate
        ).executeUpdate()
    }
  }

  def delete(id: Long) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          delete from tasting where id = {id}
          """
        ).on('id -> id).executeUpdate()
    }
  }
}

case class User(id: Pk[Long],
                email: String,
                username: String,
                password: String,
                createDate: Date = new Date(System.currentTimeMillis()),
                var authToken: String = "") {
}

object User {

  val USER_ID = "userId"
  val USERNAME = "username"

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("id") ~
      get[String]("email") ~
      get[String]("username") ~
      get[String]("password") ~
      get[Date]("createDate") ~
      get[String]("token") map {
      case id ~ email ~ username ~ password ~ createDate ~ authToken => User(id, email, username, password, createDate, authToken)
    }
  }

  // -- Queries

  /**
   * Retrieve a User from email.
   */
  def findByUsername(username: String): Option[User] = {
    println("username = " + username)
    DB.withConnection {
      implicit connection =>
        SQL("select * from users where username = {username}").on(
          'username -> username
        ).as(User.simple.singleOpt)
    }
  }

  /**
   * Retrieve a User from email.
   */
  def findByToken(token: String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from users where token = {token}").on(
          'token -> token
        ).as(User.simple.singleOpt)
    }
  }

  /**
   * Retrieve all users.
   */
  def findAll: Seq[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from users").as(User.simple *)
    }
  }

  /**
   * Authenticate a User.
   */
  def authenticate(username: String, password: String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
           select * from users where
           username = {username} and password = {password}
          """
        ).on(
          'username -> username,
          'password -> password
        ).as(User.simple.singleOpt)
    }
  }

  def update(user: User) {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          update users set
          email = {email}, 
          username = {username},
          password = {password},
          token = {authToken}
          """
        ).on(
        'email -> user.email,
        'username -> user.username,
        'password -> user.password,
        'authToken -> user.authToken
        ).executeUpdate()
    }
  }

  def insert(username: String, email: String, password: String) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
            insert into users (id, email, username, password, createDate) values ( nextval('user_seq'),
              {email}, {username}, {password}, {createDate}
            )
          """
        ).on(
          'email -> email,
          'username -> username,
          'password -> password,
          'createDate -> new Date()
        ).executeUpdate()

    }
  }
}


