package com.assabetsecurity.core.validation

import akka.actor.{Props, ActorSystem}

/**
  * User: alyas
 * Date: 8/18/13
 * Time: 2:54 PM
 */

object ValidationSystem {
  private val s = ActorSystem("ValidationSystem")
  private val d = s.actorOf(Props(new MessageValidationDispatcherActor), "mvd")
  def system = {s}
  def dispatcher = d

}
