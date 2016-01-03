package com.assabetsecurity.core.validation

import com.assabetsecurity.core.data.ValidationListId
import org.joda.time.DateTime

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 9/30/13
 * Time: 11:44 PM
 * To change this template use File | Settings | File Templates.
 */

case class ValidationResults( id:String,
                              trackingId:Option[String] = None,
                              userId:Option[String] = None,
                              results:List[ValidationResult],
                              config:Option[String] = None,
                              created:Option[DateTime] = Some(DateTime.now)
                              )
case class SiteIsNotActive(id:String)

trait ValidationResult {
  //def level:ValidationLevelEnum.Value
  def result:String
  def vcType:String
  def vcid:Option[String]
  def listName:Option[String]
  def listId:Option[ValidationListId]
}
trait ValidationResultSuccess

trait ValidationResultFailure {
  def entry:List[String]
}

case class ConfigurationValidationResultFailure( vcType:String = "configuration",
                                                 vcid:Option[String]= None,
                                                 result:String="fail",
                                                 listName:Option[String] = None,
                                                 listId:Option[ValidationListId] = None
                                                 ) extends ValidationResult

case class IpValidationResultSuccess(vcType:String = "ip",
                                     vcid:Option[String]= None,
                                     result:String="success",
                                     listName:Option[String] = None,
                                     listId:Option[ValidationListId] = None) extends ValidationResult with ValidationResultSuccess

/*case class IpValidationResultNA(vcType:String = "ip",
                                vcid:Option[String]= None,
                                result:String="na",          //return success
                                listName:Option[String] = None,
                                listId:Option[ValidationListId] = None) extends ValidationResult*/

case class IpValidationResultFailure(vcType:String = "ip",
                                     vcid:Option[String]= None,
                                     result:String="fail",
                                     listName:Option[String] = None,
                                     listId:Option[ValidationListId] = None,
                                     entry:List[String]) extends ValidationResult with ValidationResultFailure


case class TextValidationResultSuccess(vcType:String = "text",
                                       vcid:Option[String] = None,
                                       result:String="success",listName:Option[String] = None,
                                       listId:Option[ValidationListId] = None) extends ValidationResult with ValidationResultSuccess

case class TextValidationResultFailure(vcType:String = "text",
                                       vcid:Option[String] = None,
                                       result:String="fail",
                                       listName:Option[String] = None,
                                       listId:Option[ValidationListId] = None,
                                       entry:List[String] = List.empty) extends ValidationResult with ValidationResultFailure


case class EmailValidationResultSuccess(vcType:String = "email",  vcid:Option[String] = None, result:String="success", listName:Option[String] = None,
                                        listId:Option[ValidationListId] = None) extends ValidationResult with ValidationResultSuccess

case class EmailValidationResultFailure(vcType:String = "email", vcid:Option[String] = None, result:String="fail", listName:Option[String] = None,
                                        listId:Option[ValidationListId] = None,
                                        entry:List[String] = List.empty) extends ValidationResult with ValidationResultFailure

case class HtmlValidationResultSuccess(vcType:String = "html", vcid:Option[String]  = None, result:String="success", listName:Option[String] = None,
                                       listId:Option[ValidationListId] = None) extends ValidationResult with ValidationResultSuccess

case class HtmlValidationResultFailure(vcType:String = "html", vcid:Option[String] = None, result:String="fail", listName:Option[String] = None,
                                       listId:Option[ValidationListId] = None,
                                       resultMessage:Option[String] = None,
                                       entry:List[String] = List.empty) extends ValidationResult with ValidationResultFailure

case class HtmlValidationResultFailureNoEntry(vcType:String = "html", vcid:Option[String] = None, result:String="fail", listName:Option[String] = None,
                                       listId:Option[ValidationListId] = None,
                                       resultMessage:Option[String] = None ) extends ValidationResult with ValidationResultFailure {
  def entry:List[String] = ???
}


case class BandwidthValidationResultFailure(vcType:String = "bandwidth", vcid:Option[String] = None,
                                      result:String="fail",
                                      listName:Option[String] = None,
                                      listId:Option[ValidationListId] = None,
                                      resultMessage:Option[String] = None,
                                      period: Option[Int] = None, //sec
                                      count: Option[Int] =  None ) extends ValidationResult with ValidationResultFailure {
  def entry:List[String] = ???
}


case class BandwidthValidationResultSuccess(
                                             vcType:String = "bandwidth",
                                             vcid:Option[String] = None,
                                             result:String="success",
                                             listName:Option[String] = None,
                                             listId:Option[ValidationListId] = None
                                             ) extends ValidationResult with ValidationResultSuccess {
}

case class CustomValidationResultFailure(
                                          vcType:String = "custom",
                                          vcid:Option[String] = None,
                                          validationName:String="custom",
                                          result:String="fail", listName:Option[String] = None,
                                          listId:Option[ValidationListId] = None) extends ValidationResult with ValidationResultFailure {
  def entry:List[String] = ???
}
