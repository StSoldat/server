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

case class SiteMessageId(value:UUID)  extends UUIDIdentifier {
  type D = SiteMessage
  def resource = manifest[D]
}


case class SiteMessageQuery(query:DBObject = MongoDBObject())  extends DataQuery {


type D = SiteMessage
  def resource = manifest[SiteMessage]
}

case class SiteMessage (
    id: SiteMessageId,
    created: DateTime = new DateTime,
    modified: DateTime = new DateTime,
    validated:Option[DateTime],
    userId: Option[UserId] = None,
    siteId:Option[SiteId],
    content:  SiteMessageContent,
    contentHash:  Option[String] = None,
    result:List[ValidationResult],
    processingTime:Option[Int] = None,
    contentSize: Option[Int] = None,
    history:Option[SiteMessageHistory] = None //used in real-time requests only. Should be empty in db.
) extends DataRecord


