package com.assabetsecurity.core.data

import java.util.UUID
import com.assabetsecurity.core.db.{DataRecord, DataQuery, Identifier}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports.DBObject
import com.assabetsecurity.core.db.data.UserId
import org.joda.time.DateTime

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 6/30/13
 * Time: 6:10 PM
 * To change this template use File | Settings | File Templates.
 */
case class  ValidationTextListId(value:UUID = UUID.randomUUID())  extends ValidationListId {
  type D =  ValidationTextList
  def resource = manifest[D]
}

case object ValidationTextListId {
  def apply(id:String) ={
    new ValidationTextListId(UUID.fromString(id))
  }
}
case class  ValidationTextListQuery(query:DBObject = MongoDBObject())  extends DataQuery {
  type D =  ValidationTextList
  def resource = manifest[ D ]
}

/**
 *
 * @param id
 * @param created
 * @param modified
 * @param name
 * @param description
 * @param userId
 * @param siteId
 * @param language http://en.wikipedia.org/wiki/ISO_639-3 language code. eng, fra, rus...
 */
case class ValidationTextList(
                             id: ValidationTextListId,
                             created: DateTime = new DateTime,
                             modified: DateTime = new DateTime,
                             name: String,
                             description: Option[String] = None,

                             userId:Option[UserId] = None,
                             /* list scope - site only, if defined */
                             siteId:Option[SiteId] = None,
                             //entry: List[String] = List.empty,

                             language: Option[Language] = None,
                             isDeleted:Option[Boolean] = None
                             ) extends DataRecord   with ValidationList {
}

case class Language(name:String)
