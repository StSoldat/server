package com.assabetsecurity.core.dataActors

import java.io.FileInputStream
import java.util.UUID

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import com.assabetsecurity.core.data._
import com.assabetsecurity.core.db.{DB, DBOSecurityPrincipal, NamedCollectionVersion}
import com.assabetsecurity.core.db.data.{User, UserQuery}
import com.assabetsecurity.core.stat.{SiteStatActor, SiteStatActorInit}
import com.assabetsecurity.core.validation.{EmailValidationResultSuccess, HtmlValidationResultFailure, HtmlValidationResultFailureNoEntry, HtmlValidationResultSuccess, IpValidationResultFailure, IpValidationResultSuccess, TextValidationResultFailure, TextValidationResultSuccess, _}
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import org.slf4s.Logging

import scala.util.Random

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 1/22/14
 * Time: 1:22 PM
 * To change this template use File | Settings | File Templates.
 */
class SecurityActor  extends Actor with ActorLogging {

  init()

  def init() = {
    log.info("init SecurityActor")
    //val mailActor = context.actorOf(Props[MailNotificationActor])
    //mailActor ! MailData("alyas77@gmail.com", Some("CanModerate.com::System Start"), Some("Started at: "+ DateTime.now().toString()))
    log.info( "<< init SecurityActor" )
  }

  val rnd = new Random()
  val demoSiteId =  SiteId(UUID.fromString("b87c94b5-ef25-47ed-b488-ba031f5d2877"))

  var demoActor:Option[ActorRef] = None
  def receive = {
    case DemoUsersTimerTick()=>{
      demoActor.getOrElse({
        demoActor = Some(context.system.actorOf(Props(new SiteStatActor(demoSiteId)), "demo"+demoSiteId.value.toString))
      })

      demoActor.foreach(demoActor =>{
        demoActor ! SiteStatActorInit("Demo")

        log.debug("DemoUsersTimerTick at "+ (new DateTime()))

        val numberOfMessages = 100
        log.debug("generate records: " + numberOfMessages )
        val inRus = new FileInputStream("./data/messageContent-rus.html")
        val inEng = new FileInputStream("./data/messageContent-eng.html")
        val lines =
          (scala.io.Source.fromInputStream(inRus).getLines() ++   scala.io.Source.fromInputStream(inEng).getLines())
            .filter(_.size >=10).toIndexedSeq

        log.debug("Lines: "+lines.size)
        inRus.close()
        inEng.close()
        val secInDay = 86400
        val days = 3
        val date= DateTime.now.minusDays(days)



        val resultListIp =
          IpValidationResultSuccess() :: IpValidationResultSuccess() :: IpValidationResultSuccess() :: IpValidationResultFailure(entry = List.empty) ::
            HtmlValidationResultSuccess() :: HtmlValidationResultFailure() :: HtmlValidationResultFailureNoEntry() :: Nil
        val resultListText = TextValidationResultSuccess() :: TextValidationResultFailure(entry = List.empty) :: EmailValidationResultSuccess() :: EmailValidationResultFailure():: Nil


        for(m <- 1 to numberOfMessages) {
          def modified = date.plusMillis(rnd.nextInt(1000)).plusSeconds(rnd.nextInt(days*secInDay))
          log.debug("Message 'modified' date "+modified)
          val newContent = lines(rnd.nextInt(lines.size))
          val results = resultListIp.toIndexedSeq(rnd.nextInt(resultListIp.size)) :: resultListText(rnd.nextInt(resultListText.size)) :: Nil
          val m = messageEmpty.copy(
            id = SiteMessageId(UUID.randomUUID()),
            modified = modified,
            content = messageEmpty.content.copy(message = newContent),
            contentSize = Some(newContent.size),
            processingTime = Some(rnd.nextInt(2000)),
            result = results)
          demoActor ! m
        }
      })
    }
    case TimerTick() => {
      log.debug("TimerTick at "+ (new DateTime()))
      val usersToClean = DB.security.find[User](UserQuery(), DBOSecurityPrincipal())._1.filter(u=>{
        u.registrationTokenExpires.map(e=>e.isBeforeNow).getOrElse(false)
      })

      log.debug(">>>>>>>>>>>>>> usersToClean " + usersToClean.length)

      usersToClean.foreach(u=>{
        DB.security.save(u.copy(expires = DateTime.now()), DBOSecurityPrincipal() )
        val site = DB.etc.find[Site](SiteQuery(MongoDBObject("userId.value" ->u.id.value)), DBOSecurityPrincipal())._1

        site.headOption.foreach(s=>{
          log.debug("removing site: "+s)
          DB.etc.save(s.copy(isActive = Some(false)), DBOSecurityPrincipal())
        })
        log.debug(">>>>>>>>>>>>>> ");
        //DB.security.remove(u.id, DBOSecurityPrincipal())
      })

      //log.debug("usersToClean " + usersToClean.head.registrationTokenExpires)
    }
    case m =>{
      log.info("unknownMessage: "+m)
    }
  }

  private def messageEmpty = new SiteMessage(
    id = SiteMessageId(UUID.randomUUID()) ,
    created =  new DateTime(),
    validated = Some(new DateTime),
    siteId = None,
    content = new SiteMessageContent(message = "demo"),
    result = List.empty
  )
}
case class TimerTick()
case class DemoUsersTimerTick()