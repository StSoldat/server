package com.assabetsecurity.core.data

import com.assabetsecurity.core.db._
import java.util.UUID
import java.io.{InputStream, File, ByteArrayInputStream}
import collection.mutable.ListBuffer
import scalaz.{Success, Failure, Validation}
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.mongodb.casbah.Imports._
import org.joda.time.DateTime

import org.slf4s.Logging
import com.mongodb.gridfs.{GridFSDBFile, GridFS}
import com.mongodb.casbah.gridfs.Imports._

import com.mongodb.casbah.gridfs.JodaGridFSInputFile
import scalaz.Success
import com.assabetsecurity.core.db.DBSaveFailed
import com.assabetsecurity.core.db.DB

import scalaz.Failure
import java.util.regex.Pattern


/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 9/29/13
 * Time: 12:56 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * This class replaces GridFSDBFile to overwrite the handling of dates
 */


trait ValidationListId extends UUIDIdentifier with Logging   {
  def value:UUID
  type D <:DataRecord


  override def beforeRemove():Validation[DBOperationFailure, Identifier[_]] = {
    log.debug("beforeRemove for " + this.getClass.getCanonicalName)
    //super.beforeRemove()
    val foundUsages = (for {
      site <- DB.etc.find[Site](SiteQuery(), DBOSecurityPrincipal())._1
      config <- site.siteConfigs.values
      id  <- config.validationClass.map(c=>c.allowListId.toList++c.blockListId.toList).flatten if id.value==this.value
    } yield {
      id
    })
    if (foundUsages.length==0)
      Success(this)
    else
      Failure(DBRemoveRecordInUseFailed(this, "id used by " + foundUsages.length + " list(s)"))
  }
}
object ValidationList {
  RegisterJodaTimeConversionHelpers()
  val gridfs = JodaEnabledGridFS(DB.etc.db, "validationList")
}

trait ValidationList extends DataRecord {

  def id : ValidationListId

  def modified:DateTime

  def isDeleted:Option[Boolean]

  def updateEntryList(add: List[String], remove:List[String]) = {
    val toSave = (loadEntryList  filterNot (remove contains ) ) ++ add
    saveEntryList(toSave.distinct)
  }

  /**
   * replaces entire list with add values
   * @param add
   * @return
   */
  def replaceEntryList(add: List[String]) = {
    val toSave = add
    saveEntryList(toSave.distinct)
  }

  def findEntryList(find: List[String], limit:Option[Int]):List[String] = {
   val r:ListBuffer[String] = new ListBuffer[String]()
    ValidationList.gridfs.findOne(fileName).map(f=>{
      log.info("load from:"+fileName)
      val s = scala.io.Source.fromInputStream(f.inputStream)
      try {
        s.getLines().foreach(l=>{
          //r += l
          find.foreach(f=>{
            val pat = Pattern.compile(f)

            //log.debug(""+l+" :: "+f)
            if(pat.matcher(l).matches()
              //l.matches(f)
              && !r.contains(l)) {
              r.+=(l)
            }
            if (limit.isDefined && r.size>=limit.get) throw new Exception()
          })
        })
      } catch {
        case _=>
      }
    }).getOrElse({
      log.error(""+fileName)
      List.empty
    })

   r.toList
  }

  def foreachEntryList(fn: String => Unit) = {
    ValidationList.gridfs.findOne(fileName).map(f=>{
      scala.io.Source.fromInputStream(f.inputStream).getLines().foreach(l=>{
        fn(l)
      })
    })
  }

  def loadEntryList():List[String] = {
    ValidationList.gridfs.findOne(fileName).map(f=>{
      log.info("load from:"+fileName)
      scala.io.Source.fromInputStream(f.inputStream).getLines().toList
    }).getOrElse({
      log.error(""+fileName)
      List.empty
    })
  }

  def fileName = id.value.toString.replaceAllLiterally("-", "")+".txt"
  def saveEntryList(entry: List[String]):Validation[DBSaveFailed, UUIDIdentifier] =  {
    try {
      DB.etc.save(this, DBOSecurityPrincipal())
      val is = new ByteArrayInputStream(entry.mkString("\n").getBytes())

      ValidationList.gridfs.findOne(fileName).foreach(f=>{
        ValidationList.gridfs.remove(fileName)
      })

      val f:JodaGridFSInputFile = ValidationList.gridfs.createFile(is, fileName)
      f.save()
      log.info("save to: "+fileName)
      Success(id)
    } catch {
      case e =>  Failure(DBSaveFailed(this, e.getMessage))
    }
  }
}
