package com.assabetsecurity.core.dataActors

import akka.actor.{Props, ActorSystem}
import akka.util.{Timeout}
import scala.concurrent.duration._
import scala.concurrent.duration.Duration
/**
  * User: alyas
 * Date: 8/18/13
 * Time: 2:54 PM
 */

object DataSystem {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val s = ActorSystem("DataSystem")
  def system = {s}
  val securityActor = s.actorOf(Props(new SecurityActor), "securityActor")

  def start() = {
    DataSystem.system.scheduler.schedule(Timeout(10 seconds).duration, Timeout(60 seconds).duration, securityActor, TimerTick())
    //DataSystem.system.scheduler.schedule(Duration(10000, TimeUnit.HOURS), Duration(24, TimeUnit.HOURS), securityActor, TimerTick())
    DataSystem.system.scheduler.schedule(Timeout(5 seconds).duration, Timeout(24 hours).duration, securityActor, DemoUsersTimerTick())
  }

}

