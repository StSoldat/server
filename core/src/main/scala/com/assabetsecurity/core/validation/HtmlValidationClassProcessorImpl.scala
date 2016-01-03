package com.assabetsecurity.core.validation

import com.assabetsecurity.core.data._
import org.apache.commons.lang3.StringUtils
import xml.{Text, Node, XML}
import collection.mutable.ListBuffer
import java.util.UUID
import com.assabetsecurity.core.validation.HtmlValidationResultFailure
import scala.Some
import com.assabetsecurity.core.validation.HtmlValidationResultFailureNoEntry
import com.assabetsecurity.core.validation.SiteMessageContent
import com.assabetsecurity.core.validation.HtmlValidationResultSuccess
import com.assabetsecurity.core.data.HtmlValidationClass

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 10/20/13
 * Time: 10:15 AM
 * To change this template use File | Settings | File Templates.
 */

object HtmlValidationClassProcessorImpl extends ValidationClassProcessor{
  def validate(m:SiteMessageContent, data:MessageValidationActorData, siteConfig:SiteConfig):List[ValidationResult] = {
    //log.debug("--------- Validate html for site: "+data.site + "   config: "+ data.siteConfig)
    val res:List[ValidationResult] = (for {
      //c <- data.siteConfig.toList
      vClass:HtmlValidationClass <- getValidationClass[HtmlValidationClass](siteConfig).toList
    } yield {
      //log.debug("class "+vClass)

      //log.debug("data "+data)
      val r:List[ValidationResult] = (
        try {
          val dataXml = try {
            XML.loadString(m.message)
          } catch {
            case _=> try {
              log.debug("try to parse with extra wrapper 'root'")
              //XML.loadString("<root>"+Text(m.message)+"</root>")
              XML.loadString("<root>"+m.message+"</root>")
            } catch {
              case e => throw e
            }
          }
          log.debug("parsed HTML root:" + dataXml.label)

          val validationResult:List[ValidationResult] = (for {
            lid <- vClass.blockListId.toList
            validationData <- data.getHtmlValidationData(lid).toList
          } yield {
            log.debug("tags:"+validationData.entryList)
            val flatNodeList:ListBuffer[Node] = new ListBuffer()
            allChildren(dataXml.child, flatNodeList)

            val messageTags =  (List(dataXml.label)  ++ flatNodeList.map(_.label)).map(_.toLowerCase)

            val allowEntries = (for {
              allowId <- vClass.allowListId
              allowValidationData <- data.getHtmlValidationData(allowId)
            } yield {
              allowValidationData.entryList
            }).getOrElse(List.empty)

            val tags = validationData.entryList filterNot (allowEntries contains)

            val foundTags = messageTags.filter(t=>tags.find(et=>et.toLowerCase==t).isDefined)
            if (foundTags.isEmpty)
              HtmlValidationResultSuccess()
            else
              HtmlValidationResultFailure(
                listName = Some(validationData.list.name),
                listId = Some(validationData.list.id),
                vcid = Some(vClass.vcid.toString),
                entry = foundTags
              )
          }).toList
          validationResult
      } catch {
        case _ =>
          if (vClass.blockInvalidHTML) {
            HtmlValidationResultFailureNoEntry(resultMessage=Some("invalid_html")) :: Nil
          } else {
            HtmlValidationResultSuccess() :: Nil
          }
      })
      r
    }).flatten.toList

    if (res.isEmpty)
      HtmlValidationResultSuccess() :: Nil
    else
      res
  }

  /**
   * returns flat list of child nodes
   * @param root
   */
  def allChildren(root:Seq[Node], buffer:ListBuffer[Node]):Unit =  {
    root.foreach(node=>{
      buffer.append( node )
      allChildren( node.child, buffer )
    })
  }
}