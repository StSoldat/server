package com.assabetsecurity.core.stat

import akka.actor.{Cancellable, ActorLogging, Actor}
import com.assabetsecurity.core.data._
import org.joda.time.{DateTimeZone, DateTime}
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import java.util.UUID

import com.assabetsecurity.core.data.SiteMessageStatisticId
import com.assabetsecurity.core.data.SiteMessage

import com.assabetsecurity.core.data.SiteMessageStatistic
import scala.Some
import com.assabetsecurity.core.db.{SiteCollectionVersion, DBOSecurityPrincipal, DB}
import scala.collection.mutable

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 10/27/13
 * Time: 12:28 AM
 *
 */


/**
 * the message dumps stat object to DB
 * @param stopScheduler stop all scheduled data save tasks and
 */
case class DumpStatToDb(stopScheduler:Boolean = false)

/**
 * init message for specific site
 * @param systemId
 */
case class SiteStatActorInit(systemId:String)

/**
 * message to return  statistic for specific site
 * @param from
 * @param to
 * @param interval
 */
case class RequestSiteMessageStatisticData(from:DateTime, to:DateTime, interval:String)

/**
 * statistic data
 * @param from
 * @param to
 * @param interval interval type "hour" :: "day" :: "month" :: Nil
 * @param statistic statistic results and counters
 */
case class SiteMessageStatisticData(from:DateTime, to:DateTime, interval:String, statistic:List[SiteMessageStatistic])

/**
 * site messaages processing actor
 * @param siteId
 */
class SiteStatActor(val siteId:SiteId) extends Actor with ActorLogging {
  private val cache:mutable.HashMap[Int, SiteMessageStatistic] = mutable.HashMap.empty
  var dumpSchedule:Option[Cancellable] = None
  def receive = {
    case m:SiteStatActorInit =>{
      log.info("SiteStatActorInit: " + siteId +" "+m.systemId)
    }
    case m:RequestSiteMessageStatisticData => {
      log.info("RequestSiteMessageStatistic:" + siteId+" ["+m+"]")
       sender ! SiteMessageStatisticData(
        m.from, m.to, m.interval,
        new SiteMessageStatistic(
          SiteMessageStatisticId(0),
          intervalStartDate =  new DateTime(),
          interval = "",
          siteId = this.siteId) :: Nil)
    }
    case m:DumpStatToDb =>{
      log.info("dump statistic to database:" + siteId)
      if (m.stopScheduler) {
        dumpSchedule.foreach(_.cancel())
      }
    }
    case m:SiteMessage => {
      val statDetails = Map(("total" -> StatDetails(
        "total",
        vcType = "count",
        count = 1,
        size = m.contentSize.getOrElse(0),
        processingTimeTotal = m.processingTime.getOrElse(0),
        processingTimeMax = m.processingTime.getOrElse(0) ))) ++
      m.result.map(r=>{
        val t = r.result match {
          case "success" => "success"
          case "fail" => "fail"
          case _=> "other"
        }

        (t+"_"+r.vcType -> StatDetails(
          t,
          vcType = r.vcType,
          count = 1,
          size = m.contentSize.getOrElse(0),
          processingTimeTotal = m.processingTime.getOrElse(0),
          processingTimeMax = m.processingTime.getOrElse(0) ))
      }).toMap
      val date = m.modified.toDateTime(DateTimeZone.UTC)
      val toSave  = for {
         interval <- "hour" :: "day" :: "month" :: Nil
         id = SiteMessageStatistic.getHash(interval, SiteMessageStatistic.startDate(interval, date))
         stat = getStat(id, interval, date)
      } yield {
        val rs = stat.results
        val newResults = (rs.keys ++ statDetails.keys).map(key=>{
          val existingRes = rs.get(key)
          val newRes = statDetails.get(key)
          if(existingRes.isDefined) {
            val st = existingRes.get
            (key -> newRes.map(r=>{
              st.copy(
                count = st.count + r.count,
                size = st.size+r.size,
                processingTimeTotal = st.processingTimeTotal+r.processingTimeTotal,
                processingTimeMax = if(r.processingTimeMax>st.processingTimeMax) r.processingTimeMax else st.processingTimeMax
              )
            }).getOrElse(st))
          } else {
            (key -> statDetails(key))
          }
        })
        stat.copy(results = newResults.toMap)
      }
      toSave.foreach(s=>{
        DB.stat.save(s, DBOSecurityPrincipal(), Some(SiteCollectionVersion(siteId)))
     })
    }
    case m  => {
      log.warning("ssa -- unknown message: " + m)
    }
  }

  def getStat(id:Long, interval:String, d:DateTime) = {
    val s =  try {
      DB.stat.get[SiteMessageStatistic](SiteMessageStatisticId(id), DBOSecurityPrincipal(),  Some(SiteCollectionVersion(siteId)))
    } catch {
      case _: Throwable => None
    }
    s.getOrElse(new SiteMessageStatistic(
      id = SiteMessageStatisticId(id),
      siteId = siteId,
      interval = interval,
      intervalStartDate = SiteMessageStatistic.startDate(interval, d)

    ))
  }
}
