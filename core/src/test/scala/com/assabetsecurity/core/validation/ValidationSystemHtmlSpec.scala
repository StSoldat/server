package com.assabetsecurity.core.validation

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, WordSpec}
import org.scalatest.matchers.ShouldMatchers
import org.slf4s.Logging
import java.util.UUID
import com.assabetsecurity.core.data._
import akka.actor.{Actor, Props}
import akka.pattern.ask
import akka.util.Timeout
import org.joda.time.DateTime
import com.assabetsecurity.core.db.data.UserId

import com.assabetsecurity.core.data.Site
import com.assabetsecurity.core.data.EmailValidationClass
import scala.Some
import com.assabetsecurity.core.data.TextValidationClass
import com.assabetsecurity.core.data.SiteConfig
import com.assabetsecurity.core.data.HtmlValidationClass
import com.assabetsecurity.core.db.{DBOSecurityPrincipal, DB}
import org.apache.commons.net.util.SubnetUtils
import scala.concurrent.Await
import scala.xml.{Node, XML}
import scala.collection.mutable.ListBuffer

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 8/18/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
class ValidationSystemHtmlSpec  extends WordSpec with ShouldMatchers with BeforeAndAfter with BeforeAndAfterAll with Logging {
  val system = ValidationSystem.system
  implicit val timeout = Timeout(20000)


