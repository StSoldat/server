package com.assabetsecurity.core.validation

import com.assabetsecurity.core.data.{SiteConfig, EmailValidationClass, HtmlValidationClass}

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 10/20/13
 * Time: 10:15 AM
 * To change this template use File | Settings | File Templates.
 */

object EmailValidationClassProcessorImpl extends ValidationClassProcessor{
  def validate(m:SiteMessageContent, data:MessageValidationActorData, siteConfig:SiteConfig):List[ValidationResult] = {
    //log.debug("--------- Validate email for config: "+m.config )
    //log.debug("--------- Validate email for site config: "+ siteConfig.name)
    //email extractor regexp
    val reg = """(?i)\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}\b""".r

    val res:List[ValidationResult] = (for {
      vClass:EmailValidationClass <- getValidationClass[EmailValidationClass](siteConfig)
    } yield {
      val messageEmails = reg.findAllIn( m.message).toList
      val r = vClass.blockListId.map(lid=>data.getEmailValidationData(lid)).flatten.map(validationData=>{
        //log.debug("class "+vClass)
        //log.debug("email "+emails )
        //log.debug("entryList size "+validationData.entryList.size)

        val allowEntries = (for {
          allowId <- vClass.allowListId
          allowValidationData <- data.getEmailValidationData(allowId)
        } yield {
          allowValidationData.entryList
        }).getOrElse(List.empty)

        val emailList = validationData.entryList filterNot  (allowEntries contains)



        //log.debug(">>>2 db >>> "+emailList)
        val emailEntries = messageEmails.filter( e=> {
          val lce = e.toLowerCase
          emailList.find(entry=>{
              ( (entry.startsWith("@") || entry.startsWith("*")) && lce.endsWith(entry.toLowerCase) ) ||
              ( !(entry.startsWith("@") || entry.startsWith("*")) && lce == entry )
          }).isDefined
        } )

        log.debug("found emails "+emailEntries)
        EmailValidationResultFailure(
          listName = Some(validationData.list.name),
          listId = Some(validationData.list.id),
          vcid= Some(vClass.vcid.toString),
          entry = emailEntries
        )
      })
      if (vClass.blockAllEmails ==Some(true)) {
        Some(EmailValidationResultFailure(
          listName = None,
          listId = None,
          vcid= None,
          entry = messageEmails
        ))
      } else {
        r.lastOption
      }

    }).flatten.filter( v=> !(v.entry.isEmpty) )

    if(res.isEmpty)
      EmailValidationResultSuccess():: Nil
    else
      res
  }
}