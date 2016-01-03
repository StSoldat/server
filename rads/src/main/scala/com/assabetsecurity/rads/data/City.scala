package com.assabetsecurity.rads.data

import sun.net.util.IPAddressUtil

/**
 * Created by alyas on 2/10/15.
 */
case class City(country:String, name:String, population:Option[Long] = None, ip:Option[String] = None) {
  def key = country.toLowerCase + "_" + name.toLowerCase
}

