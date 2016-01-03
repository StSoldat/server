/*
package com.assabetsecurity.core.db

import data.{UserId, User}
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.verb.ShouldVerb
import java.util.UUID
import org.slf4s.Logging

/**
 * User: alyas
 * Date: 4/6/13
 * Time: 4:36 PM
 */
class XmlDbSpec extends WordSpec with ShouldMatchers with Logging {
  def testfile = "testDb.xml"
  "XmlDb" should {
    "save" in {
      XmlDb.save()
    }
    "AddUser and Save" in {
      XmlDb.addUser(new User(
        id = UserId(UUID.randomUUID()), login="TestLogin"
      ))
      XmlDb.addUser(new User(
        id = UserId(UUID.randomUUID()), login="TestLogin2"
      ))
      XmlDb.save(testfile)
    }
    "AddUser Save and Load" in {
      val u1 = new User(id = UserId(UUID.randomUUID()), login="TestLogin")
      val u12 = new User(id = UserId(UUID.randomUUID()), login="TestLogin2")
      XmlDb.addUser(u1)
      XmlDb.addUser(u12)
      XmlDb.save(testfile)
      XmlDb.load(testfile)
      val u1res= XmlDb.get(u1.id)
      log.info(">>"+u1res)
      u1res should be equals(u1)
    }
    "validate AuthType serializer" in {

    }
  }
}
*/
