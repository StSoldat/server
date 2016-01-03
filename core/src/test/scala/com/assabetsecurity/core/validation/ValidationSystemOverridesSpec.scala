package com.assabetsecurity.core.validation

import java.util.UUID

import akka.pattern.ask
import akka.util.Timeout
import com.assabetsecurity.core.data.{EmailValidationClass, HtmlValidationClass, Site, SiteConfig, _}
import com.assabetsecurity.core.db.data.UserId
import com.assabetsecurity.core.db.{DB, DBOSecurityPrincipal}
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
class ValidationSystemOverridesSpec  extends WordSpec with ShouldMatchers with BeforeAndAfter with BeforeAndAfterAll with Logging {
  val system = ValidationSystem.system
  implicit val timeout = Timeout(20000)


  def testTextList = new ValidationTextList (
   id = ValidationTextListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8ba2")),
    name = "testTextList",
    language = Some(Language("eng")) ,
    siteId = Some(SiteId(UUID.randomUUID()))
  )
  def testTextList2 = new ValidationTextList (
    id = ValidationTextListId(UUID.fromString("1d654150-887c-489d-87a0-f1e43a2f8ba2")),
    name = "testTextList2",
    language = Some(Language("eng")),
    siteId = Some(SiteId(UUID.randomUUID()))
  )
  def testIpList = new ValidationIpList (
    id = ValidationIpListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8b3")),
    name = "testIptList",
    siteId = Some(SiteId(UUID.randomUUID()))
  )

  def testIpList2 = new ValidationIpList (
    id = ValidationIpListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f823")),
    name = "testIptList2",
    siteId = Some(SiteId(UUID.randomUUID()))
  )

  def testIpListExt = new ValidationIpList (
    id = ValidationIpListId(UUID.fromString("e93411a8-640a-4681-a043-03dae068729c")),
    name = "testIptListExt",
    siteId = Some(SiteId(UUID.randomUUID())),
    source = SpamHausIpListPBLSource() :: Nil
  )


  def testHtmlList1 = new ValidationHtmlTagList (
    id = ValidationHtmlTagListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8b4")),
    name = "testHtmltagList",
    siteId = Some(SiteId(UUID.randomUUID()))
  )
  def testHtmlList2 = new ValidationHtmlTagList (
    id = ValidationHtmlTagListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2dab4")),
    name = "testHtmltagList2",
    siteId = Some(SiteId(UUID.randomUUID()))
  )

  def testEmalList1 = new ValidationEmailList (
    id = ValidationEmailListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8b5")),
    name = "testEmailList",
    siteId = Some(SiteId(UUID.randomUUID()))
  )
  def testEmalList2 = new ValidationEmailList (
    id = ValidationEmailListId(UUID.fromString("1d654150-898c-475d-87a0-f1e43a2f8b5")),
    name = "testEmailList2",
    siteId = Some(SiteId(UUID.randomUUID()))
  )


  def testSite3 = Site( id = new SiteId(UUID.fromString("34009e87-66c6-4360-ad77-64cf12c980b9")),
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
            blockAllEmails = Some(true),
            allowListId = None //("pupkin.com"::Nil)
          ) ::
          Nil
      ),
      "default" -> SiteConfig(name = "default",
        validationClass =
          HtmlValidationClass(enabled = true, name  = "htmlOnly", blockInvalidHTML = true, blockXss = true,
            allowListId = Some(testHtmlList2.id),
            blockListId = Some(testHtmlList1.id)
          ) ::
            EmailValidationClass(enabled = true, name  = "public only",
              blockAllEmails = Some(false)
              ) ::
            Nil
      )
    )
  )
  def testSite4 = Site( id = new SiteId(UUID.fromString("34009ea7-66c6-5360-ad79-64cf12c980b9")),
    created = new DateTime,
    modified = new DateTime,
    description = Some("ValidationSystemSpec"),
    userId = Some(UserId()), //UserId(UUID.fromString("d6d8b74b-9843-4e72-9c33-7c94783ae726"))),
    uri = "ValidationSystemSpec.com",
    activeSiteConfig = "email",
    siteConfigs = Map(
      "email" -> SiteConfig( name = "email",
        validationClass = HtmlValidationClass(enabled = true, name  = "htmlOnly") ::
          EmailValidationClass(
            enabled = true,
            name  = "emails",
            blockAllEmails = Some(true),
            allowListId = None //("pupkin.com"::Nil)
          ) ::
          Nil
      ),
      "default" -> SiteConfig(name = "default",
        validationClass =
          HtmlValidationClass(enabled = true, name  = "htmlOnly", blockInvalidHTML = true, blockXss = true,
            allowListId = Some(testHtmlList2.id),
            blockListId = Some(testHtmlList1.id)
          ) ::
            EmailValidationClass(enabled = true, name  = "public only",
              blockAllEmails = Some(false)
            ) ::
            Nil
      )
    )
  )
  override def beforeAll() = {
     log.debug("Init db records")

     DB.etc.save(testSite3, DBOSecurityPrincipal())
     DB.etc.save(testSite4, DBOSecurityPrincipal())
  }

  "system" should {
    "initiate akka" in {
      val v = ValidationSystem.system
      log.debug(v.name)
      v.name should equal("ValidationSystem")
    }
    "default config" in {
      val m = SiteMessageContent(
        trackingId=Some("111222333"),
        lang = None,
        config = None,
        serverIp = None,
        ip = Some("3.3.3.4"),
        message = "Test Message a@a.com")
      val future  = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSite3.id, m)
      val res = Await.result(future, timeout.duration)
      log.debug("res:"+res)
      res.asInstanceOf[ValidationResults].results.find(v=>v.isInstanceOf[EmailValidationResultSuccess]).size should equal(1)
    }
    "email override config in message" in {
      val m = SiteMessageContent(
        trackingId=Some("111222333"),
        lang = None,
        config = Some("email"),
        serverIp = None,
        ip = Some("3.3.3.4"),
        message = "Test Message a@a.com")
      val future  = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSite3.id, m)
      val res = Await.result(future, timeout.duration)
      log.debug("res:"+res)
      res.asInstanceOf[ValidationResults].results.find(v=>v.isInstanceOf[EmailValidationResultFailure]).size should equal(1)

    }
    "override default config" in {
      val m = SiteMessageContent(
        trackingId=Some("111222333"),
        lang = None,
        config = Some("email"),
        serverIp = None,
        ip = Some("3.3.3.4"),
        message = "Test Message")
      val future  = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSite4.id, m)
      val res = Await.result(future, timeout.duration)
      log.debug("res:"+res)
      res.asInstanceOf[ValidationResults].results.find(v=>v.isInstanceOf[EmailValidationResultSuccess]).size should equal(1)
    }
  }
}
