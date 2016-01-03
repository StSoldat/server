package com.assabetsecurity.core.data

import java.util.UUID
import com.assabetsecurity.core.db.{UUIDIdentifier, DataRecord, DataQuery, Identifier}
import com.mongodb.casbah.Imports._

import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import com.assabetsecurity.core.db.data.UserId

/**
 * User: alyas
 * Date: 9/29/13
 * Time: 9:05 AM
 */


case class  ValidationResultLogId(value:UUID = UUID.randomUUID())  extends UUIDIdentifier {
  type D =  ValidationResultLog
  def resource = manifest[D]
}
case object ValidationResultLogId {
  def apply(id:String) ={
    new ValidationResultLogId(UUID.fromString(id))
  }
}

case class  ValidationResultLogQuery(query:DBObject = MongoDBObject())  extends DataQuery {
  type D =  ValidationResultLog
  def resource = manifest[ D ]
}

case class ValidationResultLog(
                             id: ValidationResultLogId,
                             created: DateTime = new DateTime,
                             modified: DateTime = new DateTime,
                             name: String,
                             description: Option[String] = None,

                             /* list owner */
                             userId:Option[UserId] = None,
                             /* list scope - site only, if defined */
                             siteId:Option[SiteId] = None,

                             entry: List[String] = List.empty,
                             isDeleted:Option[Boolean] = None
                             ) extends DataRecord


