package com.assabetsecurity.core.db

import data.UserId
import org.joda.time.DateTime
import com.novus.salat._
import com.mongodb.casbah.Imports._
import java.util.UUID
import com.mongodb.MongoClient
import scalaz.{Success, Failure}
import scalaz.Validation
import com.mongodb.casbah.commons.conversions.scala.{RegisterConversionHelpers, RegisterJodaTimeConversionHelpers}
import org.slf4s.Logging
import scala.Exception
import com.assabetsecurity.core.data.ValidationList

/**
 * User: alyas
 * Date: 6/21/13
 * Time: 11:30 PM
 */

/**
 * repository base. name defines db name on a server
 */
trait Repository {
  def name:String

  /**
   * saves record to db
   * @param data - record to save
   * @param sp - principal
   * @param version - data version or branch to save
   * @tparam D - only objects derived from DataRecord can be used for DB operations.
   * @return same record or validation error in scalaz.Validation
   */
  def save[D <: DataRecord : Manifest](data: D, sp: SecurityPrincipal, version:Option[CollectionVersion]=None):Validation[DBSaveFailed, D]

  /**
   * removes data from repository
   * @param id object ID
   * @param sp principal
   * @tparam A restrict identifier to DataRecord-related instance
   * @return
   */
  def remove[A <: DataRecord : Manifest](id:Identifier[_]{type D<:A},sp: SecurityPrincipal):Validation[DBOperationFailure, Identifier[_]{type D<:A}]

  /**
   *
   * @param id record id to return, ID should be linked to specific DataRecord type
   * @param sp principal
   * @param version data version or branch to use
   * @tparam A is used to keep data definition and usage integrity
   * @return none or object derived from DataRecord and specified by type of Identifier
   */
  def get[A <: DataRecord : Manifest](id:Identifier[_]{type D<:A},sp: SecurityPrincipal, version:Option[CollectionVersion]=None):Option[A]

  /**
   * simplified version of find
   * @param query strongly-typed query object, defines returned data type. JsonQuery[A<:DataRecord] can be used to handle generic data access operations
   * @param sp  principal
   * @tparam A DataRecord type defined by query object
   * @return list of objects with passed paging params. in the case PagingParameters.empty
   */
  def find[A <: DataRecord : Manifest](query:DataQuery{type D<:A}, sp: SecurityPrincipal):(List[A], BasePagingParameters) = {
    find[A](query, PagingParameters.empty, sp, None)
  }

  /**
   *
   * @param query strongly-typed query object, defines returned data type. JsonQuery[A<:DataRecord] can be used to handle generic data access operations
   * @param paging paging parameters
   * @param sp principal
   * @param version branch or data version
   * @tparam A data type defined by query value
   * @return list of objects with passed paging params
   */
  def find[A <: DataRecord : Manifest](
                                        query:DataQuery{type D<:A},
                                        paging:BasePagingParameters,
                                        sp: SecurityPrincipal,
                                        version:Option[CollectionVersion]):  (List[A], BasePagingParameters)
  }

/**
 * real-time entity to track session/user authentication
 * can be extended to hold authorization properties
 */
trait SecurityPrincipal {
  def userId:Option[UserId]
  def expires:DateTime
}

/**
 * anonymous user activity
 * @param userId
 * @param expires
 */
case class UnauthenticatedSecurityPrincipal(userId:Option[UserId] = None, expires:DateTime=new DateTime()) extends  SecurityPrincipal

/**
 * public user - used mostly to access to public part of product.API or site
 * @param userId
 * @param expires
 */
case class PublicSecurityPrincipal(userId:Option[UserId] = None, expires:DateTime=new DateTime().plusHours(24)) extends  SecurityPrincipal

/**
 * verified and authenticated user with user-level access rights
 * @param userId
 * @param expires
 */
case class AuthenticatedSecurityPrincipal(userId:Option[UserId], expires:DateTime = (new DateTime).plusHours(2))  extends  SecurityPrincipal

/**
 * special principal to use for batch and internal data access
 * @param userId
 * @param expires
 */
case class DBOSecurityPrincipal(
                                 userId:Option[UserId] = Some(data.UserId(UUID.fromString("00000000-0000-0000-0000-000000000000"))),
                                 expires:DateTime = (new DateTime).plusHours(2)) extends  SecurityPrincipal


/**
 * DB access errors base
 */
trait DBOperationFailure{
  def e:String
}

/**
 * filed to save data record
 * @param d
 * @param e
 */
case class DBSaveFailed(d:DataRecord, e:String) extends DBOperationFailure

/**
 * remove data operation failed
 */
case class DBRemoveFailed(d:Identifier[_], e:String) extends DBOperationFailure

/**
 * PF/FK check failure
 */
case class DBRemoveRecordInUseFailed(d:Identifier[_], e:String) extends DBOperationFailure

/**
 * Single record get failure
 * @param d
 * @param e
 */
case class DBGetFailed(d:Identifier[_], e:String) extends DBOperationFailure

/**
 * find failure
 * @param d
 * @param e
 */
case class DBFindFailed(d:DataQuery, e:String) extends DBOperationFailure

/**
 * set of repositories
 */
object DB {

  private val secRepo= new MongoRepository("security")
  private val etcRepo = new MongoRepository("etc")
  private val statRepo = new MongoRepository("stat")

  private val dataRepo = new MongoRepository("data")
  private val unitTestRepo = new MongoRepository("unitTest")
  private val logRepo = new MongoRepository("log")

  def log = logRepo
  def unitTest = unitTestRepo
  def security = secRepo
  def etc = etcRepo
  def data = dataRepo
  def stat = statRepo
}