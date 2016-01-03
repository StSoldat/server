package com.assabetsecurity.core.db

import org.slf4s.Logging
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import com.assabetsecurity.core.data.{SiteId, SiteMessageId, SiteMessage}
import java.util.UUID
import org.joda.time.DateTime
import com.assabetsecurity.core.validation.SiteMessageContent

/**

 * User: alyas
 * Date: 6/23/13
 * Time: 11:26 AM

 */
class SiteMessageSpec extends WordSpec with ShouldMatchers with Logging {
  def sp = DBOSecurityPrincipal()
  val message1 = new SiteMessage(
    id = SiteMessageId(UUID.randomUUID()) ,
    created =  new DateTime(),
    validated = Some(new DateTime),
    siteId = Some(new SiteId(UUID.randomUUID())),
    content = new SiteMessageContent(message = "Unit-Test"),
    result = List.empty
  )
  "db " should {
    "save new message" in {
      val res = DB.data.save(message1,  sp )
      log.debug("res: "+res)

      res.fail.toOption.map(v=>fail(v.e))

      val getRes = DB.data.get(res.toOption.get.id,  sp)
      log.debug("get res: "+getRes)
    }
  }
}
