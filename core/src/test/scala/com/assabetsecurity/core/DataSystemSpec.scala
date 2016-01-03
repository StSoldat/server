package com.assabetsecurity.core


import akka.util.Timeout
import com.assabetsecurity.core.dataActors.DataSystem
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, WordSpec}
import org.slf4s.Logging


/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 8/18/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
class DataSystemSpec  extends WordSpec with ShouldMatchers with BeforeAndAfter with BeforeAndAfterAll with Logging {
  val system = DataSystem.system
  implicit val timeout = Timeout(10000)



  "system" should {
    "initiate akka" in {
      val v = DataSystem.system
      log.debug(v.name)
      v.name should equal("DataSystem")
      Thread.sleep(30000)
      //val future  = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSite2.id, m)
      //val res = (Await.result(future, timeout.duration)).asInstanceOf[ValidationResults]
    }
  }
}
