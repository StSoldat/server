package com.assabetsecurity.core.data
import com.assabetsecurity.core.db.{UUIDIdentifier, DataRecord, DataQuery, Identifier}
import java.util.UUID
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import com.mongodb.casbah.Imports.DBObject
import com.assabetsecurity.core.db.data.UserId
import com.assabetsecurity.core.validation.{SiteMessageContent, ValidationResult}

/**
 * User: alyas
 * Date: 6/23/13
 * Time: 10:18 AM
 */

case class SiteMessageHistoryId(value:UUID)  extends UUIDIdentifier {
  type D = SiteMessageHistory
  def resource = manifest[D]
}


case class SiteMessageHistoryQuery(query:DBObject = MongoDBObject())  extends DataQuery {


  type D = SiteMessageHistory
  def resource = manifest[SiteMessageHistory]
}

case class SiteMessageHistory (
                         id: SiteMessageHistoryId,
                         created: DateTime = new DateTime,
                         modified: DateTime = new DateTime,
                         history:List[SiteMessageHistoryItem],
                         lastModifiedBy:Option[UserId] = None,
                         trackingId:Option[String] = None,
                         userId:Option[String] = None,
                         lastItem:Option[SiteMessageHistoryItem]
                         ) extends DataRecord

case class SiteMessageHistorySimple (
                                      id: String,
                                      created: DateTime = new DateTime,
                                      modified: DateTime = new DateTime,
                                      history:List[SiteMessageHistoryItemSimple],
                                      lastModifiedBy:Option[String] = None,
                                      trackingId:Option[String] = None,
                                      userId:Option[String] = None)

case class SiteMessageHistoryItem(
                                   id:String = "",
                                   created: DateTime = new DateTime,
                                   createdByUserId: Option[UserId] = None,
                                   siteId:Option[SiteId],
                                   configName:Option[String],
                                   description:Option[String],
                                   result:List[ValidationResult]
                                   )

case class SiteMessageHistoryItemSimple(
                                   id:String = "",
                                   created: DateTime = new DateTime,
                                   createdByUserId: Option[String] = None,
                                   siteId:Option[String],
                                   configName:Option[String],
                                   description:Option[String],
                                   result:List[ValidationResult]
                                   )