package com.assabetsecurity.core.validation

import java.util.UUID

import akka.pattern.ask
import akka.util.Timeout
import com.assabetsecurity.core.data.{EmailValidationClass, HtmlValidationClass, Site, SiteConfig, TextValidationClass, _}
import com.assabetsecurity.core.db.data.UserId
import com.assabetsecurity.core.db.{DB, DBOSecurityPrincipal}
import com.assabetsecurity.core.validation.MessageValidationListCacheActor.GetValidationList
import org.joda.time.DateTime
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, WordSpec}
import org.slf4s.Logging

import scala.concurrent.Await

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 8/18/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
class MessageValidationListCacheActorSpec  extends WordSpec with ShouldMatchers with BeforeAndAfter with BeforeAndAfterAll with Logging {
  val system = ValidationSystem.system
  implicit val timeout = Timeout(5000)


  def testTextList = new ValidationTextList (
   id = ValidationTextListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8ba2")),
    name = "testTextList",
    language = Some(Language("eng"))
  )

  def testIpList = new ValidationIpList (
    id = ValidationIpListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8b3")),
    name = "testIptList"
  )

  def testIpList2 = new ValidationIpList (
    id = ValidationIpListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f823")),
    name = "testIptList2"
  )

  def testIpListExt = new ValidationIpList (
    id = ValidationIpListId(UUID.fromString("e93411a8-640a-4681-a043-03dae068729c")),
    name = "testIptListExt",
    source = SpamHausIpListPBLSource() :: Nil
  )




  def testHtmlList1 = new ValidationHtmlTagList (
    id = ValidationHtmlTagListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8b4")),
    name = "testHtmltagList"
  )

  def testEmalList1 = new ValidationEmailList (
    id = ValidationEmailListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8b5")),
    name = "testEmailList"
  )

  def testSiteExt = Site( id = new SiteId(UUID.fromString("34339e87-66c6-4360-ad77-64cf34c730b9")),
    created = new DateTime,
    modified = new DateTime,
    description = Some("ValidationSystemSpec"),
    userId = Some(UserId()), //UserId(UUID.fromString("d6d8b74b-9843-4e72-9c33-7c94783ae726"))),
    uri = "ValidationSystemSpec.com",
    activeSiteConfig = "default",
    siteConfigs = Map(
      "email" -> SiteConfig( name = "email",
        validationClass = HtmlValidationClass(enabled = true, name  = "htmlOnly") ::
          EmailValidationClass(
            enabled = true,
            name  = "emails",
            allowListId = None //("pupkin.com"::Nil)
          ) ::
          Nil
      ),
      "default" -> SiteConfig(name = "default",
        //HtmlValidationClass(enabled = true, name  = "htmlOnly", blockPlainText = true, blockXHTML5 = false) ::
        //EmailValidationClass(enabled = true, name  = "public only", allowPublicEmailDomains = true, allowPrivateEmailDomains = false) ::
        //IpValidationClass(enabled = true, name ="block spam1", blockListId = Some(ValidationIpListId())) ::
        //IpValidationClass(enabled = true, name ="block spam2", blockListId = Some(ValidationIpListId())) ::
        validationClass = IpValidationClass(
          enabled = true,
          name ="block spam  - ip - 1",
          blockListId = Some(testIpListExt.id)
        ) :: Nil
      )
    )
  )
  def testSite2 = Site( id = new SiteId(UUID.fromString("34009e87-66c6-4360-ad77-64cf34c730b9")),
    created = new DateTime,
    modified = new DateTime,
    description = Some("ValidationSystemSpec"),
    userId = Some(UserId()), //UserId(UUID.fromString("d6d8b74b-9843-4e72-9c33-7c94783ae726"))),
    uri = "ValidationSystemSpec.com",
    activeSiteConfig = "default",
    siteConfigs = Map(
      "email" -> SiteConfig( name = "email",
        validationClass = HtmlValidationClass(enabled = true, name  = "htmlOnly") ::
          EmailValidationClass(
            enabled = true,
            name  = "emails",
            allowListId = None //("pupkin.com"::Nil)
          ) ::
          Nil
      ),
      "default" -> SiteConfig(name = "default",
          //HtmlValidationClass(enabled = true, name  = "htmlOnly", blockPlainText = true, blockXHTML5 = false) ::
          //EmailValidationClass(enabled = true, name  = "public only", allowPublicEmailDomains = true, allowPrivateEmailDomains = false) ::
          //IpValidationClass(enabled = true, name ="block spam1", blockListId = Some(ValidationIpListId())) ::
          //IpValidationClass(enabled = true, name ="block spam2", blockListId = Some(ValidationIpListId())) ::
        validationClass = IpValidationClass(enabled = true, name ="block spam  - ip - 1",
            allowListId = Some(testIpList2.id),
            blockListId = Some(testIpList.id)
          ) ::
          //TextValidationClass(enabled = true, name ="block spam1", blockListId = Some(ValidationTextListId())) ::
          TextValidationClass(enabled = true, name ="block spam - 1",
            blockListId = Some(testTextList.id)) ::
          Nil
      )
    )
  )

