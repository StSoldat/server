package com.assabetsecurity.core.tools

import org.slf4s.Logging
import com.assabetsecurity.core.db.data.{UserId, User}
import akka.actor.ActorSystem
import com.assabetsecurity.core.db.{DBOSecurityPrincipal, DB}
import java.util.UUID


/**
 * User: alyas
 * Date: 6/21/13
 * Time: 11:28 PM
 */

  object UpdateDB extends App with Logging {
    val system = ActorSystem()
    cmdRun
    def cmdRun() = {
      log.debug(">>> run UpdateDB")
      val u = new User(  id = new UserId(UUID.randomUUID()),
        loginName = "test")
      DB.security.save(u, new DBOSecurityPrincipal)
    }
}
