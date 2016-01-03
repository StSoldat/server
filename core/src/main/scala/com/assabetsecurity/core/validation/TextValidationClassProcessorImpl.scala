package com.assabetsecurity.core.validation

import com.assabetsecurity.core.data.{SiteConfig, TextValidationClass}
import org.apache.commons.lang3.StringUtils

/**
  * User: alyas
 * Date: 10/20/13
 * Time: 10:15 AM
 */

object TextValidationClassProcessorImpl extends ValidationClassProcessor{
  def validate(m:SiteMessageContent, data:MessageValidationActorData, siteConfig:SiteConfig):List[ValidationResult] = {
    log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")

    log.debug("Validate text for config: "+siteConfig )

    //val separators:Array[Char] = m.message.filter(p => !p.isLetterOrDigit).toCharArray

    //fix single word message processing

/*    val messageWords = {
      val v  = StringUtils.split(m.message, separators.mkString, 0)
      if (v.isEmpty) List(m.message) else v.toList
    }*/

    val lcMessage = m.message.toLowerCase

    val res:List[ValidationResult] = (for {
      vClass <- getValidationClass[TextValidationClass](siteConfig).toList
      id <- vClass.blockListId.toList
      validationData <- data.getTextValidationData(id).toList
    } yield {
      log.debug(">>>>>>>>>>>>>> Validate text for config: "+vClass)

      val allowEntries = (for {
        allowId <- vClass.allowListId
        allowValidationData <- data.getTextValidationData(allowId)
      } yield {
        allowValidationData.entryList
      }).getOrElse(List.empty)

      val entries = validationData.entryList
        .filter(e=>lcMessage.contains(e.toLowerCase))
        .filterNot(e=>allowEntries.contains(e))

      TextValidationResultFailure(
        listName = Some(validationData.list.name),
        listId = Some(validationData.list.id),
        vcid= Some(vClass.vcid.toString),
        entry = entries
      )
    }).filter( v=> !( v.entry.isEmpty) )
    //.orElse(TextValidationResultSuccess() :: Nil)
    if(res.isEmpty)
      TextValidationResultSuccess() :: Nil
    else
      res
  }
}