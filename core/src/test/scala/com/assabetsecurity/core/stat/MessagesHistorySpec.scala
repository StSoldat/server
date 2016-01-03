package com.assabetsecurity.core.stat

import java.io.FileInputStream
import java.util.UUID

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.assabetsecurity.core.data._
import com.assabetsecurity.core.db._
import com.assabetsecurity.core.validation.{IpValidationResultFailure, IpValidationResultSuccess, SiteMessageContent, _}
import org.joda.time.DateTime
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.slf4s.Logging

import scala.concurrent.Await
import scala.util.Random

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 10/27/13
 * Time: 12:26 AM
 * To change this template use File | Settings | File Templates.
 */
class MessagesHistorySpec extends WordSpec with ShouldMatchers with Logging {

      "merge messages and history" in {
        val id = "a322a3d1-dd24-4c7f-894e-92830b51a637"
        val version = Some( SiteCollectionVersion(SiteId(UUID.fromString(id))))
        val q= JsonQuery[SiteMessageHistory](Some("""{"history.createdByUserId":{$exists:true}}"""))

        val history = DB.data.find[SiteMessageHistory] (
          SiteMessageHistoryQuery(),
          PagingParameters(),
          DBOSecurityPrincipal(),
          version
        )
        log.debug("history Size "+history._1.size)
        history._1.foreach(h=>{
          DB.data.save(h.copy(lastItem = h.history.sortBy(_.created.getMillis).lastOption), DBOSecurityPrincipal(), version)
        })
        //log.debug("history Size "+history._1.filter(_.history.s))
    }

}
