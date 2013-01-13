package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
import java.util.{UUID, Date}
import java.util


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

  def load(id: Long) = DB.withConnection(implicit c =>
    SQL("select * from tasting where id = {tastingId}").on('tastingId -> id).as(Tasting.simple.singleOpt))


  def list(implicit userId: Long): Seq[Tasting] =
    DB.withConnection(implicit c => SQL("select * from tasting where userId = {userId} order by id").on('userId -> userId).as(Tasting.simple *))


  def trending: Seq[Tasting] =
    DB.withConnection(implicit c => SQL("select * from tasting order by updatedate asc limit 5").as(Tasting.simple *))


  def mostRecent: Seq[Tasting] =
    DB.withConnection(implicit connection => SQL("select * from tasting order by updatedate asc limit 5").as(Tasting.simple *))

  def attributeListTuples(field: String)(implicit userId: Long): Seq[(String, String)] = attributeList(field).map(value => (value.toString, value.toString))

  def attributeList(field: String)(implicit userId: Long) = field.toLowerCase match {
    case "region" => distinctTasting("region")
    case "style" => distinctTasting("style")
    case "brand" => distinctTasting("brand")
    case "year" => year
    case _ => List()
  }

  def year(implicit userId: Long) = {
    DB.withConnection {
      implicit connection => {
        SQL(<s>select distinct year from tasting where year is not null and userid =
          {userId}
          order by year desc</s>.text).as(int("year") *)
      }
    }
  }

  def distinctTasting(field: String)(implicit userId: Long): List[String] =
    DB.withConnection(implicit connection => {
      SQL(<s>select distinct
        {field}
        from tasting where
        {field}
        is not null and userid =
        {userId}
        order by
        {field}
        asc</s>.text)
        .as(str(field) *)
    })

  def insert(tasting: Tasting): Long = {
    var id: Long = 0

    DB.withConnection {
      implicit connection => {
        id = SQL("select nextval('tasting_seq')").apply().head[Long]("nextval")

        SQL(
          """
            insert into tasting(id, userId, rating, notes, brand, style, region, year, updateDate) values ({id},
              {userId}, {rating}, {notes}, {brand}, {style}, {region}, {year},{updateDate}
            )
          """
        ).on(
          'id -> id,
          'userId -> tasting.userId,
          'rating -> tasting.rating,
          'notes -> tasting.notes,
          'brand -> tasting.brand,
          'style -> tasting.style,
          'region -> tasting.region,
          'year -> tasting.year,
          'updateDate -> new Date()
        ).executeUpdate()
      }
    }
    id
  }


  def update(id: Long, tasting: Tasting) {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
            update tasting
            set rating = {rating}, notes = {notes}, brand = {brand}, style = {style}, region = {region}, year = {year}, updateDate = {updateDate}
            where id = {id}
          """
        ).on(
          'id -> id,
          'rating -> tasting.rating,
          'notes -> tasting.notes,
          'brand -> tasting.brand,
          'style -> tasting.style,
          'region -> tasting.region,
          'year -> tasting.year,
          'updateDate -> new Date()
        ).executeUpdate()
    }
  }

  def delete(id: Long) =
    DB.withConnection(implicit c => SQL("delete from tasting where id = {id}").on('id -> id).executeUpdate())
}

case class User(id: Pk[Long],
                email: String,
                username: Option[String],
                password: String,
                createDate: Date = new Date(System.currentTimeMillis()),
                var authToken: String = "") {
}

object User {

  val USER_ID = "userId"
  val EMAIL = "email"

  val simple = {
    get[Pk[Long]]("id") ~
      get[String]("email") ~
      get[Option[String]]("username") ~
      get[String]("password") ~
      get[Date]("createDate") ~
      get[String]("token") map {
      case id ~ email ~ username ~ password ~ createDate ~ authToken => User(id, email, username, password, createDate, authToken)
    }
  }

  def findByEmail(email: String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from users where email = {email}").on('email -> email).as(User.simple.singleOpt)
    }
  }

  def authenticate(email: String, password: String): Option[User] =
    DB.withConnection(implicit c =>
      SQL("select * from users where email = {email} and password = {password}").on(
        'email -> email,
        'password -> password
      ).as(User.simple.singleOpt)
    )


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

  def insert(username: Option[String], email: String, password: String) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
            insert into users (id, email, username, password, token, createDate) values ( nextval('user_seq'),
              {email}, {username}, {password}, {token}, {createDate}
            )
          """
        ).on(
          'email -> email,
          'username -> username,
          'password -> password,
          'token -> UUID.randomUUID().toString,
          'createDate -> new Date()
        ).executeUpdate()

    }
  }
}


