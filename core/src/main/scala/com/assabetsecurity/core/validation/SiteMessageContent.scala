package com.assabetsecurity.core.validation

import java.util.UUID

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 9/30/13
 * Time: 11:57 PM
 * To change this template use File | Settings | File Templates.
 */
/** *
  *
  * @param id
  * @param lang
  * @param config
  * @param serverIp
  * @param serverDomain
  * @param ip
  * @param trackingId internal cleint id - can be unique post number or message id. Will be returned back along
  *                   with validation results
  * @param userId
  * @param message
  */
case class SiteMessageContent(
                               id:UUID = UUID.randomUUID(),
                               lang:Option[String] = None,
                               config:Option[String]= None,
                               serverIp:Option[String]= None,
                               serverDomain: Option[String] = None,
                                /*client options*/
                               ip:Option[String]= None,

                               trackingId:Option[String] = None,
                               userId:Option[String] = None,
                               message:String = "",
                               messageFormat:Option[String] = Some("plain-text") /* plain-text | html | xhtml */)