  override def beforeAll() = {
     log.debug("Init db records")
     DB.etc.save(testSite2, DBOSecurityPrincipal())
     val el = "aab" ::"baa"::"abc" :: "bca" :: "cdb" :: "zxv" :: "ffck":: "ussr" :: Nil
     DB.etc.save(testTextList, DBOSecurityPrincipal())
     testTextList.saveEntryList(el)
     val elIp =  "2.2.3.4" :: "2.2.3.0/24" :: "3.2.2.0/24" :: "3.3.4.0/255.255.255.0" :: "2:2:3:4:5:6:7:8":: "3:2:3::/48" ::
        "1.2.3.4" :: "1.2.3.0/24" :: "2.2.2.0/24" :: "2.3.4.0/255.255.255.0" :: "1:2:3:4:5:6:7:8":: "1:2:3::/48" :: Nil
     DB.etc.save(testIpList, DBOSecurityPrincipal())
     testIpList.saveEntryList(elIp)

     DB.etc.save(testIpList2, DBOSecurityPrincipal())
     testIpList2.saveEntryList("2.2.2.10" :: "2.2.2.128/30" ::Nil)

     DB.etc.save(testIpListExt, DBOSecurityPrincipal())
     DB.etc.save(testSiteExt, DBOSecurityPrincipal())
  }

  "system" should {
    "initiate akka" in {
      val v = ValidationSystem.system
      log.debug(v.name)
      v.name should equal("ValidationSystem")
    }
    "Request cache reset" in {
      val future  = ValidationSystem.dispatcher ? ResetCache(Some(testSite2.id))
      val res = (Await.result(future, timeout.duration))
      log.debug(" :: " + res)
    }
    "load and update list" in {
      testTextList.loadEntryList().find(_=="cache test") should equal(None)
      DB.etc.get[ValidationTextList](testTextList.id, DBOSecurityPrincipal()).foreach(v=> log.debug("modified: "+v.modified))
      ValidationSystem.dispatcher
      val actor = ValidationSystem.system.actorFor("user/validationListCache")
      log.debug(""+actor.path)
      //val future  = actor  ? ResetCache(Some(testSite2.id))
      //val res = (Await.result(future, timeout.duration))
      //log.debug(" :: " + res)
      val future2  = actor  ? GetValidationList(testTextList.id)
      val res2 = (Await.result(future2, timeout.duration))
      //log.debug(" get #1 " + res2.asInstanceOf[ValidationData[ValidationTextList]].entryList.size)
      res2 match {
        case Some(r) => log.debug(" get #1 "+r.asInstanceOf[ValidationData[ValidationTextList]].entryList.size)
      }

      val el = "cache test" :: Nil
      DB.etc.save(testTextList, DBOSecurityPrincipal())
      testTextList.saveEntryList(el)

      val future3  = actor  ? GetValidationList(testTextList.id)
      val res3 = (Await.result(future3, timeout.duration))
      //log.debug(" get #1 " + res2.asInstanceOf[ValidationData[ValidationTextList]].entryList.size)
      res3 match {
        case Some(r) => log.debug(" get #2 "+r.asInstanceOf[ValidationData[ValidationTextList]].entryList.size)
      }
    }
  }
}