  def testTextList = new ValidationTextList (
   id = ValidationTextListId(UUID.fromString("1d654150-887c-475d-87a0-f1e43a2f8ba2")),
    name = "testTextList",
    language = Some(Language("eng")),
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

  def testSiteHtmlEmpty = Site( id = new SiteId(UUID.fromString("34009e87-66c6-4360-ad77-64cf34c730a8")),
    created = new DateTime,
    modified = new DateTime,
    description = Some("ValidationSystemSpec"),
    userId = Some(UserId()), //UserId(UUID.fromString("d6d8b74b-9843-4e72-9c33-7c94783ae726"))),
    uri = "ValidationSystemSpec.com",
    activeSiteConfig = "default",
    siteConfigs = Map(
      "default" -> SiteConfig(name = "default",

        validationClass =
          HtmlValidationClass(enabled = true, name  = "htmlOnly", blockInvalidHTML = true, blockXss = true,
            allowListId = None,
            blockListId = None
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
     log.debug("Init db records")
     DB.etc.save(testSite2, DBOSecurityPrincipal())

    DB.etc.save(testSiteHtmlEmpty, DBOSecurityPrincipal())

     DB.etc.save(testTextList, DBOSecurityPrincipal())
     testTextList.saveEntryList( "lllsssttt" :: "aab" ::"baa"::"abc" :: "bca" :: "cdb" :: "zxv" :: "ffck":: "ussr" :: Nil)

     DB.etc.save(testTextList2, DBOSecurityPrincipal())
     testTextList2.saveEntryList( "ussrA" :: Nil)


    val elIp = "aab" ::"baa" :: "abc" ::
        "2.2.3.4" :: "2.2.3.0/24" :: "3.2.2.0/24" :: "3.3.4.0/255.255.255.0" :: "2:2:3:4:5:6:7:8":: "3:2:3::/48" ::
        "1.2.3.4" :: "1.2.3.0/24" :: "2.2.2.0/24" :: "2.3.4.0/255.255.255.0" :: "1:2:3:4:5:6:7:8":: "1:2:3::/48" :: Nil
     DB.etc.save(testIpList, DBOSecurityPrincipal())
     testIpList.saveEntryList(elIp)

    DB.etc.save(testHtmlList1, DBOSecurityPrincipal())
    testHtmlList1.saveEntryList(List("script", "meta", "a", "span"))

    DB.etc.save(testHtmlList2, DBOSecurityPrincipal())
    testHtmlList2.saveEntryList(List("span"))

     DB.etc.save(testEmalList1, DBOSecurityPrincipal())
     testEmalList1.saveEntryList(List("a@a.com", "*@b.com", "c.com", "@c.com", "domain.com", "allow.com"))

    DB.etc.save(testEmalList2, DBOSecurityPrincipal())
    testEmalList2.saveEntryList(List("allow.com"))

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
    "test xml" in {
      val message = "<html>test message<b><div><form></form></div></b> html<script>alert</script></html>"
      val dataXml = XML.loadString(message)

      val lst:ListBuffer[Node] = new ListBuffer()

      def allChildren(root:Seq[Node]):Unit =  {
        root.foreach(node=>{
           lst.append( node )
           allChildren( node.child )
         })
      }
      allChildren(dataXml.child)
      lst.foreach(n=>{
        log.debug(""+n.label)
      })
    }

    "validate  valid html" in {
      val message = "<html>test message</html>"
      val m = SiteMessageContent(
        trackingId=Some(UUID.randomUUID().toString),
        lang = None,
        config = None,
        serverIp = None,
        ip = Some("134.174.21.27"),
        message = message)

      val future  = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSite2.id, m)
      val res = (Await.result(future, timeout.duration)).asInstanceOf[ValidationResults]
      res.results.foreach(res=>log.debug(" :: "+res))
      res.asInstanceOf[ValidationResults].results.find(v=>v.isInstanceOf[HtmlValidationResultSuccess]).size should equal(1)
    }
    "validate  valid html not allowed tag" in {

      val message = "<html>test message<a href=\"skdjfhsjdk\">hjhfdsjfhskd</a></html>"
      val m = SiteMessageContent(
        trackingId=Some(UUID.randomUUID().toString),
        lang = None,
        config = None,
        serverIp = None,
        ip = Some("134.174.21.27"),
        message = message)

      val future  = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSite2.id, m)
      val res = (Await.result(future, timeout.duration)).asInstanceOf[ValidationResults]
      res.results.foreach(res=>log.debug(" :: "+res))
      res.asInstanceOf[ValidationResults].results.find(v=>v.isInstanceOf[HtmlValidationResultFailure]).size should equal(1)
    }
    "validate  valid html not allowed tag in allow list" in {

      val message = "<html>test message<span>aass</span></html>"
      val m = SiteMessageContent(
        trackingId=Some(UUID.randomUUID().toString),
        lang = None,
        config = None,
        serverIp = None,
        ip = Some("134.174.21.27"),
        message = message)

      val future  = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSite2.id, m)
      val res = (Await.result(future, timeout.duration)).asInstanceOf[ValidationResults]
      res.results.foreach(res=>log.debug(" :: "+res))
      res.asInstanceOf[ValidationResults].results.find(v=>v.isInstanceOf[HtmlValidationResultFailure]).size should equal(0)
    }
    "validate  broken html" in {
      val message = "<html>test message</htmla>"
      val m = SiteMessageContent(
        trackingId=Some(UUID.randomUUID().toString),
        lang = None,
        config = None,
        serverIp = None,
        ip = Some("134.174.21.27"),
        message = message)

      val future  = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSite2.id, m)
      val res = (Await.result(future, timeout.duration)).asInstanceOf[ValidationResults]
      res.results.foreach(res=>log.debug(" :: "+res))
      res.asInstanceOf[ValidationResults].results.find(v=>v.isInstanceOf[HtmlValidationResultFailureNoEntry]).size should equal(1)
    }
    "validate  broken html with empty lists" in {
      val message = "<html>test message</htmla>"
      val m = SiteMessageContent(
        trackingId=Some(UUID.randomUUID().toString),
        lang = None,
        config = None,
        serverIp = None,
        ip = Some("134.174.21.27"),
        message = message)

      val future  = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSiteHtmlEmpty.id, m)
      val res = (Await.result(future, timeout.duration)).asInstanceOf[ValidationResults]
      res.results.foreach(res=>log.debug(" :: "+res))
      res.asInstanceOf[ValidationResults].results.find(v=>v.isInstanceOf[HtmlValidationResultFailureNoEntry]).size should equal(1)
    }
    "validate  unclosed html" in {
      val message = "some text <a>test message</a> here as well"
      val m = SiteMessageContent(
        trackingId=Some(UUID.randomUUID().toString),
        lang = None,
        config = None,
        serverIp = None,
        ip = Some("134.174.21.27"),
        message = message)

      val future  = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSite2.id, m)
      val res = (Await.result(future, timeout.duration)).asInstanceOf[ValidationResults]
      res.results.foreach(res=>log.debug(" :: "+res))
      res.asInstanceOf[ValidationResults].results.find(v=>v.isInstanceOf[HtmlValidationResultFailure]).size should equal(1)
    }
  }
}
