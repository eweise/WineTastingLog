package test

import org.specs2.mutable._


class ApplicationSpec extends Specification {


  // -- Date helpers

  def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str

  // --

  "Application" should {

    "image transfer" in {

      "" must equalTo("")
    }
  }

}