package com.assabetsecurity.rads

import akka.actor.ActorSystem
import scala.math.random
import org.apache.spark._

/**
 * Created by alyas on 2/8/15.
 */
object RadsService extends App {
  println("RadsService V0.0.1")
  val system = ActorSystem("RadsSystem")
  val conf = new SparkConf().setAppName("Spark Pi").setMaster("local[4]")
  val spark = new SparkContext(conf)

  //spark.getConf.getAkkaConf.toSeq.
  runPi

  def runPi = {

    val slices = 2
    val n = 1000 * slices

    val count = spark.parallelize(1 until n, slices).map { i =>
        val x = random * 2 - 1
        val y = random * 2 - 1
        if (x*x + y*y < 1) 1 else 0
      }.reduce(_ + _)
    println("Pi is roughly " + 4.0 * count / n)
    //spark.stop()
  }

}
