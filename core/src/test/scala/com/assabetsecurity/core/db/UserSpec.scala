package com.assabetsecurity.core.db

import com.assabetsecurity.core.db.data._
import org.scalatest.WordSpec

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, WordSpec}

import org.slf4s.Logging
import java.util.UUID
import org.joda.time.DateTime
import com.mongodb.casbah.commons.MongoDBObject
import net.liftweb.json._
import net.liftweb.json.ext.JodaTimeSerializers
import net.liftweb.json.TypeInfo
import com.assabetsecurity.core.db.data.UserId
import com.assabetsecurity.core.db.data.User
import net.liftweb.json.JsonAST.JString
import com.assabetsecurity.core.db.data.UserIdentity
import com.assabetsecurity.core.db.data.UserQuery
import com.assabetsecurity.core.data.{RemoteApplicationQuery, SiteQuery, SiteId, Site}


//import net.liftweb.json.JsonAST._
import net.liftweb.json.Extraction._
import net.liftweb.json.Printer._

import net.liftweb.json.JsonAST.JField
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonAST.JString
import net.liftweb.json.Printer.pretty
import net.liftweb.json.JsonAST.render
import scala.Some


import net.liftweb.json.JsonAST.JField
import net.liftweb.json.JsonAST.JString
import scala.Some

/**
 * User: alyas
 * Date: 6/22/13
 * Time: 7:57 AM
 */
