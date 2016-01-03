package com.assabetsecurity.core.db

import com.mongodb.casbah.Imports.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import org.slf4s.Logging
import java.util.UUID

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 6/22/13
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */
trait DataQuery {
  type D <: DataRecord

  def query:DBObject
  def resource: Manifest[D]

  def collectionName = resource.erasure.getSimpleName

}

case class JsonQuery[A<:DataRecord:Manifest](query:DBObject) extends DataQuery with Logging{
  type D = A
  def resource = manifest[A]

}

case object JsonQuery {
  def apply[A<:DataRecord:Manifest](json:Option[String]) ={
   val q = json.map(v=>{
      val r = com.mongodb.util.JSON.parse(v).asInstanceOf[com.mongodb.DBObject]
      if (r.get("id.value")!=null) {
        r.put("_id", UUID.fromString(r.get("id.value").toString))
        r.removeField("id.value")
      }
      r
    }).getOrElse(MongoDBObject())
    new JsonQuery(q)
  }
}