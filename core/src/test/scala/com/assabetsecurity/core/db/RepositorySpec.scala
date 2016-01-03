package com.assabetsecurity.core.db

import org.joda.time.DateTimeZone
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.ShouldMatchers
import org.slf4s.Logging
import java.util.UUID
import com.mongodb.casbah.commons.MongoDBObject
import net.liftweb.json._
import net.liftweb.json.ext.JodaTimeSerializers
import com.assabetsecurity.core.data.Site

//import net.liftweb.json.JsonAST._

import net.liftweb.json.Printer.pretty
import net.liftweb.json.JsonAST.render


import com.assabetsecurity.core.db.data.UserId
import com.assabetsecurity.core.db.data.User
import net.liftweb.json.JsonAST.JString
import com.assabetsecurity.core.db.data.UserIdentity
import scala.Some
import com.assabetsecurity.core.db.data.UserQuery

/**
 * User: alyas
 * Date: 6/22/13
 * Time: 7:57 AM
 */
class RepositorySpec
  extends  WordSpec with ShouldMatchers with BeforeAndAfterAll with Logging {

    val userJson = "{\n  \"id\":{\n    \"value\":\"2e5e7219-69e7-4c58-aa60-9d1f86fb4f66\"\n  },\n  \"loginName\":\"test__8eb8b758-4f0f-4eed-bc37-c6985b21f46a\",\n  \"password\":\"Unknown\",\n  \"authType\":\"Password\",\n  \"isActive\":true,\n  \"expires\":\"2013-06-23T19:36:54.676Z\",\n  \"identity\":{\n    \"firstName\":\"test_name1\",\n    \"lastName\":\"test_name2\"\n  },\n  \"passwordHistory\":[]\n}"
    val userJsonNoId = "{\n  \"loginName\":\"test__8eb8b758-4f0f-4eed-bc37-c6985b21f46a\",\n  \"password\":\"Unknown\",\n  \"authType\":\"Password\",\n  \"isActive\":true,\n  \"expires\":\"2013-06-23T19:36:54.676Z\",\n  \"identity\":{\n    \"firstName\":\"test_name1\",\n    \"lastName\":\"test_name2\"\n  },\n  \"passwordHistory\":[]\n}"

    def testUser =  new User(
      id = new UserId(UUID.randomUUID()),
      loginName = "test__"+UUID.randomUUID().toString(),
      identity = Some(UserIdentity(Some("test_name1"), Some("test_name2")))
    )

   override def afterAll  {
     DB.unitTest.find(UserQuery(), sp)._1.foreach(u => {
       log.debug(">> remove "+u.id)
       DB.unitTest.remove(u.id, sp)
     })
   }

    def sp = DBOSecurityPrincipal()

    "repository" should {
        "save user and get" in {
          val u  = testUser
          assert(DB.unitTest.save(u, sp).isSuccess)
          assert(DB.unitTest.get(u.id, sp).isDefined)
          log.debug(""+u)
          log.debug(""+u.copy(created = u.created.withZone(DateTimeZone.UTC)) )

        }
        "load json query" in {
          val u  = testUser.copy(loginName = "asdfgh")
          assert(DB.unitTest.save(u, sp).isSuccess)
          val res = DB.unitTest.find(JsonQuery[User](Some("""{"loginName":"asdfgh"} """)), sp)
          log.debug("get res: "+res)
          assert(!res._1.isEmpty)
       }
      "save user and delete" in {
        val u  = testUser
        assert(DB.unitTest.save(u, sp).isSuccess)
        assert(DB.unitTest.get(u.id, sp).isDefined)
        DB.unitTest.remove(u.id, sp)
        assert(DB.unitTest.get(u.id, sp).isEmpty)
      }
      "load User by UserQuery" in {
        val u  = testUser
        assert(DB.unitTest.save(u, sp).isSuccess)
        val res = DB.unitTest.find(UserQuery(MongoDBObject("_id" -> u.id.value)), sp)
        assert(!res._1.isEmpty)
      }
      "load user empty UserQuery" in {
        val u = testUser
        assert(DB.unitTest.save(u, sp).isSuccess)
        val res = DB.unitTest.find(UserQuery(), sp)
        //check for id only - modified date would be updated by repository
        assert(res._1.map(_.id).contains(u.id))
      }
    }
}
