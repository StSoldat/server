package com.assabetsecurity.core.validation

import com.assabetsecurity.core.data.{ValidationListId, SpamIpListDNSSource, ValidationIpList, ValidationList}
import org.slf4s.Logging
import org.joda.time.DateTime
import java.util.UUID

/**
  * User: alyas
 * Date: 10/20/13
 * Time: 10:03 PM
  */
trait BaseValidationData[A<:ValidationList] {
  def list:A
  def entryList:List[String]
  def entryFor(en:String):List[String] = entryList.filter(_==en)
  def loadedAt:DateTime
  def modified:DateTime
}


/**
 * validation data object to store in memory cache
 * @param list
 * @param entryList
 * @param loadedAt
 * @param modified
 * @tparam A
 */
class ValidationData[A<:ValidationList](val list:A, val entryList:List[String], val loadedAt:DateTime, val modified:DateTime) extends BaseValidationData[A]

/**
 * custom implementation for IP list to support external data sources
 * @param list
 * @param entryList
 * @param loadedAt
 * @param modified
 */
class ValidationDataIp(val list:ValidationIpList, val entryList:List[String], val loadedAt:DateTime, val modified:DateTime) extends BaseValidationData[ValidationIpList] with Logging {
  /**
   * loads entry status from external service if defined
   * uses local for empty source list
   * @param en
   * @return
   */
  override def entryFor(en:String):List[String] = {
     val r:List[String] = (for {
       src <- list.source
     } yield {
       log.info(">>>>> source "+src)
       src match {
         case s:SpamIpListDNSSource  => {
          val r = s.isSpamIP(en)
          log.info(">>>>> source SpamIpListDNSSource "+r)
          val res = r.toOption.map(v=>if (v.isSpamIp) Some(en) else None).flatten
          res.lastOption
         }
         case _=>{
           log.warn("unknown source: "+src.getClass)
           None
         }
       }
     }).flatten.toList
     if (r.isEmpty) super.entryFor(en) else r
  }
}
