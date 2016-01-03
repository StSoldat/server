package com.assabetsecurity.core.data

import java.util.UUID
import com.assabetsecurity.core.db.{DataRecord, DataQuery, Identifier}
import com.mongodb.casbah.Imports._


import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.{DateTimeZone, DateTime}
import com.assabetsecurity.core.db.data.UserId
import com.assabetsecurity.core.validation.{ValidationResult, SiteMessageContent}

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 10/27/13
 * Time: 10:58 PM
 * To change this template use File | Settings | File Templates.
 */

case class SiteMessageStatisticId(value:Long)  extends Identifier[Long] {
  type D = SiteMessageStatistic
  def resource = manifest[D]
}


case class SiteMessageStatisticQuery(query:DBObject = MongoDBObject())  extends DataQuery {
  type D = SiteMessageStatistic
  def resource = manifest[SiteMessageStatistic]
}

case class SiteMessageStatistic (
                         id: SiteMessageStatisticId,
                         modified:DateTime = new DateTime,
                         interval: String,
                         intervalStartDate: DateTime,

                         siteId:SiteId,
                         year:Option[Int] = None,
                         month:Option[Int]= None,
                         day:Option[Int]= None,
                         hour:Option[Int]= None,

                         results:Map[String, StatDetails] = Map.empty
                        ) extends DataRecord


case class StatDetails( result:String,
                        //vcid:Option[String] = None,
                        vcType:String,
                        count:Int,
                        size:Int,
                        processingTimeMax:Int,
                        processingTimeTotal:Int)
case object SiteMessageStatistic {
  def startDate(interval: String, date: DateTime): DateTime = {
    val y = date.getYear
    val m = date.getMonthOfYear
    val d = date.getDayOfMonth
    val h = date.hourOfDay.get

    interval match {
      case "hour" =>{
        new DateTime(y, m, d, h, 0, DateTimeZone.UTC)
      }
      case "day" =>{
        new DateTime(y, m, d, 0, 0, DateTimeZone.UTC)
      }
      case "month" =>{
        new DateTime(y, m, 1, 0, 0, DateTimeZone.UTC)
      }
    }

  }


  def getHash(interval:String, d:DateTime):Long = {
    val h = (d.year.get()*366 + d.dayOfYear().get())*24 + d.hourOfDay().get()
    val r:Long = interval match {
      case "hour" =>{
         h
      }
      case "day" =>{
        2* h
      }
      case "month" =>{
        4 * h
      }
    }
    r
  }
}