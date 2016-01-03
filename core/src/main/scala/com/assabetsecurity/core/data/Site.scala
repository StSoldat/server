package com.assabetsecurity.core.data

import java.util.UUID
import com.assabetsecurity.core.db.{UUIDIdentifier, DataRecord, DataQuery, Identifier}
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import com.assabetsecurity.core.db.data._
import com.mongodb.casbah.Imports.DBObject
/**
 * User: alyas
 * Date: 6/23/13
 * Time: 11:36 AM

 */

case class  SiteId(value:UUID = UUID.randomUUID())  extends UUIDIdentifier {
  type D =  Site
  def resource = manifest[D]
}

case object SiteId {
  def apply(id:String) ={
    new SiteId(UUID.fromString(id))
  }
}

case class  SiteQuery(query:DBObject = MongoDBObject())  extends DataQuery {
  type D =  Site
  def resource = manifest[ Site]
}

case class Site(
                 id: SiteId,
                 created: DateTime,
                 modified: DateTime,
                 name:Option[String] = None,
                 description:Option[String] = None,

                 /**
                  * Owner user ID. if None - the configuration is root template and contains set of available default configs.
                  */
                 userId: Option[UserId],

                 /**
                  * users, who can manage the site
                  */
                 siteUsers: List[SiteUser] = List.empty,

                 uri: String = "",
                 activeSiteConfig:String = "",
                 siteConfigs:Map[String, SiteConfig] = Map.empty,
                 usageLimits:Option[SiteUsageLimits] = None,
                 isActive:Option[Boolean] = Some(true)
                 ) extends DataRecord

case class SiteUsageLimits (
                            messagesPerMonth:Option[Int],
                            messagesPerDay:Option[Int],
                            messagesPerHour:Option[Int],
                            messagesTotal:Option[Int])


case class SiteConfig(

                       name:String = "",
                       validationClass:List[ValidationClass] = List.empty,

                       storeContent:Option[Boolean] = Some(true),
                       storeContentHash:Option[Boolean] = Some(true),
                       //push the message into PostValidation queue
                       postValidationSuccess:Option[Boolean] = Some(true),
                       postValidationFailure:Option[Boolean] = Some(true)
                       )

case object   SiteConfig {
 def apply(name:String):SiteConfig = new SiteConfig(name,validationClass=List.empty,  storeContent=Some(true), storeContentHash=Some(true) )
}
case class SiteUser(user:UserId, role:SecurityRoleEnum.Value = SecurityRoleEnum.SiteAdmin, expires:Option[DateTime]=None, enabled:Boolean)

/**
 * base validation class
 */
trait ValidationClass {
  /**
   * unique class instance id
   * @return
   */
  def vcid:UUID

  def enabled:Boolean
  def name:String
  def description:Option[String]
  def blockListId:Option[ValidationListId]
  def allowListId:Option[ValidationListId]
}

/**
 * html formating and xss validation
 * @param enabled
 * @param blockXss
 * @param allowListId - allow all tags if empty, or allow messages with listed tags only
 * @param blockListId - allow all tags if empty, or block messages with listed tags only
 */
case class HtmlValidationClass(
                               vcid:UUID = UUID.randomUUID(),
                               enabled:Boolean,
                               name:String,
                               description:Option[String] = None,
                               blockInvalidHTML:Boolean = true,
                               //allowPlainText:Option[Boolean] = true,
                               //allowPlainTextblockHTML4:Boolean = true,
                               //blockXHTML5:Boolean = true,
                               blockXss:Boolean = true,
                               allowListId:Option[ValidationHtmlTagListId] =  None,
                               blockListId:Option[ValidationHtmlTagListId] = None ) extends ValidationClass

/**
 * email validation class
 * @param enabled
 */
case class EmailValidationClass(vcid:UUID = UUID.randomUUID(),
                                enabled:Boolean,
                                name:String,
                                description:Option[String] = None,
                                //allowPublicEmailDomains:Boolean = false,
                                //allowPrivateEmailDomains:Boolean = true,
                                allowListId:Option[ValidationEmailListId] = None,
                                blockListId:Option[ValidationEmailListId] = None,
                                blockAllEmails:Option[Boolean]=None ) extends ValidationClass


/**
 * text validation class
 * @param enabled
 * @param allowListId - list of allowed IPs
 * @param blockListId - block messages form the IPs
 */
case class IpValidationClass(
                             vcid:UUID = UUID.randomUUID(),
                             enabled:Boolean,
                             name:String,
                             description:Option[String] = None,
                             allowListId:Option[ValidationIpListId] = None,
                             blockListId:Option[ValidationIpListId] = None
                            ) extends ValidationClass

/**
 * text validation class
 * @param enabled
 * @param allowListId - list of allowed text fragments - good for paranoidal validations
 * @param blockListId - block messages with listed text fragments
 */
case class TextValidationClass(
                               vcid:UUID = UUID.randomUUID(),
                               enabled:Boolean,
                               name:String,
                               description:Option[String] = None,
                               allowListId:Option[ValidationTextListId] = None,
                               blockListId:Option[ValidationTextListId] = None
                              ) extends ValidationClass

case class BandwidthValidationClass(
                                vcid:UUID = UUID.randomUUID(),
                                enabled:Boolean,
                                name:String,
                                description:Option[String] = None,
                                period:Int = 5, //sec,
                                countSuccess:Boolean = true,
                                countFailure:Boolean = true,
                                userLevel:Boolean = false,
                                blockThreshold:Int = 100, //messages within period
                                sendTo: Option[String] = None,
                                subject: Option[String] = None,
                                body: Option[String] = None
                                //allowListId:Option[ValidationTextListId] = None,
                                //blockListId:Option[ValidationTextListId] = None
                                ) extends ValidationClass
{
  //not applicable
  def blockListId:Option[ValidationListId] = None
  def allowListId:Option[ValidationListId] = None
}





