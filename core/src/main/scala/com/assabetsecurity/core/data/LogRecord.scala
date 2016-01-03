package com.assabetsecurity.core.data

import java.util.UUID
import com.assabetsecurity.core.db.{DataRecord, DataQuery, UUIDIdentifier}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import com.assabetsecurity.core.db.data.UserId


/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 11/1/13
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */
case class LogRecordId(value:UUID)  extends UUIDIdentifier {
  type D = LogRecord
  def resource = manifest[D]
}


case class LogRecordQuery(query:DBObject = MongoDBObject())  extends DataQuery {
  type D = LogRecord
  def resource = manifest[LogRecord]
}

case class LogRecord (
                         id: LogRecordId,
                         created: DateTime = new DateTime,
                         application: String,
                         level: String, //"debug|info|warn|error|system"
                         threadId: Option[String] = None,
                         server: Option[String] = None,
                         userId: Option[UserId] = None,
                         siteId: Option[SiteId] = None,
                         message: String,
                         messageData:Map[String, String] = Map.empty
                         ) extends DataRecord

