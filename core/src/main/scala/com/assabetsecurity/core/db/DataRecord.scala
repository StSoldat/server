package com.assabetsecurity.core.db

import java.util.UUID
import org.slf4s.Logging
import com.novus.salat._
import com.novus.salat.StringTypeHintStrategy
import com.assabetsecurity.core.data.SiteId
import scalaz.{Success, Validation}

/**
  * User: alyas
 * Date: 4/6/13
 * Time: 8:53 PM
 */

trait DataRecord extends Logging {
  def id: Identifier[_]
  import com.novus.salat._
  implicit val ctx: Context = new Context {
    val name = "DataRecord"
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always, typeHint = TypeHint)
  }

  def asDBObject[ R<:DataRecord:Manifest ](data:R) = {
    grater.asDBObject(data)
  }

  def collectionVersion:Option[CollectionVersion] = None


}

trait CollectionVersion {
  def collectionName(originalName:String):String
}
case class NamedCollectionVersion(name:String) extends CollectionVersion {
  def collectionName(originalName:String) ={
    originalName+"_"+name.toString
  }
}

case class SiteCollectionVersion(id:SiteId) extends CollectionVersion {
  def collectionName(originalName:String) ={
    originalName+"_"+id.value.toString.replaceAllLiterally("-", "")
  }
}
/*

case class InvalidId(value:UUID)  extends Identifier {
  type D = InvalidDataRecord
  //def collectionName = "invalidDataRecord"
}
case class InvalidDataRecord (id:InvalidId, error:String,  data:Map[_, _]) extends DataRecord*/
