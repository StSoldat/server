package com.assabetsecurity.core.validation

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import com.assabetsecurity.core.db.data.UserId
import net.liftweb.json._
import com.assabetsecurity.core.data._
import java.net.InetAddress
import com.assabetsecurity.core.db.{SiteCollectionVersion, DBOSecurityPrincipal, DB}

import com.assabetsecurity.core.data.ValidationTextList
import com.assabetsecurity.core.data.ValidationIpList
import com.assabetsecurity.core.data.Site
import collection.mutable
import org.joda.time.{ Seconds, Period, DateTime}
import com.assabetsecurity.core.stat.{SiteStatActorInit, SiteStatActor}
import com.assabetsecurity.core.validation.MessageValidationListCacheActor.GetValidationList
import akka.pattern.ask
import akka.util.Timeout
import org.slf4s.Logging
import com.assabetsecurity.core.security.Sha256
import java.util.UUID

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * User: alyas
 * Date: 8/18/13
 * Time: 1:56 PM
 */

trait MessageValidationLists extends Logging {
  def cacheActor:ActorRef

  implicit val timeout = Timeout(5000)

  def getValidationData[A<:BaseValidationData[_]:Manifest](id:ValidationListId): Option[A] = {
     val future  = cacheActor  ? GetValidationList(id)
     val res = (Await.result(future, timeout.duration))
     res match {
       case Some(l) if(l.isInstanceOf[A @unchecked]) =>{
         l match {
           case l:A  => Some(l)
           case _=> None
         }
       }
       case _=> {
         log.error("List not found: "+id)
         None
       }
     }
  }
}

trait MessageValidationActorData extends MessageValidationLists {
  def siteId:SiteId

  def site:Option[Site] = None

  def getIpValidationData(id:ValidationListId):Option[ValidationDataIp] = {
    val r = getValidationData[ValidationDataIp](id)
    r
  }
  def getTextValidationData(id:ValidationListId):Option[ValidationData[ValidationTextList]] = {
    val r = getValidationData[ValidationData[ValidationTextList]](id)
    r
  }

  def getEmailValidationData(id:ValidationListId):Option[ValidationData[ValidationEmailList]] = {
    val r = getValidationData[ValidationData[ValidationEmailList]](id)
    r
  }

  def getHtmlValidationData(id:ValidationListId):Option[ValidationData[ValidationHtmlTagList]] = {
    val r = getValidationData[ValidationData[ValidationHtmlTagList]](id)
    r
  }

  def siteConfigs = site.map(_.siteConfigs.map(_._2)).toList.flatten.toList

}




class MessageValidationActor(val siteId:SiteId) extends Actor with MessageValidationActorData with Logging {
  val cacheActor = context.system.actorFor("user/validationListCache")
  val mailActor = context.system.actorFor("user/MailNotificationActor")

  val statActor = context.actorOf(Props(new SiteStatActor(siteId)))


  override def site = _site
  var _site = DB.etc.get[Site](siteId, DBOSecurityPrincipal())

  init
  def  init = {
    log.info("===========================================")
    log.info("initialize actor for  "+siteId.value)
    log.info("initialize actor path "+this.self.path)
    log.info("  site: "+site.map(s=>s.id+" : "+s.name))
    log.info("===========================================")



    siteId.synchronized {
      loadValidationLists
    }
  }

  def loadValidationLists = {
    log.info("load validation lists")
    try {
      for {
        siteConfig <- siteConfigs
        vclass <- siteConfig.validationClass
      } {
        vclass match {
          case c: ValidationClass => {
            (c.allowListId.toSeq ++ c.blockListId).foreach(id => {
              getValidationData(id)
            })
          }
          case _ =>
            log.warn("undefined config type for " + vclass)
        }
      }
    } catch {
      case e:Throwable => log.error("Lists loading error: "+e.getMessage)
    }
    statActor ! SiteStatActorInit
  }

  class UsageData(val userId:Option[String], val siteConfigName:String, val date:DateTime, val success:Int, val failure:Int)

  val usageBuffer = new mutable.ListBuffer[UsageData]()

  def siteConfig(name:String) = site.map(_.siteConfigs.get(name)).flatten

