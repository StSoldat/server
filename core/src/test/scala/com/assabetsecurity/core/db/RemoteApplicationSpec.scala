package com.assabetsecurity.core.db


import org.scalatest.matchers.ShouldMatchers
import org.scalatest.WordSpec

import org.slf4s.Logging
import java.util.UUID
import com.assabetsecurity.core.data._

import com.assabetsecurity.core.data.ValidationTextList
import com.assabetsecurity.core.data.ValidationTextListId
import com.assabetsecurity.core.data.ValidationIpList
import scala.Some
import com.assabetsecurity.core.data.ValidationIpListId
import com.assabetsecurity.core.db.data.{SecurityRoleEnum, UserId}
import util.Random

/**
 * User: alyas
 * Date: 6/22/13
 * Time: 7:57 AM
 */
class RemoteApplicationSpec
  extends WordSpec with ShouldMatchers with Logging {

  def RemoteApplication1 =  new RemoteApplication (
    id = RemoteApplicationId(UUID.fromString("ce437562-074d-46be-8254-fed0f703571d")),
    name ="Test RemoteApp",
    siteId = Some(SiteId(UUID.fromString("26cddcda-54e1-4fee-9e08-4a455975008b")))
  )
  def accessToken = new AccessToken(
    id=AccessTokenId(UUID.randomUUID()),
    applicationId = Some(RemoteApplication1.id),
    roles = SecurityRoleEnum.MessageAPI ::Nil
  )
  def sp = DBOSecurityPrincipal()
    "db" should {
      "save remote App" in {
        log.debug(""+RemoteApplication1)
        DB.security.save(accessToken, sp)
      }
      "generate new app" in {
        val r1 =  new RemoteApplication (
          id = RemoteApplicationId(UUID.fromString("ce437562-074d-46be-8254-fed0f703571d")),
          name ="Test RemoteApp",
          siteId = Some(SiteId(UUID.fromString("26cddcda-54e1-4fee-9e08-4a455975008b")))
        )
        log.debug(""+r1)
        r1.clientId should not equal(RemoteApplication1.clientId)
      }
/*      "generate new app ids" in {
        DB.security.find[RemoteApplication](RemoteApplicationQuery(), sp).foreach(v=>{
          val r = v.copy(clientId = Some(RemoteApplication.newClientId), secret =  Some(RemoteApplication.newSecret))
          DB.security.save(r, sp)
        })
      }*/
    }
}
