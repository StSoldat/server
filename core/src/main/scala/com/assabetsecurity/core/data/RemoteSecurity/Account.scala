/*
package com.assabetsecurity.core.db.data

import org.joda.time.{DateTime, LocalTime}
import java.util.UUID
import util.Random
import com.assabetsecurity.core.db.{Identifier, DataRecord}

/**
 * User: alyas
 * Date: 4/6/13
 * Time: 3:45 PM
 */

trait UpdateStrategy

case class DailyUpdate(at:LocalTime = new LocalTime(0, 0, 0)) extends UpdateStrategy

case class AccountId(value:UUID = UUID.randomUUID()) extends Identifier {
  type E = Account

  def this(v:String) = {
    this(UUID.fromString(v))
  }
}

case class Account ( id:AccountId,
                     userName:String,
                     serverName:String,
                     domainName:Option[String] = None,
                     fullUserName:Option[String] = None,
                     description:Option[String] = None,
                     parentAccount:Option[UUID] = None,
                     keys:List[AccountKey] = List.empty,
                     lastPasswordUpdate:DateTime,
                     updateStrategy:UpdateStrategy) extends DataRecord {

}

case class AccountKeyId(value:UUID = UUID.randomUUID()) extends Identifier {
  type E = AccountKey

  def collectionName = "accountKey"

  def this(v:String) = {
    this(UUID.fromString(v))
  }
}

case class AccountKey(
                       id:AccountKeyId,
                       accountId:AccountId,
                       created:DateTime = new DateTime(),
                       startDate:DateTime,
                       endDate:DateTime,
                       isActive:Boolean = true,
                       value:String)  extends DataRecord {

}
case object AccountKey {
  def generateKey = {
    val md = java.security.MessageDigest.getInstance("SHA-256")
    val s = new sun.misc.BASE64Encoder().encode(md.digest(Random.nextString(256).getBytes))
    s
  }
}*/
