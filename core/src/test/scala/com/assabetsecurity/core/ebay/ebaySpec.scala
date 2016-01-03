package com.assabetsecurity.core.ebay


import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.TestKit
import akka.util.Timeout
import com.assabetsecurity.core.db.MongoRepository

import com.assabetsecurity.core.tools.ebay._
import com.mongodb.MongoClient
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import org.slf4s.{Logging, Logger}

/**
 * Created with IntelliJ IDEA.
 * User: alyas
 * Date: 8/18/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
class EbaySpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpec with MustMatchers with BeforeAndAfterAll with Logging {


  def this() = this(ActorSystem("MySpec"))

  implicit val timeout = Timeout(10000)

  "system" should {
    "start EBayLoader" in {

      val a = system.actorOf(Props(new EBayLoader()))
      a ! Start()
      expectMsg(StartSuccess())
      //val future  = ValidationSystem.dispatcher ? SiteMessageValidationRequest(testSite2.id, m)
      //val res = (Await.result(future, timeout.duration)).asInstanceOf[ValidationResults]
    }
    "parse test data" in {

      val items = EBayDataStore.fromXmlString(testXml)
      val client = new MongoClient("localhost")

      log.debug(""+items)
      items.searchResult.foreach(i=>{
        log.debug(""+i)

      })

    }
    "parse categories" in {
      val c0 = new Categories(new Category(0, "asd", List.empty)::Nil)
      log.debug(EBayDataStore.mapper.writeValueAsString(c0))
      val c = EBayDataStore.loadCategories
      log.debug(""+c.categories.mkString("\n"))
      val res = c.categories.filter(_.id==58058).map(c=>{
        c.allChildren
      }).flatten
      log.debug("\n"+res.mkString("\n"))
      log.debug("\n"+res.size)
    }
  }


  val testJson = "{\"findItemsAdvancedResponse\":[{\"ack\":[\"Success\"],\"version\":[\"1.13.0\"],\"timestamp\":[\"2015-11-30T04:37:07.688Z\"],\"searchResult\":[{\"@count\":\"3\",\"item\":[{\"itemId\":[\"161846419842\"],\"title\":[\"Canon EOS 700D \\/ T5I Rebel  18.0 MP Digital SLR DSLR Camera Body - BRAND NEW \"],\"globalId\":[\"EBAY-US\"],\"primaryCategory\":[{\"categoryId\":[\"31388\"],\"categoryName\":[\"Digital Cameras\"]}],\"galleryURL\":[\"http:\\/\\/thumbs3.ebaystatic.com\\/m\\/mt1pTJPnYTzXKRXz60jOGAA\\/140.jpg\"],\"viewItemURL\":[\"http:\\/\\/www.ebay.com\\/itm\\/Canon-EOS-700D-T5I-Rebel-18-0-MP-Digital-SLR-DSLR-Camera-Body-BRAND-NEW-\\/161846419842\"],\"productId\":[{\"@type\":\"ReferenceID\",\"__value__\":\"144819529\"}],\"paymentMethod\":[\"PayPal\"],\"autoPay\":[\"true\"],\"postalCode\":[\"07036\"],\"location\":[\"Linden,NJ,USA\"],\"country\":[\"US\"],\"shippingInfo\":[{\"shippingServiceCost\":[{\"@currencyId\":\"USD\",\"__value__\":\"0.0\"}],\"shippingType\":[\"FlatDomesticCalculatedInternational\"],\"shipToLocations\":[\"US\",\"CA\",\"MX\"],\"expeditedShipping\":[\"true\"],\"oneDayShippingAvailable\":[\"true\"],\"handlingTime\":[\"1\"]}],\"sellingStatus\":[{\"currentPrice\":[{\"@currencyId\":\"USD\",\"__value__\":\"410.86\"}],\"convertedCurrentPrice\":[{\"@currencyId\":\"USD\",\"__value__\":\"410.86\"}],\"sellingState\":[\"Active\"],\"timeLeft\":[\"P2DT4H46M55S\"]}],\"listingInfo\":[{\"bestOfferEnabled\":[\"false\"],\"buyItNowAvailable\":[\"false\"],\"startTime\":[\"2015-10-03T09:19:02.000Z\"],\"endTime\":[\"2015-12-02T09:24:02.000Z\"],\"listingType\":[\"StoreInventory\"],\"gift\":[\"false\"]}],\"returnsAccepted\":[\"true\"],\"condition\":[{\"conditionId\":[\"1000\"],\"conditionDisplayName\":[\"New\"]}],\"isMultiVariationListing\":[\"false\"],\"topRatedListing\":[\"true\"]},{\"itemId\":[\"301761348387\"],\"title\":[\"Canon EOS Rebel T6i\\/750d DSLR Camera (Body Only) Black!! Brand New!!\"],\"globalId\":[\"EBAY-US\"],\"primaryCategory\":[{\"categoryId\":[\"31388\"],\"categoryName\":[\"Digital Cameras\"]}],\"galleryURL\":[\"http:\\/\\/thumbs4.ebaystatic.com\\/m\\/mDaX5gHLUY4x9G6G-y65nQw\\/140.jpg\"],\"viewItemURL\":[\"http:\\/\\/www.ebay.com\\/itm\\/Canon-EOS-Rebel-T6i-750d-DSLR-Camera-Body-Only-Black-Brand-New-\\/301761348387\"],\"paymentMethod\":[\"PayPal\"],\"autoPay\":[\"true\"],\"postalCode\":[\"11694\"],\"location\":[\"Rockaway Park,NY,USA\"],\"country\":[\"US\"],\"shippingInfo\":[{\"shippingServiceCost\":[{\"@currencyId\":\"USD\",\"__value__\":\"0.0\"}],\"shippingType\":[\"Free\"],\"shipToLocations\":[\"US\"],\"expeditedShipping\":[\"true\"],\"oneDayShippingAvailable\":[\"true\"],\"handlingTime\":[\"1\"]}],\"sellingStatus\":[{\"currentPrice\":[{\"@currencyId\":\"USD\",\"__value__\":\"549.99\"}],\"convertedCurrentPrice\":[{\"@currencyId\":\"USD\",\"__value__\":\"549.99\"}],\"sellingState\":[\"Active\"],\"timeLeft\":[\"P6DT12H11M20S\"]}],\"listingInfo\":[{\"bestOfferEnabled\":[\"false\"],\"buyItNowAvailable\":[\"false\"],\"startTime\":[\"2015-10-07T16:43:27.000Z\"],\"endTime\":[\"2015-12-06T16:48:27.000Z\"],\"listingType\":[\"FixedPrice\"],\"gift\":[\"false\"]}],\"returnsAccepted\":[\"true\"],\"condition\":[{\"conditionId\":[\"1000\"],\"conditionDisplayName\":[\"New\"]}],\"isMultiVariationListing\":[\"false\"],\"topRatedListing\":[\"false\"]},{\"itemId\":[\"380923802076\"],\"title\":[\"Canon EOS 5D Mark III 22.3 MP Digital SLR Camera - Black (Body Only) - Brand NEW\"],\"globalId\":[\"EBAY-US\"],\"subtitle\":[\"OVER 700 SOLD by Top Rated PLUS Seller. 1yr Warranty\"],\"primaryCategory\":[{\"categoryId\":[\"31388\"],\"categoryName\":[\"Digital Cameras\"]}],\"galleryURL\":[\"http:\\/\\/thumbs1.ebaystatic.com\\/m\\/mA3INSrCOWSi6uKkEJQ5GJw\\/140.jpg\"],\"viewItemURL\":[\"http:\\/\\/www.ebay.com\\/itm\\/Canon-EOS-5D-Mark-III-22-3-MP-Digital-SLR-Camera-Black-Body-Only-Brand-NEW-\\/380923802076\"],\"paymentMethod\":[\"PayPal\",\"VisaMC\",\"AmEx\",\"Discover\"],\"autoPay\":[\"false\"],\"postalCode\":[\"11210\"],\"location\":[\"Brooklyn,NY,USA\"],\"country\":[\"US\"],\"shippingInfo\":[{\"shippingServiceCost\":[{\"@currencyId\":\"USD\",\"__value__\":\"0.0\"}],\"shippingType\":[\"Free\"],\"shipToLocations\":[\"US\",\"CA\",\"AU\",\"MX\"],\"expeditedShipping\":[\"true\"],\"oneDayShippingAvailable\":[\"true\"],\"handlingTime\":[\"1\"]}],\"sellingStatus\":[{\"currentPrice\":[{\"@currencyId\":\"USD\",\"__value__\":\"2137.2\"}],\"convertedCurrentPrice\":[{\"@currencyId\":\"USD\",\"__value__\":\"2137.2\"}],\"sellingState\":[\"Active\"],\"timeLeft\":[\"P1DT10H28M38S\"]}],\"listingInfo\":[{\"bestOfferEnabled\":[\"false\"],\"buyItNowAvailable\":[\"false\"],\"startTime\":[\"2014-06-09T15:00:45.000Z\"],\"endTime\":[\"2015-12-01T15:05:45.000Z\"],\"listingType\":[\"FixedPrice\"],\"gift\":[\"false\"]}],\"returnsAccepted\":[\"true\"],\"galleryPlusPictureURL\":[\"http:\\/\\/galleryplus.ebayimg.com\\/ws\\/web\\/380923802076_1_6_1.jpg\"],\"condition\":[{\"conditionId\":[\"1000\"],\"conditionDisplayName\":[\"New\"]}],\"isMultiVariationListing\":[\"false\"],\"discountPriceInfo\":[{\"originalRetailPrice\":[{\"@currencyId\":\"USD\",\"__value__\":\"3499.0\"}],\"pricingTreatment\":[\"STP\"],\"soldOnEbay\":[\"false\"],\"soldOffEbay\":[\"false\"]}],\"topRatedListing\":[\"true\"]}]}],\"paginationOutput\":[{\"pageNumber\":[\"5\"],\"entriesPerPage\":[\"3\"],\"totalPages\":[\"25577\"],\"totalEntries\":[\"76731\"]}],\"itemSearchURL\":[\"http:\\/\\/www.ebay.com\\/sch\\/31388\\/i.html?_ddo=1&_ipg=3&_pgn=5\"]}]}"
  val testXml = "<findItemsAdvancedResponse><ack>Success</ack><version>1.13.0</version><timestamp>2015-11-30T05:54:29.856Z</timestamp><searchResult count=\"3\"><item><itemId>161846419842</itemId><title>Canon EOS 700D / T5I Rebel  18.0 MP Digital SLR DSLR Camera Body - BRAND NEW </title><globalId>EBAY-US</globalId><primaryCategory><categoryId>31388</categoryId><categoryName>Digital Cameras</categoryName></primaryCategory><galleryURL>http://thumbs3.ebaystatic.com/m/mt1pTJPnYTzXKRXz60jOGAA/140.jpg</galleryURL><viewItemURL>http://www.ebay.com/itm/Canon-EOS-700D-T5I-Rebel-18-0-MP-Digital-SLR-DSLR-Camera-Body-BRAND-NEW-/161846419842</viewItemURL><productId type=\"ReferenceID\">144819529</productId><paymentMethod>PayPal</paymentMethod><autoPay>true</autoPay><postalCode>07036</postalCode><location>Linden,NJ,USA</location><country>US</country><shippingInfo><shippingServiceCost currencyId=\"USD\">0.0</shippingServiceCost><shippingType>FlatDomesticCalculatedInternational</shippingType><shipToLocations>US</shipToLocations><shipToLocations>CA</shipToLocations><shipToLocations>MX</shipToLocations><expeditedShipping>true</expeditedShipping><oneDayShippingAvailable>true</oneDayShippingAvailable><handlingTime>1</handlingTime></shippingInfo><sellingStatus><currentPrice currencyId=\"USD\">410.86</currentPrice><convertedCurrentPrice currencyId=\"USD\">410.86</convertedCurrentPrice><sellingState>Active</sellingState><timeLeft>P2DT3H29M33S</timeLeft></sellingStatus><listingInfo><bestOfferEnabled>false</bestOfferEnabled><buyItNowAvailable>false</buyItNowAvailable><startTime>2015-10-03T09:19:02.000Z</startTime><endTime>2015-12-02T09:24:02.000Z</endTime><listingType>StoreInventory</listingType><gift>false</gift></listingInfo><returnsAccepted>true</returnsAccepted><condition><conditionId>1000</conditionId><conditionDisplayName>New</conditionDisplayName></condition><isMultiVariationListing>false</isMultiVariationListing><topRatedListing>true</topRatedListing></item><item><itemId>301761348387</itemId><title>Canon EOS Rebel T6i/750d DSLR Camera (Body Only) Black!! Brand New!!</title><globalId>EBAY-US</globalId><primaryCategory><categoryId>31388</categoryId><categoryName>Digital Cameras</categoryName></primaryCategory><galleryURL>http://thumbs4.ebaystatic.com/m/mDaX5gHLUY4x9G6G-y65nQw/140.jpg</galleryURL><viewItemURL>http://www.ebay.com/itm/Canon-EOS-Rebel-T6i-750d-DSLR-Camera-Body-Only-Black-Brand-New-/301761348387</viewItemURL><paymentMethod>PayPal</paymentMethod><autoPay>true</autoPay><postalCode>11694</postalCode><location>Rockaway Park,NY,USA</location><country>US</country><shippingInfo><shippingServiceCost currencyId=\"USD\">0.0</shippingServiceCost><shippingType>Free</shippingType><shipToLocations>US</shipToLocations><expeditedShipping>true</expeditedShipping><oneDayShippingAvailable>true</oneDayShippingAvailable><handlingTime>1</handlingTime></shippingInfo><sellingStatus><currentPrice currencyId=\"USD\">549.99</currentPrice><convertedCurrentPrice currencyId=\"USD\">549.99</convertedCurrentPrice><sellingState>Active</sellingState><timeLeft>P6DT10H53M58S</timeLeft></sellingStatus><listingInfo><bestOfferEnabled>false</bestOfferEnabled><buyItNowAvailable>false</buyItNowAvailable><startTime>2015-10-07T16:43:27.000Z</startTime><endTime>2015-12-06T16:48:27.000Z</endTime><listingType>FixedPrice</listingType><gift>false</gift></listingInfo><returnsAccepted>true</returnsAccepted><condition><conditionId>1000</conditionId><conditionDisplayName>New</conditionDisplayName></condition><isMultiVariationListing>false</isMultiVariationListing><topRatedListing>false</topRatedListing></item><item><itemId>380923802076</itemId><title>Canon EOS 5D Mark III 22.3 MP Digital SLR Camera - Black (Body Only) - Brand NEW</title><globalId>EBAY-US</globalId><subtitle>OVER 700 SOLD by Top Rated PLUS Seller. 1yr Warranty</subtitle><primaryCategory><categoryId>31388</categoryId><categoryName>Digital Cameras</categoryName></primaryCategory><galleryURL>http://thumbs1.ebaystatic.com/m/mA3INSrCOWSi6uKkEJQ5GJw/140.jpg</galleryURL><viewItemURL>http://www.ebay.com/itm/Canon-EOS-5D-Mark-III-22-3-MP-Digital-SLR-Camera-Black-Body-Only-Brand-NEW-/380923802076</viewItemURL><paymentMethod>PayPal</paymentMethod><paymentMethod>VisaMC</paymentMethod><paymentMethod>AmEx</paymentMethod><paymentMethod>Discover</paymentMethod><autoPay>false</autoPay><postalCode>11210</postalCode><location>Brooklyn,NY,USA</location><country>US</country><shippingInfo><shippingServiceCost currencyId=\"USD\">0.0</shippingServiceCost><shippingType>Free</shippingType><shipToLocations>US</shipToLocations><shipToLocations>CA</shipToLocations><shipToLocations>AU</shipToLocations><shipToLocations>MX</shipToLocations><expeditedShipping>true</expeditedShipping><oneDayShippingAvailable>true</oneDayShippingAvailable><handlingTime>1</handlingTime></shippingInfo><sellingStatus><currentPrice currencyId=\"USD\">2137.2</currentPrice><convertedCurrentPrice currencyId=\"USD\">2137.2</convertedCurrentPrice><sellingState>Active</sellingState><timeLeft>P1DT9H11M16S</timeLeft></sellingStatus><listingInfo><bestOfferEnabled>false</bestOfferEnabled><buyItNowAvailable>false</buyItNowAvailable><startTime>2014-06-09T15:00:45.000Z</startTime><endTime>2015-12-01T15:05:45.000Z</endTime><listingType>FixedPrice</listingType><gift>false</gift></listingInfo><returnsAccepted>true</returnsAccepted><galleryPlusPictureURL>http://galleryplus.ebayimg.com/ws/web/380923802076_1_6_1.jpg</galleryPlusPictureURL><condition><conditionId>1000</conditionId><conditionDisplayName>New</conditionDisplayName></condition><isMultiVariationListing>false</isMultiVariationListing><discountPriceInfo><originalRetailPrice currencyId=\"USD\">3499.0</originalRetailPrice><pricingTreatment>STP</pricingTreatment><soldOnEbay>false</soldOnEbay><soldOffEbay>false</soldOffEbay></discountPriceInfo><topRatedListing>true</topRatedListing></item></searchResult><paginationOutput><pageNumber>5</pageNumber><entriesPerPage>3</entriesPerPage><totalPages>25626</totalPages><totalEntries>76878</totalEntries></paginationOutput><itemSearchURL>http://www.ebay.com/sch/31388/i.html?_ddo=1&_ipg=3&_pgn=5</itemSearchURL></findItemsAdvancedResponse>"
}

