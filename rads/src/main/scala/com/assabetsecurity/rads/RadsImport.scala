package com.assabetsecurity.rads

import java.io.File

import akka.actor.{ActorSystem, Props}
import akka.routing.RoundRobinRouter
import com.github.tototoshi.csv.CSVReader
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by alyas on 2/10/15.
 */
object RadsImport extends App {
  //def fileName = "dbip-city-2015-02.csv"
  //val reader = CSVReader.open(new File(fileName))
  val conf = new SparkConf().setAppName("SparkImport").setMaster("local[20]")
  val spark = new SparkContext(conf)
  val system = ActorSystem("RadsImportSystem")

  run()

  def run() = {


    //create 5 actors
    val actor = system.actorOf(Props[CsvImportActor].withRouter(RoundRobinRouter(nrOfInstances = 5)), "csvImport")
    val wActor  = system.actorOf(Props[WorldCityActor], "WorldCityActor")
    wActor ! UpdateStat()
    val wFile = "./worldcitiespop.txt.full"

    actor ! ImportFile(wFile, wActor.path)

    val ipFile = "./dbip-city-2015-02.csv.full"

    val ipActor  = system.actorOf(Props[WorldCityIPActor], "WorldCityIPActor")
    actor ! ImportFile(ipFile, ipActor.path)


  }

}
