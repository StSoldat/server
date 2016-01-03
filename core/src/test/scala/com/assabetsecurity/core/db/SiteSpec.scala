package com.assabetsecurity.core.db

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.slf4s.Logging
import net.liftweb.json.{NoTypeHints, Serialization}
import net.liftweb.json.ext.JodaTimeSerializers
import com.assabetsecurity.core.data._
import org.joda.time.DateTime
import com.assabetsecurity.core.db.data.{UserQuery, User, UserId}

import com.assabetsecurity.core.data.SiteConfig
import com.assabetsecurity.core.data.Site
import com.assabetsecurity.core.data.HtmlValidationClass
import com.assabetsecurity.core.data.SiteId
import scala.Some
import java.util.UUID
import com.mongodb.casbah.commons.MongoDBObject

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 6/30/13
 * Time: 11:32 PM
 * To change this template use File | Settings | File Templates.
 */
class SiteSpec
  extends WordSpec with ShouldMatchers with Logging {

  def testSite2 = Site( id = new SiteId(UUID.fromString("6a72f6fa-cfb3-4880-ab65-8dfd10d97a08")),
    created = new DateTime,
    modified = new DateTime,
    description = Some("test-0014"),
    userId = Some(UserId()), //UserId(UUID.fromString("d6d8b74b-9843-4e72-9c33-7c94783ae726"))),

    uri = "1234567890~!@#$%^&*()_+=-{:>PL<}\"?|/*[];'l;,.//.`вкоасткатЁ\"2<hi>{}]}\"'>\n",
    activeSiteConfig = "default",
    siteUsers = List(
      SiteUser(UserId(UUID.fromString("6a72f6fa-cfb3-4880-aaaa-8dfd10d97a08")), enabled =true)
    ),
    siteConfigs = Map(
      "default" -> SiteConfig( name = "default",
        validationClass = HtmlValidationClass(enabled = true, name  = "htmlOnly", blockInvalidHTML=true ) ::
        EmailValidationClass(
          enabled = true,
          name  = "emails",
          allowListId = None //("pupkin.com"::Nil)
        ) ::
        Nil
      ),
      "full" -> SiteConfig(name = "full",
        validationClass = HtmlValidationClass(enabled = true, name  = "htmlOnly", blockInvalidHTML = true) ::
         EmailValidationClass(enabled = true, name  = "public only") ::
         IpValidationClass(enabled = true, name ="block spam1", blockListId = Some(ValidationIpListId())) ::
           IpValidationClass(enabled = true, name ="block spam2", blockListId = Some(ValidationIpListId())) ::
           IpValidationClass(enabled = true, name ="block spam3", blockListId = Some(ValidationIpListId())) ::
         TextValidationClass(enabled = true, name ="block spam1", blockListId = Some(ValidationTextListId())) ::
         TextValidationClass(enabled = true, name ="block spam2", blockListId = Some(ValidationTextListId())) ::
         Nil
       )
      )
  )

  def sp = DBOSecurityPrincipal()
  "db " should {
    "save new site" in {
      val res = DB.etc.save(testSite2,  sp )

    }
    "cleanup sites" in {
     val users = List("admin1@admin1.com",      "112@mailinator.com",      "mstest@canmoderate.com",      "vvvv@vvvv.com")
     val sites = (for {
        user <- DB.security.find[User] (UserQuery(), sp)._1.filter(u => users.contains(u.loginName))
        site <- DB.etc.find[Site](SiteQuery(MongoDBObject("userId.value" -> user.id.value)), DBOSecurityPrincipal())._1
      } yield {
        site
      }).map(_.id)

      log.debug("sites: "+sites.size)
      val toDelete = DB.security.find[User] (UserQuery(), sp)._1.filterNot(u => users.contains(u.loginName))

      log.debug("to delete: "+toDelete.size)

      toDelete.foreach(s=>{
        //DB.security.remove(s.id, sp)
      })
      //DB.security.find[User](UserQuery(), sp)._1.filter(u=>users.contains(u.loginName)).foreach(u=>{
      //  log.debug(">> "+u.loginName)
      //})
    }
/*    "fix sites" in {
      val res = DB.etc.find[Site](SiteQuery(), sp)
      res.foreach(s=>{
        DB.etc.save(s, sp)
      })
    }*/
    "find user site" in {
      //val json = "{\"userId.value\":{\"$uuid\":\"d6d8b74b-9843-4e72-9c33-7c94783ae726\"} }"
      //val json = "{\"description\" : \"Site022AA\"}"
      val query = MongoDBObject("userId.value" -> UUID.fromString("d6d8b74b-9843-4e72-9c33-7c94783ae726"))
      val res = DB.etc.find[Site](SiteQuery(query), sp)
      log.debug(""+res)
    }
    "find site" in {
      val res = DB.etc.get(new SiteId(UUID.fromString("c41b0cb3-fbb6-4119-b1c3-0f0d46746a6b")), sp)
      log.debug(""+res)
    }
  }

}
