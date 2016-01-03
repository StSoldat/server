package com.assabetsecurity.core.validation

import akka.actor.{ActorLogging, Actor}
import collection.mutable
import com.assabetsecurity.core.data._
import org.joda.time.DateTime
import com.assabetsecurity.core.db.{DBOSecurityPrincipal, DB}
import com.assabetsecurity.core.db.DBOSecurityPrincipal

/**
  * User: alyas
 * Date: 11/30/13
 * Time: 6:35 PM
 */
//single actor to store cache of validation lists
class MessageValidationListCacheActor extends Actor with ActorLogging {
  import MessageValidationListCacheActor._

  val validationListCache:mutable.HashMap[ValidationListId, BaseValidationData[_]] = mutable.HashMap()

  def expired(d:DateTime) = {
    d.isAfter(DateTime.now().plusSeconds(600))
  }

  def receive  = {
    case m:ResetCache =>{
      log.debug("reset cached lists for site: "+m)
      for {
        sid <-  m.siteId
        site <- DB.etc.get[Site](sid, DBOSecurityPrincipal())
        conf <- site.siteConfigs.values
        vc <- conf.validationClass
        vid <- vc.allowListId ++ vc.blockListId
      } {
        log.info("Reset "+vid)
      }
      sender ! ResetCacheSuccess(m.siteId)
    }
    case m:ResetCacheAll =>{
      log.debug("ResetCacheAll")
    }
    case m:ResetValidationListCache => {
      log.debug("reset cached lists:")
      m.list.foreach(id=>{
        log.info("reset list: "+id)
        validationListCache.remove(id)
      })
    }
    case m:GetValidationList  => {
      //log.info("get for "+m.id)
      val lst = validationListCache.get(m.id)
      lst.foreach(l=>{
        //hard reload
        if (expired(l.loadedAt)) {
          log.warning(">>>>>>>> reset expired list : "+m.id)
          validationListCache.remove(m.id)
        }
      })
      m.id match {
        case id:ValidationIpListId => loadList(id)
        case id:ValidationTextListId => loadList(id)
        case id:ValidationHtmlTagListId => loadList(id)
        case id:ValidationEmailListId => loadList(id)
      }
      sender ! validationListCache.get(m.id)
    }

    case x => {
      log.error("Unknown request" + x )
    }
  }

  private def loadList[T<:ValidationList:Manifest](id:ValidationListId { type D <: T}) = {
    //log.warning(">>>1 to load id: "+id)
    DB.etc.get[T](id, DBOSecurityPrincipal()).foreach(r =>{
      if(validationListCache.get(id).isEmpty ||  validationListCache.get(id).map(_.modified.isBefore(r.modified)).getOrElse(false)) {
        val rd =  r.asInstanceOf[ValidationList]
        log.warning(">>>2 to load id: "+id)
        log.warning(">>>2 to load id: "+id.getClass)
        id match {
          case ValidationIpListId(_) =>
            validationListCache(id) = new ValidationDataIp(r.asInstanceOf[ValidationIpList], rd.loadEntryList(), loadedAt = DateTime.now, modified = rd.modified)
          case l:ValidationListId =>
            validationListCache(id) = new ValidationData[ValidationList](rd, rd.loadEntryList(), loadedAt = DateTime.now, modified = rd.modified)
          case _ =>
        }
      }
    })
  }
}



object MessageValidationListCacheActor {
  //get request to retrieve validation list
  case class GetValidationList(id:ValidationListId)
  case class ResetValidationListCache(list:List[ValidationListId])
}