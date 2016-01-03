package com.assabetsecurity.rads

import java.io.File

import akka.actor.{Actor, ActorPath}
import akka.actor.Actor.Receive
import akka.event.Logging
import akka.routing.Broadcast
import com.assabetsecurity.rads.data.{City, Country}
import com.github.tototoshi.csv.CSVReader
import org.apache.spark
import org.apache.spark.mllib.linalg.{Vectors, DenseVector}
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.rdd.RDD

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Created by alyas on 2/10/15.
 */
class CsvImportActor extends Actor {
  val log = Logging(context.system, this)

  override def receive: Receive = {

    case ImportFile(filePath, actorPath) => {
      log.info(s"Import Request: $filePath, actor: ${actorPath.toString}"  )
      log.info(s"Csv Import actor: ${self.path.toString}")
      try {
        val processingActor = context.actorSelection(actorPath)

        val reader = CSVReader.open(new File(filePath))
        log.info(s" Reader delimiter: [${reader.delimiter}], quote: [${reader.quoteChar}]")

        //send first line/header
        reader.readNext().foreach( v=> processingActor ! CsvHeader(v))

        reader.foreach(row => {
          processingActor ! row
        })

        reader.close()
        log.info(s"Import finished. File: $filePath")
        processingActor ! UpdateStat()
      } catch {
        case e:Throwable =>{
          log.error(e, "")
        }
      }
    }
    case stat:Stat => {
      log.info(s"Stat: $stat for ${sender.path.name}")
    }
  }
}

case class ImportFile(filePath:String, actorName:ActorPath)

case class CsvHeader(data:Seq[String])

case class UpdateCity(c:City)

case class UpdateStat()

case class Stat(processed:Int, items:Int, parsingErrors:Seq[String])

class WorldCityIPActor extends Actor {
  val log = Logging(context.system, this)
  var processed:Int = 0
  val cityActor = context.system.actorSelection("user/WorldCityActor")
  override def receive: Actor.Receive = {
    case data:CsvHeader => {
      val c = City(ip = Some(data.data(0)), country = data.data(2), name = data.data(4))
      cityActor  ! UpdateCity(c)
    }
    case data:Seq[String] => {
      //log.info(">>"+data)
      try {
        val c = City(ip = Some(data(0)), country = data(2), name = data(4))
        cityActor  ! UpdateCity(c)
        processed += 1
        if(processed%100000==0) {
          log.info("processed: "+processed)
        }
      } catch {
        case _:Throwable =>
      }
    }
    case m:UpdateStat =>{
      cityActor.forward(m)
    }
    case _=>
  }
}

class WorldCityActor extends Actor {
  val log = Logging(context.system, this)
  //val countries:mutable.HashMap[String, Country] = new mutable.HashMap()
  var processed:Int = 0
  val expectedHeader = List("Country", "City", "AccentCity", "Region", "Population", "Latitude", "Longitude")
  val cityList = new mutable.HashMap[String, City]()

  override def receive: Actor.Receive = {
    case UpdateCity(c) =>{
      cityList(c.key) = cityList.getOrElse(c.key, c).copy(ip=c.ip)
    }
    case data:CsvHeader =>{
      if(data.data  == expectedHeader) {
        log.info("expected header")
      } else {
        log.error("unexpected header")
      }
    }
    case data:Seq[String] =>{
      //log.info(">>"+data)
      val p  = try {
        if(data(4)!="")
          Some(data(4).toLong)
        else
          None
      } catch {
        case e:Throwable => None
      }
      val c = City(country = data(0).toUpperCase, name = data(2), population = p)

      //update population
      cityList(c.key) = cityList.getOrElse(c.key, c).copy(population=c.population)

      processed += 1

      //update progress
      if(processed%100000==0) {
        log.info("processed: "+processed)
      }
    }
    case UpdateStat() => {
      sender ! Stat(processed, 0, Seq.empty)



      val rddCity:RDD[City] = RadsImport.spark.parallelize(cityList.values.filter(_.population.isDefined).toSeq)


      log.info(" reduce:")



      log.info(s" rddCity size: ${rddCity.count()}")
      log.info(s"   with population data: ${rddCity.filter(_.population.isDefined).count()}")

      val byCountry = rddCity.groupBy(c=>c.country)
      log.info(" countries "+byCountry.count())
/*      byCountry.foreach(v=>{
        log.debug(s" country: ${v._1} Population ${v._2.reduceOption((v1, v2)=>v1.copy( population = Some(v1.population.get+v2.population.get))).map(_.population)}")
      })*/

      val r = rddCity.reduce((v1, v2)=>v1.copy( population = Some(v1.population.get+v2.population.get)))
      log.info(" total population:"+r.population)

      log.info(">> UpdateStat Total:" + cityList.size)
      log.info(">> UpdateStat with IP:"+cityList.values.count(_.ip.isDefined))
      log.info(">> UpdateStat with Population:"+cityList.values.count(_.population.isDefined))
      log.info(">> UpdateStat with Population & IP:"+cityList.values.count(c=> c.population.isDefined && c.ip.isDefined))

      log.info(">> UpdateStat Population:"+cityList.values.filter(c=> c.population.isDefined)
        .reduce((c1, c2)=> c1.copy(population = Some(c1.population.get + c2.population.get))))

      log.info(">> UpdateStat Population for defined IPs:"+cityList.values.filter(c=> c.population.isDefined && c.ip.isDefined)
        .reduce((c1, c2)=> c1.copy(population = Some(c1.population.get + c2.population.get))).population)

    }
  }
}
