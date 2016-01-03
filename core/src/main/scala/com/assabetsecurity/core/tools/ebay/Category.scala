package com.assabetsecurity.core.tools.ebay

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonIgnoreProperties, JsonProperty}
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper

/**
 * Created by alyas on 12/10/15.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
case class Category(
                     @JsonProperty("ID")
                     id:Int,
                     @JsonProperty("Name")
                     name:String,
                     @JsonProperty("Children")
                     children:List[Category] = List.empty
                    ) {

  @JsonIgnore
  def allChildren:List[Category] = {
    val res = if(children!=null) {
      children.map(c=>{
        c.allChildren
      }).flatten
    }
    else
      List.empty

    this :: res
  }
}

case class Categories(
                       //@JsonProperty("Category")
                       @JacksonXmlElementWrapper(localName = "Category", useWrapping=false)
                       @JsonProperty("Category")
                       categories:List[Category]
)