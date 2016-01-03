package com.assabetsecurity.core.tools.ebay

import akka.actor.{Actor, ActorLogging}
import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
import com.fasterxml.jackson.databind.{AnnotationIntrospector, ObjectMapper}
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.{JacksonXmlText, JacksonXmlRootElement, JacksonXmlProperty, JacksonXmlElementWrapper}
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.joda.time.DateTime
import org.slf4s.Logging

/**
 * Created by alyas on 11/29/15.
 */
class EBayLoader extends Actor with ActorLogging {

  val serverUri  = "http://svcs.ebay.com/services/search/FindingService/v1"
  val operation = "?OPERATION-NAME=findItemsAdvanced&SERVICE-VERSION=1.0.0&RESPONSE-DATA-FORMAT=JSON"
  val appId = "SECURITY-APPNAME=canmoder-e262-4385-a984-fa75503576bb"
  var categoryId = 31388
  def category="categoryId="+categoryId
  val paging = "paginationInput.entriesPerPage=100"

  def pageNumber = "paginationInput.pageNumber=1"

  def fullUri = {
    serverUri + operation +"&"+appId+"&"+category+paging
  }

  override def receive: Receive = {
    case m:Start =>{
      log.debug("Start...")
      sender ! StartSuccess()
    }
    case m:Any => {
      unhandled(m)
    }
  }

  def load {

  }
}
case class Start()
case class StartSuccess()
