package com.assabetsecurity.core.security

import java.util.UUID
import org.joda.time.DateTime
import com.assabetsecurity.core.db.data.{UserSecurityRole, UserId, SecurityRoleEnum}
import com.assabetsecurity.core.data.{ValidationListId, SiteId}
import com.assabetsecurity.core.db.{Identifier, AuthenticatedSecurityPrincipal}

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 9/30/13
 * Time: 11:36 PM
 * To change this template use File | Settings | File Templates.
 */

trait BaseAccessToken {
  def id:UUID
  def clientId:String
  def expires:DateTime
  def scope:List[SecurityRoleEnum.Value]
  def sessionInfo:SessionAuthInfo
  def sites:List[SiteId]

  def sp = {
    AuthenticatedSecurityPrincipal(sessionInfo.user, expires)
  }
}

case class AccessTokenData( id:UUID = UUID.randomUUID() ,
                            clientId:String,
                            expires:DateTime,
                            scope:List[SecurityRoleEnum.Value],
                            sessionInfo:SessionAuthInfo = SessionAuthInfo(None, List.empty),
                            sites:List[SiteId] = List.empty
                            )  extends BaseAccessToken

case class SessionAuthInfo(user:Option[UserId],
                           sessionRoles:List[SecurityRoleEnum.Value],
                           userSecurityRoles:List[UserSecurityRole] = List.empty)

case class DownloadAccessTokenData( id:UUID = UUID.randomUUID() ,
                            clientId:String = "download",
                            expires:DateTime = DateTime.now().plusMinutes(5),
                            scope:List[SecurityRoleEnum.Value] = SecurityRoleEnum.Download :: Nil,
                            sessionInfo:SessionAuthInfo = SessionAuthInfo(None, List.empty),
                            sites:List[SiteId] = List.empty,
                            resourceId:Identifier[_]
                            )  extends BaseAccessToken

object DownloadAccessTokenData {
  def apply(id:UUID, token:BaseAccessToken, resourceId:Identifier[_]):DownloadAccessTokenData = {
    DownloadAccessTokenData(id=id, sessionInfo=token.sessionInfo, sites=token.sites, resourceId = resourceId)
  }
}