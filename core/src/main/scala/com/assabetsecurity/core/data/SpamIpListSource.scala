package com.assabetsecurity.core.data

import org.slf4s.Logging
import scalaz.{Failure, Success, Validation}
import java.net.{HttpURLConnection, InetAddress}
import java.io.{FileOutputStream, BufferedOutputStream, InputStream, OutputStream}
import org.apache.commons.net.util.SubnetUtils

/**
 * User: alyas
 * Date: 10/9/13
 * Time: 7:13 PM
 */

trait SpamIpListSource extends Logging {
  def isInRange(mask:String, ip:String) = {
    new SubnetUtils(mask).getInfo.isInRange(ip)
  }
}

trait  SpamIpURISource extends SpamIpListSource {
  def sourceUrl:String

  /**
   * lifetime in seconds
   */
  def lifetime:Int

  def download(list:ValidationIpList) = {
    var out: OutputStream = null
    var in: InputStream = null

    try {
      val url = new java.net.URL(sourceUrl)
      val connection = url.openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("GET")
      in = connection.getInputStream
      val localFile = "data.txt"
      out = new BufferedOutputStream(new FileOutputStream(localFile))
      //val byteArray = Stream.continually(in.read).takeWhile(-1 !=).map(_.toByte).toArray

      var byteArray:Array[Byte] = Array.empty
      var cnt = 0
      do {
        byteArray = Stream.continually(in.read).take(1024*1024).takeWhile(-1 !=).map(_.toByte).toArray
        out.write(byteArray)
        cnt += byteArray.length
        log.debug("downloaded ["+sourceUrl+"]: "+cnt/1024+"K")
      } while(!byteArray.isEmpty)
      log.debug("downloaded file size: "+cnt)
    } catch {
      case e: Exception => println(e.printStackTrace())
    } finally {
      out.close
      in.close
    }
  }

}

case class SpamIp_stop_spam_org_bnbl_URISource() extends SpamIpURISource {
  def lifetime = 86400 //update every 24 hours
  def sourceUrl  = "http://tcats.stop-spam.org/bnbl/bnbl.txt"
}

case class SpamIp_stop_spam_org_nmsbl_URISource() extends SpamIpURISource {
  def lifetime = 86400 //update every 24 hours
  def sourceUrl  = "http://tcats.stop-spam.org/nmsbl/nmsbl.txt"
}

trait SpamIpListDNSSource extends SpamIpListSource {
  def dnsRoot:String
  def isSpamIP(ip:String):Validation[SpamIPValidationError, SpamIpValidationSuccess]
  def spamReturnCode:List[String]
}

trait SpamHausIpListSource extends SpamIpListDNSSource {
  def isSpamIP(ip:String) = {
    try {
      val ipDNS = ip.split('.').reverse.mkString(".") + dnsRoot
      val inetAddressArray = InetAddress.getAllByName(ipDNS)
      //log.debug(">>val res" + inetAddressArray.toSeq.map(_.getHostAddress).mkString(" : "))
      Success(SpamIpValidationSuccess(ip, !inetAddressArray.toSeq.filter(v=>spamReturnCode.contains(v.getHostAddress)).isEmpty, dnsRoot))
    }
    catch {
      case _:java.net.UnknownHostException =>{
        Success(SpamIpValidationSuccess(ip, false, dnsRoot))
      }
      case e:Throwable => {
        log.debug(e.getMessage+"::"+e.getClass)
        Failure(TimeOutSpamIPValidationError(ip))
      }
    }
  }
}

case class SpamHausIpListPBLSource(dnsRoot:String =".zen.spamhaus.org", blockList:String="sbl+xbl+pbl", updateDb:Boolean=true) extends SpamHausIpListSource{
  val spamReturnCode = "127.0.0.2" :: "127.0.0.3" :: "127.0.0.4" :: "127.0.0.5" :: "127.0.0.6" :: "127.0.0.7" :: "127.0.0.10" :: "127.0.0.11" :: Nil
}

case class SpamHausIpListXBLSource(dnsRoot:String =".zen.spamhaus.org", blockList:String="sbl+xbl", updateDb:Boolean=true) extends SpamHausIpListSource{
  val spamReturnCode = "127.0.0.2" :: "127.0.0.3" :: "127.0.0.4" :: "127.0.0.5" :: "127.0.0.6" :: "127.0.0.7" :: Nil
}

case class SpamHausIpListSBLSource(dnsRoot:String =".zen.spamhaus.org", blockList:String="sbl", updateDb:Boolean=true) extends SpamHausIpListSource{
  val spamReturnCode = "127.0.0.2" :: "127.0.0.3" :: Nil
}

