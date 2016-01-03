package com.assabetsecurity.core.validation

import com.assabetsecurity.core.data._
import org.apache.commons.net.util.SubnetUtils
import collection.mutable
import com.assabetsecurity.core.validation.IpValidationResultFailure
import com.assabetsecurity.core.validation.IpValidationResultSuccess
import scala.Some
import com.assabetsecurity.core.validation.SiteMessageContent
import com.assabetsecurity.core.data.IpValidationClass

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 10/14/13
 * Time: 11:29 PM
 * To change this template use File | Settings | File Templates.
 */
object IpValidationClassProcessorImpl extends ValidationClassProcessor {

  def validate(m:SiteMessageContent, data:MessageValidationActorData, siteConfig:SiteConfig):List[ValidationResult] = {
    //log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>IpValidationClassProcessorImpl "+siteConfig)
    val res:List[ValidationResult] = (for {
      ip <- m.ip.toList
      vClass:IpValidationClass <- getValidationClass[IpValidationClass](siteConfig).toList
    } yield {
      //log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>IpValidationClassProcessorImpl "+ip)
      log.debug("to validate: "+ip)

      val allowIps = vClass.allowListId.map(lid=>{
        data.getIpValidationData(lid).map(_.entryFor(ip)).toList.flatten
      }).toList.flatten

      val blockIps = vClass.blockListId.map(lid=>{
        data.getIpValidationData(lid).map(_.entryFor(ip)).toList.flatten
      }).toList.flatten


      val blockIpsSubnets = for{
        lid <- vClass.blockListId.toList
        lst <- data.getIpValidationData(lid).toList
        e <- lst.entryList if(checkSubnet(ip, e))
      } yield {
        e
      }

      val allowIpsSubnets = for{
        lid <- vClass.allowListId.toList
        lst <- data.getIpValidationData(lid).toList
        e <- lst.entryList if(checkSubnet(ip, e))
      } yield {
        e
      }

      //prepopulate
      val isBlocked = !blockIps.isEmpty
      val isBlockIpsSubnet = !blockIpsSubnets.isEmpty
      val isAllowed = !allowIps.isEmpty
      val isAllowIpsSubnet = !allowIpsSubnets.isEmpty

      //log.debug(" isAllowed: "+isAllowed )
      //log.debug(" isBlocked: "+isBlocked )

      //log.debug(" isAllowIpsSubnet: "+isAllowIpsSubnet )
      //log.debug(" isBlockIpsSubnet: "+isBlockIpsSubnet )

      val fentry = blockIpsSubnets ++  blockIps
      val res = vClass match {
        case _ if(isAllowed || isAllowIpsSubnet) => {
          None //Some(IpValidationResultSuccess(vcid = Some(vClass.vcid.toString)))
        }
        case _ if (isBlocked || isBlockIpsSubnet) => {
          val blockList = vClass.blockListId.map(lid=>{
            data.getIpValidationData(lid)
          }).flatten.lastOption

          Some(IpValidationResultFailure(
            listName = blockList.map(_.list.name),
            listId = blockList.map(_.list.id),
            vcid = Some(vClass.vcid.toString),
            entry=fentry)
          )
        }
        case _=>
          None
      }
      res
    }).toList.flatten
    if(res.isEmpty) (IpValidationResultSuccess() :: Nil) else res
  }


  val checkSubnetCache:mutable.HashMap[Int, Boolean] =  new mutable.HashMap()
  def checkSubnetCacheMaxSize  = 1024*1024
  /**
   * returns true if ip matches subnet
   * @param ip
   * @param entry
   * @return
   */
  def checkSubnet(ip:String, entry:String) ={
    val h = (ip+"!"+entry).hashCode
    checkSubnetCache.get(h).getOrElse({
      val r = if (entry.contains('/')) {
        try {
          try {
            new SubnetUtils(entry).getInfo.isInRange(ip)
          } catch {
            case _:Throwable => {
              val e = entry.split('/')
              new SubnetUtils(e(0), e(1)).getInfo.isInRange(ip)
            }
          }
        } catch {
          case _:Throwable
          => false
        }
      } else {
        false
      }
      checkSubnetCache(h) = r
      if(checkSubnetCache.size>checkSubnetCacheMaxSize) {
        log.warn("checkSubnetCache reset")
        checkSubnetCache.clear()
      }
      r
    })
  }
}
