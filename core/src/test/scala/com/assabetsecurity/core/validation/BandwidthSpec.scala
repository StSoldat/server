package com.assabetsecurity.core.validation

import java.util.{Properties, UUID}
import javax.mail.{Transport, Message, PasswordAuthentication, Session}
import javax.mail.internet.{InternetAddress, MimeMessage}

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
class BandwidthSpec extends WordSpec with ShouldMatchers with BeforeAndAfter with BeforeAndAfterAll with Logging {

  val system = ValidationSystem.system

  implicit val timeout = Timeout(20000)
  def testSiteExt2 = Site(
    id = new SiteId(UUID.fromString("34339e87-6776-4360-ad77-64cf34c730bb")),
    created = new DateTime,
    modified = new DateTime,
    description = Some("ValidationSystemSpec"),
    userId = Some(UserId()), //UserId(UUID.fromString("d6d8b74b-9843-4e72-9c33-7c94783ae726"))),
    uri = "ValidationSystemSpec.com",
    activeSiteConfig = "default",
    siteConfigs = Map(
      "default" -> SiteConfig(name = "default",
        validationClass =
          BandwidthValidationClass(
            enabled = true,
            name ="bw val",
            blockThreshold = 7,
            period = 7,
            userLevel = true,
            subject = Some("Test S"),
            sendTo = Some("email@test"),
            body = Some("Notification Body")
          ) ::
            IpValidationClass(
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
  def testSiteExt = Site(
    id = new SiteId(UUID.fromString("34339e87-6776-4360-ad77-64cf34c730bb")),
    created = new DateTime,
    modified = new DateTime,
    description = Some("ValidationSystemSpec"),
    userId = Some(UserId()), //UserId(UUID.fromString("d6d8b74b-9843-4e72-9c33-7c94783ae726"))),
    uri = "ValidationSystemSpec.com",
    activeSiteConfig = "default",
    siteConfigs = Map(
      "default" -> SiteConfig(name = "default",
        validationClass =
        BandwidthValidationClass(
          enabled = true,
          name ="bw val",
          blockThreshold = 3,
          period = 5,
          userLevel = true,
          subject = Some("Test S"),
          sendTo = Some("email@test"),
          body = Some("Notification Body")
        ) ::
        IpValidationClass(
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

  //def spamIP = "175.44.56.37"
  "Bandwidth" should {
    "validate 10 messages per sec" in {

      val m = SiteMessageContent(
        trackingId=Some(UUID.randomUUID().toString),
        lang = None,
        config = None,
        serverIp = None,
        message = "test message")


      for(i <- 1 to 20) {
        val future  = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSiteExt.id, m)
        val res = (Await.result(future, timeout.duration)).asInstanceOf[ValidationResults]
        res.results.foreach(res=>log.debug(" :: "+res))
        Thread.sleep(50)
      }

      Thread.sleep(5000)
      //res.asInstanceOf[ValidationResults].results.find(v=>v.isInstanceOf[IpValidationResultFailure]).size should equal(1)

    }

      "validate 10 messages per sec for user" in {

        val m = SiteMessageContent(
          trackingId=Some(UUID.randomUUID().toString),
          lang = None,
          config = None,
          serverIp = None,
          userId  = Some("123"),
          message = "test message")


        for(i <- 1 to 20) {
          val future  = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSiteExt.id, m)
          val res = (Await.result(future, timeout.duration)).asInstanceOf[ValidationResults]
          res.results.foreach(res=>log.debug(" :: "+res))
          Thread.sleep(50)
        }

        Thread.sleep(5000)
        //res.asInstanceOf[ValidationResults].results.find(v=>v.isInstanceOf[IpValidationResultFailure]).size should equal(1)

      }

    "send test message" in {
      val props = new Properties()

      props.put("mail.smtp.ssl.enable", "true");
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
      props.put("mail.smtp.host", "xjdz4.dailyrazor.com")
      props.put("mail.transport.protocol", "smtp");
      props.put("mail.smtp.port", "465")
      props.put("mail.smtp.timeout", "2000");

      val session = Session.getInstance(props,
        new javax.mail.Authenticator() {

          override def getPasswordAuthentication():PasswordAuthentication  = {
            new PasswordAuthentication("service@assabetsecurity.com", "sdfr23432");
          }

        });

      session.setDebug(true)
      val message = new MimeMessage(session)

      message.setRecipient(Message.RecipientType.TO, new InternetAddress("alyas77@gmail.com"))

      message.setFrom(new InternetAddress("noreply@canmoderate.com"))

      message.setSubject("CanModerate")

      message.setText("test")
      log.info("SMTP >> send... ")
      //Transport.send(message);
      //val transport = session.getTransport()
      //transport.connect("service@assabetsecurity.com", "sdfr23432")
      //transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
      log.info("SMTP >> send complete")
      Thread.sleep(20000)
    }
    "validate text message and reset cache" in {
      val message = "some test message"
      val m = SiteMessageContent(
        trackingId=Some("111222333"),
        lang = None,
        config = None,
        serverIp = None,
        ip = Some("10.0.0.1"),
        messageFormat = Some("plain-text"),
        message = message)
      for(i <- 1 to 5) {
        val future = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSiteExt.id, m)
        val res = (Await.result(future, timeout.duration)).asInstanceOf[ValidationResults]
        res.results.foreach(res => log.debug(" :: " + res))
      }
      DB.etc.save(testSiteExt2, DBOSecurityPrincipal())
      ValidationSystem.dispatcher ? ResetCache(Some(testSiteExt.id))
      for(i <- 1 to 5) {
        val future = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSiteExt.id, m)
        val res = (Await.result(future, timeout.duration)).asInstanceOf[ValidationResults]
        res.results.foreach(res => log.debug(" :: " + res))
      }
      //res.asInstanceOf[ValidationResults].results.find(v=>v.isInstanceOf[HtmlValidationResultFailureNoEntry]).size should equal(1)
    }
  }
}
