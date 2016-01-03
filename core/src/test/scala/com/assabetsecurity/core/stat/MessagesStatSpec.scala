package com.assabetsecurity.core.stat

import java.io.FileInputStream
import java.util.UUID

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.assabetsecurity.core.data._
import com.assabetsecurity.core.db._
import com.assabetsecurity.core.validation.{IpValidationResultFailure, IpValidationResultSuccess, SiteMessageContent, _}
import org.joda.time.DateTime
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.slf4s.Logging

import scala.concurrent.Await
import scala.util.Random

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 10/27/13
 * Time: 12:26 AM
 * To change this template use File | Settings | File Templates.
 */
class MessagesStatSpec extends WordSpec with ShouldMatchers with Logging {
  val system = ValidationSystem.system
  implicit val timeout = Timeout(50000)

  val testSiteId = SiteId(UUID.fromString("01a65144-aa07-4243-9ce9-76ac5311299b"))

  val messageEmpty = new SiteMessage(
    id = SiteMessageId(UUID.randomUUID()) ,
    created =  new DateTime(),
    validated = Some(new DateTime),
    siteId = Some(testSiteId),
    content = new SiteMessageContent(message = "Unit-Test"),
    result = List.empty
  )
  "find messages" in {
    val testSiteId =  SiteId(UUID.fromString("24dd7d82-5070-44a3-8e76-99f2811fad3d"))


  }
  "generate messages" in {
    val testSiteId =  SiteId(UUID.fromString("b87c94b5-ef25-47ed-b488-ba031f5d2877"))
    val a = system.actorOf(Props(new SiteStatActor(testSiteId)))
    log.debug(a.path.toString)
    a ! SiteStatActorInit("UnitTest")

    val rnd = new Random()
    val inRus = new FileInputStream("./data/messageContent-rus.html")
    val inEng = new FileInputStream("./data/messageContent-eng.html")
    val lines =
      (scala.io.Source.fromInputStream(inRus).getLines() ++   scala.io.Source.fromInputStream(inEng).getLines())
        .filter(_.size >=10).toIndexedSeq

    log.debug("Lines: "+lines.size)
    inRus.close()
    inEng.close()
    val secInDay = 86400
    val days = 60
    val date= DateTime.now.minusDays(days).minusDays(70)

    val numberOfMessages = days*100


    //val vcids = "e7518293-0721-401b-96d4-5f5bdb122f4d\nccc1c042-43e8-495f-a643-981012ccb1fa\n26cc747c-b541-49cd-9ab0-39a6a7ac0247\nbaec2c94-076d-4ff5-b43d-ad8d84fe13ed\n5902cf5b-e8c5-4014-98ce-7eb63eb7ec30".lines.toIndexedSeq
    val resultListIp =
      IpValidationResultSuccess() :: IpValidationResultSuccess() :: IpValidationResultSuccess() :: IpValidationResultFailure(entry = List.empty) ::
        HtmlValidationResultSuccess() :: HtmlValidationResultFailure() :: HtmlValidationResultFailureNoEntry() :: Nil
    val resultListText = TextValidationResultSuccess() :: TextValidationResultFailure(entry = List.empty) :: EmailValidationResultSuccess() :: EmailValidationResultFailure():: Nil

    //val resultListText = TextValidationResultSuccess() :: TextValidationResultFailure(entry = List.empty) :: Nil

    //log.debug(""+vcids.mkString("::") )

    for(m <- 1 to numberOfMessages) {
      def modified = date.plusMillis(rnd.nextInt(1000)).plusSeconds(rnd.nextInt(days*secInDay))
      log.debug(""+modified)
      val newContent = lines(rnd.nextInt(lines.size))
      //val vcid = vcids(rnd.nextInt(vcids.size))
      val results = resultListIp.toIndexedSeq(rnd.nextInt(resultListIp.size)) :: resultListText(rnd.nextInt(resultListText.size)) :: Nil
      val m = messageEmpty.copy(
        id = SiteMessageId(UUID.randomUUID()),
        modified = modified,
        content = messageEmpty.content.copy(message = newContent),
        contentSize = Some(newContent.size),
        processingTime = Some(rnd.nextInt(2000)),
        result = results)
      a ! m
    }
    Thread.sleep(120000)
    //messageEmpty.copy()
  }
  "MessagesStatSpec " should {
    "init akka" in {
      val v = ValidationSystem.system
      log.debug(v.name)
      v.name should equal("ValidationSystem")
    }
    "do SiteStatActorInit" in {
      val a = system.actorOf(Props(new SiteStatActor(testSiteId)))
      log.debug(a.path.toString)
      a ! SiteStatActorInit("UnitTest")
      Thread.sleep(20000)
    }
    "RequestSiteMessageStatisticData" in {
      val a = system.actorOf(Props(new SiteStatActor(testSiteId)))
      log.debug(a.path.toString)
      a ! SiteStatActorInit("UnitTest")
      a ! messageEmpty
      val future = a ? RequestSiteMessageStatisticData(DateTime.now(), DateTime.now(), "hour")
      val res = (Await.result(future, timeout.duration))
      log.debug(""+res)
    }
    "merge messages and history" in {
      val id = "a322a3d1-dd24-4c7f-894e-92830b51a637"
      val version = Some( SiteCollectionVersion(SiteId(UUID.fromString(id))))

      val history = DB.data.find[SiteMessageHistory] (
        SiteMessageHistoryQuery(),
        PagingParameters(),
        DBOSecurityPrincipal(),
        version
      )
      log.debug("history Size "+history._1.size)
    }
  }
}
