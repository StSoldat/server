/*
package com.assabetsecurity.core.db

import org.slf4s.Logging
import collection.mutable
import data.{AccountId, Account, UserId, User}
import scalaz.{Validation, Success, Failure}

import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import ext.JodaTimeSerializers
import net.liftweb.json.Xml.{toJson, toXml}
import java.util.UUID


/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 4/6/13
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */

object  XmlDb extends Logging {
  def dbFileName = "./db.xml"
/*  implicit val formats  = Serialization.formats(FullTypeHints(
    List(classOf[UserId], classOf[User], classOf[Account] )
  )) ++ JodaTimeSerializers.all*/
  implicit val formats = Serialization.formats(NoTypeHints) ++ JodaTimeSerializers.all +  UUIDSerializer + BooleanSerializer
  /**
   * load database from predefined location into memory
   */
  def load():Unit = {
    load(dbFileName)
  }

  def load(dbFileName:String):Unit = {
    val xml = scala.xml.XML.load(scala.xml.Source.fromFile(dbFileName))

    users.clear()
    // load users collection
    (xml \\ "users" \ "user").foreach(n=>{
      //log.info(">>"+compact(JsonAST.render(Xml.toJson(n) \ "user" )))
      val user = (Xml.toJson(n) \ "user").extractOpt[User]

      log.info("loaded user: "+user)

      //insert users to memory structure
      user.foreach(u=>{
        users(u.id) = u
      })
    })
    log.info(" Users loaded: "+users.size)
    //load accounts collection
  }


  /**
   * save to default file
   */
  def save():Unit = {
    save(dbFileName)
  }
  /**
   * saves database to file
   */
  def save(dbFileName:String):Unit = {
    log.info("Save XML database to "+dbFileName)
    val xml = <data>
      <users>

        {
          users.map(u=>{
            val user = u._2
            <user>
              {
                Xml.toXml(Extraction.decompose(user))
              }
            </user>
          })
        }
      </users>
      <accounts>
      </accounts>
      <version>0.1</version>
    </data>

    scala.xml.XML.save(dbFileName, xml)
  }

  protected val users:mutable.HashMap[UserId, User]  = mutable.HashMap.empty


  protected val accounts:mutable.HashMap[AccountId, Account] = mutable.HashMap.empty

  def addUser[U <:User](u:U):Validation[DataResourceExistsFailure[U], U] = {
    log.info("Add user with id:"+u.id)
    if(users.get(u.id).isEmpty) {
      users(u.id) = u
      log.info("  User Added")
      Success(u)
    } else {
      Failure(DataResourceExistsFailure(u))
    }
  }

  def saveUser(u:User) = {

  }

  def get[A <: Identifier](id:A):Option[DataResource] =  {
    id match {
      case id:UserId => users.get(id)
      case id:AccountId => accounts.get(id)
      case _=>  None
    }
  }
}

*/