class UserSpec
  extends WordSpec with ShouldMatchers with Logging {

/*
  case object IdentifierSerializer extends CustomSerializer[Identifier](format => (
    {
      case _: Identifier => throw new Exception("we don't currently support serialization of this")
    },
    {
      case id: Identifier => {
        JObject(List(
          JField("value", JString(id.value.toString))
        ))
      }
    }
    ))
*/

  class UUIDFormat extends Serializer[UUID] {
    val UUIDClass = classOf[UUID]

    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), UUID] = {
      case (TypeInfo(UUIDClass, _), JString(x)) => UUID.fromString(x)
      case (TypeInfo(UUIDClass, _), _) => UUID.randomUUID()
    }

    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case x: UUID => JString(x.toString)
    }
  }

  implicit val formats  = Serialization.formats(NoTypeHints) ++ JodaTimeSerializers.all +  new UUIDFormat

    val userJson = "{\n    \"jsonClass\":\"User\",\n    \"id\":{\n      \"jsonClass\":\"UserId\",\n      \"value\":\"e40898f7-8fa0-4fbf-af07-fd435f06a004\"\n    },\n    \"created\":\"2013-07-20T12:55:11.844Z\",\n    \"modified\":\"2013-07-20T12:55:11.844Z\",\n    \"loginName\":\"test2013-06-22T17:49:56.393-04:00\",\n    \"password\":\"Unknown\",\n    \"authType\":\"Password\",\n    \"isActive\":true,\n    \"expires\":\"2013-06-22T21:49:56.431Z\",\n    \"passwordHistory\":[]\n  },{\n    \"jsonClass\":\"User\",\n    \"id\":{\n      \"jsonClass\":\"UserId\",\n      \"value\":\"d6d8b74b-9843-4e72-9c33-7c94783ae726\"\n    },\n    \"created\":\"2013-07-20T12:55:11.844Z\",\n    \"modified\":\"2013-07-20T12:55:11.844Z\",\n    \"loginName\":\"testProperty[millisOfDay]\",\n    \"password\":\"Unknown\",\n    \"authType\":\"Password\",\n    \"isActive\":true,\n    \"expires\":\"2013-06-23T19:35:53.381Z\",\n    \"identity\":{\n      \"jsonClass\":\"UserIdentity\",\n      \"firstName\":\"test_name1\",\n      \"lastName\":\"test_name2\"\n    },\n    \"passwordHistory\":[]\n  }"
    val userJsonNoId = "{\n    \"jsonClass\":\"User\",   \"created\":\"2013-07-20T12:55:11.844Z\",\n    \"modified\":\"2013-07-20T12:55:11.844Z\",\n    \"loginName\":\"test2013-06-22T17:49:56.393-04:00\",\n    \"password\":\"Unknown\",\n    \"authType\":\"Password\",\n    \"isActive\":true,\n    \"expires\":\"2013-06-22T21:49:56.431Z\",\n    \"passwordHistory\":[]\n  },{\n    \"jsonClass\":\"User\",\n    \"id\":{\n      \"jsonClass\":\"UserId\",\n      \"value\":\"d6d8b74b-9843-4e72-9c33-7c94783ae726\"\n    },\n    \"created\":\"2013-07-20T12:55:11.844Z\",\n    \"modified\":\"2013-07-20T12:55:11.844Z\",\n    \"loginName\":\"testProperty[millisOfDay]\",\n    \"password\":\"Unknown\",\n    \"authType\":\"Password\",\n    \"isActive\":true,\n    \"expires\":\"2013-06-23T19:35:53.381Z\",\n    \"identity\":{\n      \"jsonClass\":\"UserIdentity\",\n      \"firstName\":\"test_name1\",\n      \"lastName\":\"test_name2\"\n    },\n    \"passwordHistory\":[]\n  }"
    def testUser1 =  new User(
      id = new UserId(UUID.randomUUID()),
      loginName = "test__"+UUID.randomUUID().toString(),
      identity = Some(UserIdentity(Some("test_name1"), Some("test_name2"))),
      userSecurityRoles = List(UserSecurityRole(SecurityRoleEnum.Public), UserSecurityRole(SecurityRoleEnum.SiteAdmin))
    )

    def sp = DBOSecurityPrincipal()

    "db " should {
      "update sysadmin" in {
        val q= MongoDBObject(
          ("loginName" ->  "admin@canmoderate.com"))

        val admin = DB.security.find(UserQuery(q),  sp)._1.last
        log.debug(""+admin)
        admin.copy(
            isActive = true,
            userSecurityRoles = List(
              UserSecurityRole(SecurityRoleEnum.SysDBO, Some(new DateTime())),
              UserSecurityRole(SecurityRoleEnum.SysAdmin), UserSecurityRole(SecurityRoleEnum.SiteAdmin), UserSecurityRole(SecurityRoleEnum.SiteOwner),
              UserSecurityRole(SecurityRoleEnum.Public))
        ).updatePassword(None, "UEghfd#4Jn4", sp).toOption.foreach(v=> DB.security.save(v.user, sp))
      }
      "add sysadmin" in {
        def sysadmin =  new User(
          id = new UserId(UUID.fromString("23af93c9-22ca-407b-b244-f25f35744ae3")),
          loginName = "sysadmin@canmoderate.com",
          identity = Some(UserIdentity(Some("sysadmin"), Some("sysadmin"))),
          //userRole = List(UserRole(PublicSecurityRole()), UserRole(SiteAdminSecurityRole()), UserRole(SysAdminSecurityRole())),
          userSecurityRoles = List(
            UserSecurityRole(SecurityRoleEnum.SysDBO, Some(new DateTime())),
            UserSecurityRole(SecurityRoleEnum.SysAdmin), UserSecurityRole(SecurityRoleEnum.SiteAdmin), UserSecurityRole(SecurityRoleEnum.SiteOwner),
            UserSecurityRole(SecurityRoleEnum.Public))
        )
        sysadmin.updatePassword(None, "", sp).toOption.foreach(v=> DB.security.save(v.user, sp))
        val site = Site( id = new SiteId(UUID.fromString("6a72f6fa-cfb3-aadd-ab65-8dfd10d97a08")),
          created = new DateTime,
          modified = new DateTime,
          description = Some("sysadmin"),
          userId = Some(sysadmin.id), //UserId(UUID.fromString("d6d8b74b-9843-4e72-9c33-7c94783ae726"))),
          uri = "canmoderate.com",
          activeSiteConfig = ""
        )
        DB.etc.save(site, sp)
      }
      "find all" in {
        DB.security.find[User]( UserQuery((MongoDBObject())), sp)._1.foreach(u=>{
          log.debug(""+u.loginName)
        })
      }
      "load saved user" in {
          val res = DB.security.save(testUser1,  sp )
          log.debug("res: "+res)

          res.fail.toOption.map(v=>fail(v.e))

        val getRes = DB.security.get(res.toOption.get.id,  sp)
        log.debug("get res: "+getRes)
      }
      "get all users" in {
        //val res = DB.security.find(UserQuery(), DBOSecurityPrincipal())
        val q = MongoDBObject()
        log.debug("collection name: "+ UserQuery(q).collectionName)

        val res = DB.security.find(UserQuery(q), sp)

        log.debug("collection res: "+ res)
      }
      "get user as json" in {
        //val res = DB.security.find(UserQuery(), DBOSecurityPrincipal())
        val q= MongoDBObject()
        log.debug("collection name: "+ UserQuery(q).collectionName)

        val res = Extraction.decompose(DB.security.find(UserQuery(q), sp)._1.last)
        val r = pretty(render(res))
        log.debug("collection res: "+ r)
      }
      "restore User from json" in {
        log.debug("user Json: " +  userJson)
        val json = parse(userJson)
        log.debug("user Json: " +  json )
        val user = Extraction.extract[User](json)
        log.debug("user Json: " +  user )
        //val res = Extraction.decompose()
        //val r = pretty(render(res))
        //log.debug("collection res: "+ r)
      }
      "restore User from json new" in {
        log.debug("user Json: " +  userJsonNoId)
        val json = parse(userJsonNoId)
        log.debug("user Json: " +  json )
        val user = Extraction.extract[User](json)
        log.debug("user Json: " +  user )
        //val res = Extraction.decompose()
        //val r = pretty(render(res))
        //log.debug("collection res: "+ r)
      }
      "updatePassword" in {
        //log.debug("user Json: " +  userJsonNoId)
        val json = parse(userJson)
        //log.debug("user Json: " +  json )
        val user = Extraction.extract[User](json)
        //log.debug("user Json: " +  user )
        val res = user.updatePassword(None, "AABBCC", sp)
        log.debug("user Json: " +  res.map(_.user))
        val res2 = res.toOption.get.user.validatePassword("AABBCC")
        log.debug("user Json: " +  res2)
        res2.isSuccess should be (true)

        val res3 = res.toOption.get.user.validatePassword("AABBCC2")
        res3.isSuccess should be (false)
        DB.security.save(res.toOption.last.user, sp)
      }
      "create demo user" in {
        //val u  = "mstest@canmoderate.com"
        val u = "demo_template@canmoderate.com"
        val q= MongoDBObject(
          ("loginName" ->  u))
        val templateUser = DB.security.find(UserQuery(q),  sp)._1.last
        log.debug(""+templateUser)
        val site = DB.etc.find[Site](SiteQuery(MongoDBObject("userId.value" ->templateUser.id.value)), DBOSecurityPrincipal())._1
        log.debug("site :: "+site)
        site.foreach(site=>{
          val remoteApps = DB.security.find(RemoteApplicationQuery(MongoDBObject("siteId.value"->site.id.value)), DBOSecurityPrincipal())._1
          log.debug("remoteApps :: "+remoteApps )
        })
      }

      "drop demo users" in {
        DB.security.find(UserQuery(),  sp)._1.
          filter(u=>u.loginName.startsWith("demo_"))
          .filterNot(_.loginName == "demo_template@canmoderate.com").
        foreach(u=>{
          log.debug(u.loginName)
          DB.security.remove(u.id, sp)
        })

      }
    }
}
