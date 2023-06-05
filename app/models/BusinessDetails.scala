package models

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, OFormat, Reads, __}
import reactivemongo.api.bson.{BSONDocument, BSONDocumentWriter}

import java.time.LocalDateTime
import scala.util.Try


sealed trait BusinessDetails

case class InitialBusinessDetails(businessId : String,
                           name : String,
                           address : String,
                           city : String,
                           state : String,
                           postalCode : String,
                           latitude : Double,
                           longitude : Double,
                           stars : Double,
                           reviewCount : Int,
                           isOpen : Int,
                           //attributes : Attributes,
                           categories : Option[String],
                           //hours : Option[String]
                          ) extends BusinessDetails

//case class Attributes(byAppointmentOnly : Option[String])
//
//object Attributes{
//  implicit val readAttributes  =(
//    (__ \ "ByAppointmentOnly").read[Option[String]]
//    )(Attributes.apply _)
//}

object InitialBusinessDetails {
  implicit val reads : Reads[InitialBusinessDetails] = (
    (__ \ "business_id").read[String] and
      (__ \ "name").read[String] and
      (__ \ "address").read[String] and
      (__ \ "city").read[String] and
      (__ \ "state").read[String] and
      (__ \ "postal_code").read[String] and
      (__ \ "latitude").read[Double] and
      (__ \ "longitude").read[Double] and
      (__ \ "stars").read[Double] and
      (__ \ "review_count").read[Int] and
      (__ \ "is_open").read[Int] and
     // (__ \ "attributes").read[Attributes] and
      (__ \ "categories").readNullable[String]
    //  (__ \ "hours").readNullable[String]
  )(InitialBusinessDetails.apply _)
}


case class FinalBusinessDetails(businessId : String,
                                name : String,
                                address : String,
                                city : String,
                                state : String,
                                postalCode : String,
                                latitude : Double,
                                longitude : Double,
                                region : String,
                                stars : Double,
                                reviewCount : Int,
                                isOpen : Boolean,
                                categoryType : String,
                                insertTimestamp : LocalDateTime = LocalDateTime.now()) extends BusinessDetails

object FinalBusinessDetails {

  implicit val format = Json.format[FinalBusinessDetails]
  implicit object BusinessBSONWriter extends BSONDocumentWriter[FinalBusinessDetails] {

    override def writeTry(bd: FinalBusinessDetails): Try[BSONDocument] =
          Try(BSONDocument("_id" -> bd.businessId,
            "name" -> bd.name,
            "address" -> bd.address,
            "city" -> bd.city,
            "state" -> bd.state,
            "postalCode" -> bd.postalCode,
            "latitude" -> bd.latitude,
            "longitude" -> bd.longitude,
            "region" -> bd.region,
            "stars" -> bd.stars,
            "reviewCount" -> bd.reviewCount,
            "isOpen" -> bd.isOpen,
            "categoryType" -> bd.categoryType,
            "insertTimestamp" -> bd.insertTimestamp
            ))
  }
}
