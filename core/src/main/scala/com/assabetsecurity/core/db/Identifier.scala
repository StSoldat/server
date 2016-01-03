package com.assabetsecurity.core.db

import java.util.UUID
import org.joda.time.LocalDateTime
import org.slf4s.Logging
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports.DBObject
import scalaz.{Success, Validation}

/**
 * User: alyas
 * Date: 1/8/13
 * Time: 5:13 PM
 */


//can be used for embedded objects to unify messages
case class EmbeddedId(value:String)

trait Identifier[A] {
  type D <: DataRecord
  //type I <: Identifier

  def value: A
  def collectionName:String = resource.erasure.getSimpleName
  def resource: Manifest[_]

  def beforeRemove():Validation[DBOperationFailure, Identifier[_]] = {
    Success(this)
  }
}

trait UUIDIdentifier extends Identifier[UUID]{
  def value: UUID
}

trait BasePagingParameters {
  def sortBy:Map[String, Byte]
  def skip:Option[Int]
  def limit:Option[Int]

  //output params

  /**
   * size of returned dataset
   * @return
   */
  def size:Option[Int]

  /**
   * total number of records in dataset
   * @return
   */
  def total:Option[Int]
}


case class PagingParameters(sortBy:Map[String, Byte] = Map.empty, skip:Option[Int]=None, limit:Option[Int]=None,
                            size:Option[Int]=None, total:Option[Int]=None
                             ) extends BasePagingParameters

object  PagingParameters {
  def empty = new PagingParameters(sortBy = Map.empty, skip = None, limit = None, size=None, total=None)
}
/*
case object IdentifierRestSerializer extends CustomSerializer[Identifier](format => (
    {
      case _: Identifier => throw new Exception("not supported")
    },
    {
      case id: Identifier => {
        implicit val formats = Serialization.formats(NoTypeHints) //++ JodaTimeSerializers.all
        JObject(List(
          JField("uri", JString("/api/" + id.entityUri + "/" + id.value)),
          JField("value", Extraction.decompose(id.value.toString))
        ))
      }
    }
  )
)

case object UUIDSerializer extends CustomSerializer[UUID](format => (
  {
    case JString(v)  => {
      UUID.fromString(v)
    }
  },
  {
    case id: UUID => {
      implicit val formats = Serialization.formats(NoTypeHints) //++ JodaTimeSerializers.all
        JString(id.toString)
    }
  }
))
case object BooleanSerializer extends CustomSerializer[Boolean](format => (
  {
    case JString(v)  => {
      v.toBoolean
    }
  },
  {
    case v: Boolean => {
      implicit val formats = Serialization.formats(NoTypeHints)
      JString(v.toString)
    }
  }
))*/
