package com.assabetsecurity.core.data

import java.util.UUID
import com.assabetsecurity.core.db.{UUIDIdentifier, DataRecord, DataQuery, Identifier}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports.DBObject
import com.assabetsecurity.core.db.data.UserId
import org.joda.time.DateTime
import java.security.SecureRandom
import org.slf4s.Logging
import util.Random

/**

 * User: alyas
 * Date: 6/30/13
 * Time: 5:46 PM
  */

case class  RemoteApplicationId(value:UUID = UUID.randomUUID())  extends UUIDIdentifier {
  type D =  RemoteApplication
  def resource = manifest[D]
}
case object RemoteApplicationId {
  def apply(id:String) ={
    new RemoteApplicationId(UUID.fromString(id))
  }
}

case class  RemoteApplicationQuery(query:DBObject = MongoDBObject())  extends DataQuery {
  type D =  RemoteApplication
  def resource = manifest[ D ]
}

case class RemoteApplication(
                             id: RemoteApplicationId,
                             created: DateTime = new DateTime,
                             modified: DateTime = new DateTime,
                             name: String,
                             description: Option[String] = None,

                             /* list owner */
                             userId:Option[UserId] = None,
                             /* list scope - site only, if defined */
                             siteId:Option[SiteId] = None,

                             clientId:Option[String] = None, //Some(RemoteApplication.newClientId),
                             secret:Option[String] = None  //Some(RemoteApplication.newSecret)

                              ) extends DataRecord  {
}

object RemoteApplication extends Logging {
  def rnd = {
    new Random()
  }
  def newClientId:String = {
    val res = (for(i <- 1 to 24) yield {
      (rnd.nextInt(26)+65).toChar
    }).mkString
    log.info("new client id: "+res)
    res
  }
  def newSecret:String  = {
    (for(i <- 1 to 12) yield {
      (rnd.nextInt(10)+48).toChar
    }).mkString
  }
}
