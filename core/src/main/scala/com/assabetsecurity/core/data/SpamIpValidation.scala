package com.assabetsecurity.core.data

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 10/9/13
 * Time: 7:14 PM
 * To change this template use File | Settings | File Templates.
 */
trait  SpamIpValidation {
  def ip:String
}

trait SpamIPValidationError extends SpamIpValidation
case class TimeOutSpamIPValidationError(ip:String) extends SpamIPValidationError

case class SpamIpValidationSuccess(ip:String, isSpamIp:Boolean, dnsRoot:String) extends SpamIpValidation