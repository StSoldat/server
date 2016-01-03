package com.assabetsecurity.core.db

import org.scalatest.WordSpec

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, WordSpec}

import org.slf4s.Logging
import java.util.UUID
import com.mongodb.casbah.commons.MongoDBObject
import net.liftweb.json._
import net.liftweb.json.ext.JodaTimeSerializers
import com.assabetsecurity.core.data._

import com.assabetsecurity.core.data.ValidationTextList
import com.assabetsecurity.core.data.ValidationTextListId
import com.assabetsecurity.core.data.ValidationIpList
import scala.Some
import com.assabetsecurity.core.data.ValidationIpListId
import com.assabetsecurity.core.db.data.UserId
import util.Random
import java.net.InetAddress

/**
 * User: alyas
 * Date: 6/22/13
 * Time: 7:57 AM
 */
class ValidationListSpec
  extends WordSpec with ShouldMatchers with Logging {

  def testIPlist =  new ValidationIpList(
      id = new ValidationIpListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8ba0")),
      name = "test_local",
      userId = None
      //entry = "10.1.1.1" ::"127.0.0.1"::"127.1.1.1" :: Nil
    )
  def testIPlist2 =  new ValidationIpList(
    id = new ValidationIpListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8bb0")),
    name = "test_local2",
    userId = None
    //entry = "10.0.0.1" ::"127.0.0.1"::"192.168.0.1" :: Nil
  )
  def testIPlist3 =  new ValidationIpList(
    id = new ValidationIpListId(UUID.randomUUID()),
    name = "default IP validation",
    userId = None
    //entry = "10.0.0.1" ::"127.0.0.1"::"192.168.0.1" :: Nil
  )
  def testTextlist =  new ValidationTextList(
    id = new ValidationTextListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8ba1")),
    name = "test_local",
    userId = None,
    language = Some(Language("eng"))
    //entry = "booboo" ::"obama"::"ussr" :: Nil
  )
  def testTextlist2 = new ValidationTextList(
    //id = new ValidationTextListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8ba1")),
    id = new ValidationTextListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8ba2")),
    name = "default text validation (eng)",
    userId = None,
    language = Some(Language("eng"))
    //entry = "booboo" ::"obama"::"ussr" :: Nil
  )
  def testTextlist3 = new ValidationTextList(
    //id = new ValidationTextListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8ba1")),
    id = new ValidationTextListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8ba3")),
    name = "Site validation List",
    userId = Some(UserId(UUID.randomUUID())),
    siteId = Some(SiteId(UUID.randomUUID())),
    language = Some(Language("eng"))
    //entry = "booboo" ::"obama"::"ussr" :: Nil
  )
  def sp = DBOSecurityPrincipal()

    "db" should {
      "update list" in {
        val entry = for ( i <- 1 to 100 ) yield {
           Random.nextInt(100) +"." + Random.nextInt(100) +"."+Random.nextInt(100) +"." + Random.nextInt(100)
        }

        val id = ValidationIpListId(UUID.fromString("1ca827c4-b3e9-4ceb-8c4f-efde16e361e4"))
        DB.etc.get[ValidationIpList](id, sp).foreach(v=>{
          //val n  = v.copy(entry= "0.0.0.0" :: "128.0.0.1" ::"123.0.0.1":: entry.toList  )
          DB.etc.save(v, sp)
        })
      }
      "save text list site" in {
        val res = DB.etc.save(testTextlist3,  sp )
        log.debug(""+res)
        res.isSuccess should be (true)
      }
      "save test text list" in {
        val res = DB.etc.save(testTextlist,  sp )

        log.debug(""+res)
        res.isSuccess should be (true)
        val el = "booboo" ::"obama"::"ussr" :: Nil
        val res2 = testTextlist.saveEntryList(el)
        log.debug(""+res2)
        testTextlist.loadEntryList()(0) should be (el(0))
      }
      "update test text list" in {
        val res = DB.etc.save(testTextlist,  sp )
        log.debug(""+res)
        res.isSuccess should be (true)
        val el = "aab" ::"baa"::"abc" :: "bca" :: "cdb":: Nil
        val res2 = testTextlist.saveEntryList(el)
        log.debug(""+testTextlist.loadEntryList())
        testTextlist.updateEntryList("banana":: Nil, "ussr"::Nil)
        testTextlist.updateEntryList("banana":: Nil, List.empty)
        log.debug(""+testTextlist.loadEntryList())
      }
      "find test text list" in {
        val res = DB.etc.save(testTextlist,  sp )
        log.debug(""+res)
        res.isSuccess should be (true)
        val el = "aab" ::"baa"::"abc" :: "bca" :: "cdb" :: "zxv" :: "1234":: "1233":: "1232":: "1231" :: Nil
        log.debug(">>> "+el.filter(p=>p.matches("(.*)c(.*)")) )
        val res2 = testTextlist.saveEntryList(el)
        log.debug(""+testTextlist.loadEntryList())
        val r = testTextlist.findEntryList("(.*)c(.*)" :: "(.*)a(.*)" :: Nil, Some(3))
        log.debug(""+r)
      }

      "find test text list ci" in {
        val res = DB.etc.save(testTextlist,  sp )
        log.debug(""+res)
        res.isSuccess should be (true)
        val el = "123hello" ::"hEllo"::"HELLO" :: "hello123" :: "asdfgh" :: "qwert" :: Nil
        log.debug(">>> "+el.filter(p=>p.matches("(.*)c(.*)")) )
        val res2 = testTextlist.saveEntryList(el)
        log.debug(""+testTextlist.loadEntryList())
        val r = testTextlist.findEntryList("(.*)((?i)el)(.*)" :: Nil, Some(7))
        log.debug(""+r)
      }
      "load test text list" in {
        val res = DB.etc.get[ValidationTextList](testTextlist.id,  sp )

        log.debug(""+res)
        res.isDefined should be (true)
        val res2 =testTextlist.loadEntryList()
        log.debug(""+res2)
      }
      "save ip list2" in {
        val res = DB.etc.save(testIPlist2,  sp )
        log.debug(""+res)
        res.isSuccess should be (true)
      }
      "save text list" in {
        val res = DB.etc.save(testTextlist,  sp )
        log.debug(""+res)
        res.isSuccess should be (true)
      }
    }
}
