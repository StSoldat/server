package com.assabetsecurity.core.db

import com.mongodb.MongoClient
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.{MongoCollection, MongoDB}
import com.mongodb.casbah.commons.conversions.scala.{RegisterJodaTimeConversionHelpers, RegisterConversionHelpers}
import com.novus.salat._
import org.joda.time.DateTime
import org.slf4s.Logging

import scalaz.{Success, Failure, Validation}

/**
 * Created by alyas on 2/2/15.
 */
/**
 * base class to support mongo DB
 * @param name
 */
class MongoRepository(val name:String) extends Repository with Logging {

  /**
   * TODO: improve to use config/bootstrap for db name and credentials
   */
  val client = {
    log.debug(">>>>>>>>>>>> load client settings")
    new MongoClient( "localhost" , 27020 );
  }

  val db = new MongoDB(client.getDB(name)) {
    def getCollection(name:String, version:Option[CollectionVersion]) ={
      if(version.isEmpty)
        super.getCollection(name)
      else
        super.getCollection(version.get.collectionName(name))
    }
  }

  //register helpers to support specific data types like Identifier or joda DateTime
  RegisterConversionHelpers()
  RegisterJodaTimeConversionHelpers()

  implicit val ctx: Context = new Context {
    val name = "DB"
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always, typeHint = TypeHint)
  }

  /**
   * saves record to db
   * @param data - record to save
   * @param sp - principal
   * @param version - data version or branch to save
   * @tparam D - only objects derived from DataRecord can be used for DB operations.
   * @return same record or validation error in scalaz.Validation
   */
  def save[D <: DataRecord : Manifest](data: D, sp: SecurityPrincipal, version:Option[CollectionVersion]=None):Validation[DBSaveFailed, D] = {
    val record = grater[D].asDBObject(data)
    record.remove("id")
    record.put("_id", data.id.value)

    record.getAs[DateTime]("modified").foreach(v=>{
      record.remove("modified")
      record.put("modified", DateTime.now)
    })
    val result = db.getCollection(data.id.collectionName, version).save(record)

    if (result.getError != null && !result.getError.isEmpty) {
      Failure(DBSaveFailed(data, result.getError))
    } else {
      Success(data)
    }
  }

  /**
   * removes data from repository
   * @param id object ID
   * @param sp principal
   * @tparam A restrict identifier to DataRecord-related instance
   * @return
   */
  def remove[A <: DataRecord : Manifest](id:Identifier[_]{type D<:A},sp: SecurityPrincipal):Validation[DBOperationFailure, Identifier[_]{type D<:A}] = {

    val res:Validation[DBOperationFailure, Identifier[_]] = id.beforeRemove()
    val res2 = res match {
      case Success(x) => {
        val collection = new MongoCollection(db.getCollection(id.collectionName))

        val result = collection.remove(MongoDBObject("_id"->id.value))
        if (result.getError != null && !result.getError.isEmpty) {
          Failure(DBRemoveFailed(id, result.getError))
        } else {
          Success(id)
        }
      }
      case Failure(x) => Failure(x)
    }
    res2
  }

  /**
   * MongoDb specific implementation
   * @param id record id to return, ID should be linked to specific DataRecord type
   * @param sp principal
   * @param version data version or branch to use
   * @tparam A is used to keep data definition and usage integrity
   * @return none or object derived from DataRecord and specified by type of Identifier
   */
  def get[A <: DataRecord : Manifest](id:Identifier[_]{type D<:A},sp: SecurityPrincipal, version:Option[CollectionVersion]=None):Option[A] = {
    val collection = new MongoCollection(db.getCollection(id.collectionName, version))

    collection.findOneByID(id.value.asInstanceOf[AnyRef]).map { dbObj =>
      try{
        dbObj.put("id", MongoDBObject("value" -> id.value))
        grater[A].asObject(dbObj)
      } catch {
        case e => {
          log.error("Error when converting dbObj: " + dbObj, e)
          throw e
        }
      }

    }
  }

  /**
   * mongo implementation for find functionality
   * @param query strongly-typed query object, defines returned data type. JsonQuery[A<:DataRecord] can be used to handle generic data access operations
   * @param paging paging parameters
   * @param sp principal
   * @param version branch or data version
   * @tparam A data type defined by query value
   * @return list of objects with passed paging params
   */
  def find[A <: DataRecord : Manifest](query:DataQuery{type D<:A}, paging:BasePagingParameters, sp: SecurityPrincipal, version:Option[CollectionVersion]):
  (List[A], BasePagingParameters) = {
    val collection = new MongoCollection(db.getCollection(query.collectionName, version))
    log.trace("find collection [" + collection.name  +"] with query ::: " + query.query.toString +" in db:"+db.name+" limit: "+paging.limit )
    
    try{
      val sortObject = MongoDBObject.newBuilder
      paging.sortBy.foreach(sortObject += _)

      val dbres = collection.find(query.query).skip(paging.skip.getOrElse(0)).limit(paging.limit.getOrElse(0)).sort(sortObject.result())

      val retPaging = PagingParameters(sortBy = paging.sortBy, skip = paging.skip, limit = paging.limit, size = Some(dbres.size), total=Some(dbres.count))

      val res = dbres.map(dbObj=>{
        dbObj.put("id", MongoDBObject("value" -> dbObj.get("_id")))

        //do not pass back 'grater' issues. log as error and return none
        //the issues should be detected by unit tests
        val d = try{
          Some(grater[A].asObject(dbObj))
        } catch {
          case e:Exception =>
            log.error("Error in grater: " + e.getMessage)
            None
          case _=> None
        }
        d
      }).filter(_.isDefined).map(_.get).toList //flatmap for the values.


      (res,  retPaging)
    } catch {
      case e:Throwable => {
        //log and pass exception to upper level
        log.error("Error in find: " + e)
        throw e
      }
    }
  }
}