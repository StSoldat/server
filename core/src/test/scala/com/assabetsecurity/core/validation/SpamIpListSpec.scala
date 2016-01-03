package com.assabetsecurity.core.validation

import java.util.UUID

import akka.pattern.ask
import akka.util.Timeout
import com.assabetsecurity.core.data.{Site, SpamIp_stop_spam_org_nmsbl_URISource, _}
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
 * Date: 10/9/13
 * Time: 10:44 PM
 * To change this template use File | Settings | File Templates.
 */
class SpamIpListSpec extends WordSpec with ShouldMatchers with BeforeAndAfter with BeforeAndAfterAll with Logging {
  val system = ValidationSystem.system
  implicit val timeout = Timeout(20000)

  def testSiteExt = Site( id = new SiteId(UUID.fromString("34339e87-6776-4360-ad77-64cf34c730b9")),
    created = new DateTime,
    modified = new DateTime,
    description = Some("ValidationSystemSpec"),
    userId = Some(UserId()), //UserId(UUID.fromString("d6d8b74b-9843-4e72-9c33-7c94783ae726"))),
    uri = "ValidationSystemSpec.com",
    activeSiteConfig = "default",
    siteConfigs = Map(
      "default" -> SiteConfig(name = "default",
        validationClass = IpValidationClass(
          enabled = true,
          name ="SpamHaus PBL validation",
          blockListId = Some(testIpListExt1.id)
        ) :: IpValidationClass(
          enabled = true,
          name ="SpamHaus XBL validation",
          blockListId = Some(testIpListExt2.id)
        ) :: Nil
      )
    )
  )
  def testIpListExt1 = new ValidationIpList (
    id = ValidationIpListId(UUID.fromString("74728881-401a-428f-8392-e0c424113f13")),
    name = "SpamHaus PBL",
    description = Some("External Spamhaus Block List PBL+(SBL+XBL). http://www.spamhaus.org/pbl"),
    source = SpamHausIpListPBLSource() :: Nil
  )
  def testIpListExt2 = new ValidationIpList (
    id = ValidationIpListId(UUID.fromString("3dd538bf-fbd9-457c-9ad9-f376e7295898")),
    name = "SpamHaus XBL",
    description = Some("External Spamhaus Policy Block List. http://www.spamhaus.org/xbl"),
    source = SpamHausIpListXBLSource() :: Nil
  )
  def testIpListExt3 = new ValidationIpList (
    id = ValidationIpListId(UUID.fromString("804d4557-b4a3-4211-9468-f55ff5861d68")),
    name = "SpamHaus SBL",
    description = Some("External Spamhaus Policy Block List. http://www.spamhaus.org/sbl"),
    source = SpamHausIpListSBLSource() :: Nil
  )
  override def beforeAll() = {
    log.debug("Init db records")
    DB.etc.save(testSiteExt, DBOSecurityPrincipal())
    DB.etc.save(testIpListExt1, DBOSecurityPrincipal())
    DB.etc.save(testIpListExt2, DBOSecurityPrincipal())
    DB.etc.save(testIpListExt3, DBOSecurityPrincipal())

  }
  def spamIP= "112.101.64.12"
  //def spamIP = "175.44.56.37"
  "SpamList" should {
    "validate PBL" in {
      val s = SpamHausIpListPBLSource()
      log.debug("PBL:"+ s.isSpamIP(spamIP))
    }
    "validate XBL" in {
      val s = SpamHausIpListXBLSource()
      log.debug("XBL:"+ s.isSpamIP(spamIP))
    }
    "validate SBL" in {
      val s = SpamHausIpListSBLSource()
      log.debug("SBL:"+ s.isSpamIP(spamIP))
      log.debug("SBL:"+ s.isSpamIP("127.0.0.4"))
    }
    "validate spam IP via service" in {
      val m = SiteMessageContent(
        trackingId=Some(UUID.randomUUID().toString),
        lang = None,
        config = None,
        serverIp = None,
        ip = Some(spamIP),
        message = "test message")

      val future  = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSiteExt.id, m)
      val res = (Await.result(future, timeout.duration)).asInstanceOf[ValidationResults]
      res.results.foreach(res=>log.debug(" :: "+res))
      res.asInstanceOf[ValidationResults].results.find(v=>v.isInstanceOf[IpValidationResultFailure]).size should equal(1)

    }
    "validate mask" in {
      val mask = "12.10.10.240/28"
      val s = SpamIp_stop_spam_org_nmsbl_URISource()
      log.debug("1:"+ s.isInRange(mask,  "12.10.10.1"))
      log.debug("2:"+ s.isInRange(mask,  "12.10.10.244"))
    }
  }
}
