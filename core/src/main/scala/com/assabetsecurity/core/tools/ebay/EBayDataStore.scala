package com.assabetsecurity.core.tools.ebay

import java.io.File

import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnoreProperties}
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.joda.time.DateTime
import org.slf4s.Logging

/**
 * Created by alyas on 12/10/15.
 */

object EBayDataStore extends Logging {
  val mapper = {
    val m = new XmlMapper()
    m.registerModule(DefaultScalaModule)
    m.registerModule(new JodaModule());
    m
  }

  def loadCategories:Categories = {
    mapper.readValue(new File("./data/ebay_categories_full.xml"), classOf[Categories])
  }
  def fromXmlString(s:String):FindResponse = {
    //log.debug("")
    mapper
      .readValue[FindResponse](
        s.replaceAllLiterally("&", "&amp;"),
        classOf[FindResponse])

    //json.findItemsAdvancedResponse.searchResult.item
    //List.empty
  }
  @JsonIgnoreProperties(ignoreUnknown = true)
  case class FindResponseRoot(findItemsAdvancedResponse:FindResponse)


  case class FindResponse(ack:String
                          , version:String
                          , timestamp:DateTime
    @JacksonXmlElementWrapper(localName = "searchResult")
    @JsonProperty("item")
                          , searchResult:List[Item],
                          paginationOutput:Map[String, Any],
                          itemSearchURL:String
                           )

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class SearchResult(
                           //@JsonProperty("@count")
                           //count:Int,
                           item:List[Item])

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class Item(itemId:String,
                  title:String,
                  subtitle:String,
                  productId:String,
                  globalId:String,
                  country:String,
                  primaryCategory:CategoryEmbedded,
                  sellingStatus:SellingStatus//Map[String, Any]
                   )

  case class CategoryEmbedded(categoryId:String, categoryName:String)

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class SellingStatus(

                            currentPrice:Map[String, String],
                            convertedCurrentPrice:Map[String, String],
                            sellingState:String, timeLeft:String)



}