  def receive = {
    case ResetCache(id) => {

      siteId.synchronized{ loadValidationLists }
      _site = DB.etc.get[Site](siteId, DBOSecurityPrincipal())
      log.info("ResetCache for site "+ siteId.value +" isActive: "+site.map(_.isActive.getOrElse(true)))
    }
    case m:SiteMessageContent => {
      log.info("ValidationMessage  for site "+ siteId.value)
      if(site.map(_.isActive.getOrElse(true))==Some(true)) {
        val started = System.currentTimeMillis
        val activeConfigName = m.config.getOrElse(site.get.activeSiteConfig)

        siteConfig(activeConfigName).map(siteConfig => {

          val res = if (m.message.isEmpty && m.ip.isEmpty && m.serverIp.isEmpty)
          //empty message - return oll korrect :)
            IpValidationResultSuccess() ::
              TextValidationResultSuccess() ::
              EmailValidationResultSuccess() ::
              HtmlValidationResultSuccess() :: Nil
          else {
            //do actual validation
            IpValidationClassProcessorImpl.validate(m, this, siteConfig) ++
              TextValidationClassProcessorImpl.validate(m, this, siteConfig) ++
              HtmlValidationClassProcessorImpl.validate(m, this, siteConfig) ++
              EmailValidationClassProcessorImpl.validate(m, this, siteConfig)
          }


          val storeContent = siteConfig.storeContent.getOrElse(true)
          val storeContentHash = siteConfig.storeContent.getOrElse(true)


          usageBuffer += new UsageData(
            m.userId,
            siteConfigName = siteConfig.name,
            new DateTime().withMillisOfSecond(0),
            res.count(v => v.isInstanceOf[ValidationResultSuccess]),
            res.count(v => v.isInstanceOf[ValidationResultFailure])
          )

          val bwRes = siteConfig.validationClass
            .filter(_.isInstanceOf[BandwidthValidationClass])
            .filter(_.enabled)
            .map(_.asInstanceOf[BandwidthValidationClass])
            .map(c => {
              log.debug(s"User Level${c.userLevel} userId:${m.userId} in Buffer ${usageBuffer.count(d => m.userId == d.userId)}")
              val filterAfter = DateTime.now().minusSeconds(c.period)

              val filtered = usageBuffer
                .filter(_.siteConfigName == siteConfig.name)
                .filter(_.date.isAfter(filterAfter))
                .filter(d => {
                if (c.userLevel) {
                  m.userId == d.userId
                } else {
                  true
                }
              })

              log.info(s"filtered buffer: ${filtered.size} of ${usageBuffer.size}")

              val count = filtered.count(f => f.failure > 0 && c.countFailure || f.success > 0 && c.countSuccess)

              log.info(s"BandwidthValidation: count ${count} threshold ${c.blockThreshold} period ${c.period}")

              //5 messages, block on 5. 5 would be fine, 6th we block with email.rest of messages
              if (count > c.blockThreshold) {
                //send message once, but keep adding Block to results
                if (count == c.blockThreshold + 1) {
                  c.sendTo.foreach(sendTo => {
                    log.debug(s"Send message to ${sendTo}")
                    mailActor ! MailData(sendTo, c.subject, c.body)
                  })
                }
                Some(BandwidthValidationResultFailure(period = Some(c.period), count = Some(count)))
              }
              else None
          }).flatten

          val message = new SiteMessage(
            id = new SiteMessageId(m.id),
            validated = Some(new DateTime()),
            siteId = Some(this.siteId),
            content = if (storeContent) m else m.copy(message = ""),
            contentHash = if (storeContentHash) Some(Sha256.hex_digest(m.message.toString)) else None,
            result = res ++ bwRes,
            contentSize = Some(m.message.size),
            processingTime = Some((System.currentTimeMillis - started).toInt)
          )

          //respond to caller
          context.sender ! ValidationResults(
            results = message.result,
            id = m.id.toString,
            trackingId = m.trackingId,
            userId=m.userId,
            config = Some(activeConfigName)
          )

          //save statistic amd message data
          statActor ! message
          DB.data.save(message, sp = DBOSecurityPrincipal(), Some(SiteCollectionVersion(siteId)))

          if ((siteConfig.postValidationFailure.getOrElse(false) && !res.find(v => v.isInstanceOf[ValidationResultFailure]).isEmpty) ||
            (siteConfig.postValidationSuccess.getOrElse(false) && res.find(v => v.isInstanceOf[ValidationResultFailure]).isEmpty)) {
            log.debug("save postValidation")
            var history = List(new SiteMessageHistoryItem(
              id = UUID.randomUUID().toString,
              created = DateTime.now,
              createdByUserId = None,
              siteId = Some(this.siteId),
              configName = Some(siteConfig.name),
              description = None,
              result = res ++ bwRes
            ))
            val mh = SiteMessageHistory(id =
              SiteMessageHistoryId(message.id.value),
              created = DateTime.now,
              modified = DateTime.now,
              trackingId = message.content.trackingId,
              userId = message.content.userId,
              history = history,
              lastItem = history.lastOption
            )

            DB.data.save(mh, sp = DBOSecurityPrincipal(), Some(SiteCollectionVersion(siteId)))
          }
        })
      } else {
        context.sender ! SiteIsNotActive(siteId.value.toString)
      }
    }
    case m => {
      log.info("mva -- unknown message: "+m)
    }
  }


}


