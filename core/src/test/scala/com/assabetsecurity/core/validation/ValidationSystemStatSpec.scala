package com.assabetsecurity.core.validation

import java.util.UUID

import akka.actor.{Actor, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.assabetsecurity.core.data.{EmailValidationClass, HtmlValidationClass, Site, SiteConfig, TextValidationClass, _}
import com.assabetsecurity.core.db.data.UserId
import com.assabetsecurity.core.db.{DB, DBOSecurityPrincipal, PagingParameters, SiteCollectionVersion}
import com.mongodb.casbah.commons.MongoDBObject
import org.apache.commons.net.util.SubnetUtils
import org.joda.time.DateTime
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, WordSpec}
import org.slf4s.Logging

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.xml.{Node, XML}

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 8/18/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
class ValidationSystemStatSpec  extends WordSpec with ShouldMatchers with BeforeAndAfter with BeforeAndAfterAll with Logging {
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


  def testSite2 = Site( id = new SiteId(UUID.fromString("34009e87-66c6-4360-ad77-64cf34c730b9")),
    created = new DateTime,
    modified = new DateTime,
    description = Some("ValidationSystemSpec"),
    userId = Some(UserId()), //UserId(UUID.fromString("d6d8b74b-9843-4e72-9c33-7c94783ae726"))),
    uri = "ValidationSystemSpec.com",
    activeSiteConfig = "default",
    siteConfigs = Map(
      "email" -> SiteConfig( name = "email",
        validationClass = HtmlValidationClass(enabled = true, name  = "emailOnly") ::
          EmailValidationClass(
            enabled = true,
            name  = "emails",
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
            allowListId = Some(testEmalList2.id),
            blockListId = Some(testEmalList1.id)) ::

          IpValidationClass(enabled = true, name ="block spam  - ip - 2",
              allowListId = Some(testIpList2.id),
              blockListId = Some(testIpList.id)
          ) ::
          IpValidationClass(enabled = true, name ="block spam  - ip - 1",
            allowListId = Some(testIpList2.id),
            blockListId = Some(testIpList.id)
          ) ::

          TextValidationClass(enabled = true, name ="block spam - 1",
            allowListId = Some(testTextList2.id),
            blockListId = Some(testTextList.id)
          ) ::
          Nil
      )
    )
  )


  override def beforeAll() = {

  }

  "system" should {
    "check user stat" in {
      val version = Some( SiteCollectionVersion(testSite2.id))
      val q = MongoDBObject()
      def sp = DBOSecurityPrincipal()
      val shistory = DB.data.find[SiteMessageHistory](SiteMessageHistoryQuery(q), PagingParameters.empty, sp, version)

      log.debug(s"${shistory._1.size}")
      log.debug(s"${shistory._1.count(p=>p.lastModifiedBy.isDefined)}")

      shistory._1.groupBy(_.userId)

    }
  }
}
