package com.assabetsecurity.core.validation

import akka.actor._
import net.liftweb.json._
import java.net.InetAddress
import com.assabetsecurity.core.data.{ValidationListId, SiteId}
import java.util.UUID
import akka.routing.RoundRobinRouter
import org.joda.time.DateTime


/**
 * User: alyas
 * Date: 8/18/13
 * Time: 1:55 PM
 */
class MessageValidationDispatcherActor extends Actor with ActorLogging {
  //def defaultConfigName = "default"
  //val siteActors:collection.mutable.HashMap[String, ActorRef] = new collection.mutable.HashMap()

  val messageValidationListCacheActor = context.system.actorOf(Props[MessageValidationListCacheActor], "validationListCache")

  val mailActor = context.system.actorOf(Props[MailNotificationActor], "MailNotificationActor")

  context.system.actorFor("user/MailNotificationActor") ! MailData("alyas77@gmail.com", Some("System MessageValidation"), Some("Started at: "+DateTime.now().toString()))
  log.info( ">> MessageValidationDispatcherActor" )

  def receive = {
    case m:ResetCache =>{
      log.info("reset cache message for: "+m.siteId)
      log.info("reset cache message forward to:"+ messageValidationListCacheActor.path)
      messageValidationListCacheActor.forward(m)
      m.siteId.foreach(v=>{
        context.child("MessageValidation_"+v.value.toString).foreach(a=>{
          a ! ResetCache(None)
        })
      })
    }
    case m:ResetCacheAll =>{
      log.info("ResetCacheAll")
      messageValidationListCacheActor.forward(m)
      context.children.foreach(a=>{
        a ! ResetCache(None)
      })
    }
    case m:SiteMessageValidationRequest => {
      //val configName = m.data.config.getOrElse(defaultConfigName)
      val path = "MessageValidation_"+m.siteId.value.toString
      context.child(path).getOrElse({
        //TODO site/use bandwidth monitoring actor or shared buffer for messages log
        context
          .actorOf(Props(new MessageValidationActor(m.siteId)).withRouter(RoundRobinRouter(nrOfInstances = 1)), path )
      }).forward(m.data)
    }
    case x =>
      log.debug("unknown message: "+x)
  }
}



case class SiteMessageValidationRequest(siteId:SiteId, data:SiteMessageContent)
case class ResetCache(siteId:Option[SiteId])
case class ResetCacheSuccess(siteId:Option[SiteId])
case class ResetCacheAll()


/*
object ValidationLevelEnum extends Enumeration {
  type ValidationLevel= Value
  val Default, Low,  Medium, High = Value
}

/**
 * validation options
 */
trait ValidationOption {
  def level:ValidationLevelEnum.Value
}

case class IpOptions(level:ValidationLevelEnum.Value = ValidationLevelEnum.Default) extends ValidationOption
case class TextOptions(level:ValidationLevelEnum.Value = ValidationLevelEnum.Default) extends ValidationOption
case class EmailOptions(level:ValidationLevelEnum.Value = ValidationLevelEnum.Default) extends ValidationOption
case class HtmlOption(level:ValidationLevelEnum.Value = ValidationLevelEnum.Default) extends ValidationOption



*/
