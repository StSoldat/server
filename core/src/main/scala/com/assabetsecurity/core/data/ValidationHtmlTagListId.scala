package com.assabetsecurity.core.data

import com.assabetsecurity.core.db.{DataQuery, Identifier, DataRecord}
import org.joda.time.DateTime
import java.util.UUID
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.assabetsecurity.core.db.data.UserId


case class  ValidationHtmlTagListId(value:UUID = UUID.randomUUID())  extends ValidationListId {
  type D =  ValidationHtmlTagList
  def resource = manifest[D]
}
case object ValidationHtmlTagListId {
  def apply(id:String) ={
    new ValidationHtmlTagListId(UUID.fromString(id))
  }
}

case class  ValidationHtmlTagListQuery(query:DBObject = MongoDBObject())  extends DataQuery {
  type D =  ValidationHtmlTagList
  def resource = manifest[ D ]
}
case class ValidationHtmlTagList(
                                id: ValidationHtmlTagListId,
                                created: DateTime = new DateTime,
                                modified: DateTime = new DateTime,
                                name: String,
                                description: Option[String] = None,

                                /* list owner */
                                userId:Option[UserId] = None,

                                /* list scope - site only, if defined */
                                siteId:Option[SiteId] = None,

                                //entry: List[String] = List.empty
                                isDeleted:Option[Boolean] = None
                                ) extends DataRecord with ValidationList {

}


