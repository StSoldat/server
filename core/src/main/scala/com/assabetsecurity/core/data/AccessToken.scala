package com.assabetsecurity.core.data

import java.util.UUID
import com.assabetsecurity.core.db.{UUIDIdentifier, DataRecord, DataQuery, Identifier}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports.DBObject
import com.assabetsecurity.core.db.data.{SecurityRoleEnum, UserSecurityRole, UserId}
import org.joda.time.DateTime

/**

 * User: alyas
 * Date: 6/30/13
 * Time: 5:46 PM
 */

case class  AccessTokenId(value:UUID = UUID.randomUUID())  extends UUIDIdentifier {
  type D =  AccessToken
  def resource = manifest[D]
}
case object AccessTokenId {
  def apply(id:String) ={
    new AccessTokenId(UUID.fromString(id))
  }
}

case class  AccessTokenQuery(query:DBObject = MongoDBObject())  extends DataQuery {
  type D =  AccessToken
  def resource = manifest[ D ]
}

case class AccessToken(
                              id: AccessTokenId,
                              created: DateTime = new DateTime,
                              modified: DateTime = new DateTime,

                              applicationId:Option[RemoteApplicationId] = None,
                              /* list scope - site only, if defined */
                              userId:Option[UserId] = None,
                              roles:List[SecurityRoleEnum.Value],
                              sites:List[SiteId] = List.empty //would be populated from DB on authentication
                              ) extends DataRecord


