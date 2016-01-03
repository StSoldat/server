package com.assabetsecurity.core.data

import java.util.UUID
import com.assabetsecurity.core.db.{DataRecord, DataQuery, Identifier}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports.DBObject
import com.assabetsecurity.core.db.data.UserId
import org.joda.time.DateTime
import com.mongodb.casbah.gridfs.GridFS
import scalaz.{Failure, Success, Validation}
import java.net.InetAddress
import org.slf4s.Logging

/**
 * User: alyas
 * Date: 6/30/13
 * Time: 5:46 PM
 */

case class  ValidationIpListId(value:UUID = UUID.randomUUID())  extends ValidationListId {
  type D =  ValidationIpList
  def resource = manifest[D]
}
case object ValidationIpListId {
  def apply(id:String) ={
    new ValidationIpListId(UUID.fromString(id))
  }
}

case class  ValidationIpListQuery(query:DBObject = MongoDBObject())  extends DataQuery {
  type D =  ValidationIpList
  def resource = manifest[ D ]
}

case class ValidationIpList(
                 id: ValidationIpListId,
                 created: DateTime = new DateTime,
                 modified: DateTime = new DateTime,
                 name: String,
                 description: Option[String] = None,

                 /* list owner */
                 userId:Option[UserId] = None,
                 /* list scope - site only, if defined */
                 siteId:Option[SiteId] = None,

                 source: List[SpamIpListSource] = List.empty,
                 isDeleted:Option[Boolean] = None
) extends DataRecord  with ValidationList  {

}




