package com.assabetsecurity.core.data

import com.assabetsecurity.core.db.{DataQuery, Identifier, DataRecord}
import org.joda.time.DateTime
import java.util.UUID
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.assabetsecurity.core.db.data.UserId


case class  ValidationEmailListId(value:UUID = UUID.randomUUID())  extends ValidationListId {
  type D =  ValidationEmailList
  def resource = manifest[D]
}
case object ValidationEmailListId {
  def apply(id:String) ={
    new ValidationEmailListId(UUID.fromString(id))
  }
}

case class  ValidationEmailListQuery(query:DBObject = MongoDBObject())  extends DataQuery {
  type D =  ValidationEmailList
  def resource = manifest[ D ]
}
case class ValidationEmailList(
                 id: ValidationEmailListId,
                 created: DateTime = new DateTime,
                 modified: DateTime = new DateTime,
                 name: String,
                 description: Option[String] = None,

                 /* list owner */
                 userId:Option[UserId] = None,

                 /* list scope - site only, if defined */
                 siteId:Option[SiteId] = None,


                 isDeleted:Option[Boolean] = None
) extends DataRecord  with ValidationList {

}


