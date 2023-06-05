package modules.flows

import MongoDao.BusinessRepository
import akka.kafka.ConsumerMessage
import akka.stream.Supervision
import config.{AppConfig, AppConstants}
import exception.InvalidKafkaMessageException
import models.{FinalBusinessDetails, InitialBusinessDetails, KafkaMessage}
import play.api.Logger
import play.api.libs.json.{JsError, JsPath, JsResult, JsSuccess, JsValue, Json}
import reactivemongo.core.errors.DatabaseException

import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Try, Using}

object AppFlows {

  final val logger = Logger.apply(AppFlows.getClass)

  def processKafkaMessage(msg : ConsumerMessage.CommittableMessage[String, String])
                         (implicit ec : ExecutionContext) = {
    Future(KafkaMessage(msg.record.value(),null,msg.committableOffset))
  }

  def transformBusinessDetails(kafkaMessage: KafkaMessage,appConfig: AppConfig) (implicit ec : ExecutionContext) = {
    val msg: JsValue = Json.parse(kafkaMessage.kafkaMessage)
    val initialBusinessDetails = Json.fromJson[InitialBusinessDetails](msg)
    initialBusinessDetails match {
      case JsSuccess(ibd : InitialBusinessDetails, path: JsPath) =>
        Future(kafkaMessage.copy(businessDetails = createFinalBusinessDetails(ibd,appConfig)))

      case e@JsError(_) =>
        throw new InvalidKafkaMessageException("Error while processing kafka message : "+msg+" : "+JsError.toJson(e).toString())
    }
  }

  def fetchStates() = {
    Using(Source.fromFile(AppConstants.statesCsvPath)) { l =>
      l.getLines().drop(1).map(line => line.split(",").map(_.trim)
        .map(_.replace("\"","")))
      .map(x => (x(1),x(0))).toMap }.get
  }

  def createFinalBusinessDetails(ibd : InitialBusinessDetails,appConfig : AppConfig) = {
    FinalBusinessDetails(businessId = ibd.businessId, name = ibd.name, address = ibd.address, city = ibd.city,
      state = Try(appConfig.statesMap(ibd.state)).getOrElse(ibd.state), postalCode = ibd.postalCode, latitude = ibd.latitude,
      longitude = ibd.latitude, region = region(ibd.latitude,ibd.longitude), stars = ibd.stars, reviewCount = ibd.reviewCount, isOpen = storeStatus(ibd.isOpen),
      categoryType = categoryType(ibd.categories))
  }

  def insertBusinessRecords(kafkaMessage: KafkaMessage,businessRepository: BusinessRepository) (implicit ec : ExecutionContext) ={
    for {
      _ <- businessRepository.insertBusinessRecords(kafkaMessage.businessDetails.asInstanceOf[FinalBusinessDetails])
    } yield kafkaMessage
  }

  def storeStatus(openStatus : Int) = {
    if(openStatus == 0) false
    else true
  }

  def categoryType(category : Option[String]) = {
    category match {
      case Some(cat) => cat
      case None => "N/A"
    }
  }

  def region(latitude : Double, longitude : Double) = {
    if((latitude >= 37.0 && latitude <= 48.0) || (longitude >= -85.0 && longitude <= -66.0))
      "North-Eastern Zone"
    else if((latitude >= 24.0 && latitude <= 35.0) || (longitude >= -98.0 && longitude <= -77.0))
      "Southern Zone"
    else if((latitude >= 36.0 && latitude <= 49.0) || (longitude >= -105.0 && longitude <= -88.0))
      "Mid-Western Zone"
    else if((latitude >= 31.0 && latitude <= 49.0) || (longitude >= -125.0 && longitude <= -102.0))
      "Western Zone"
    else if((latitude >= 31.0 && latitude <= 37.0) || (longitude >= -109.0 && longitude <= -94.5))
      "South Western Zone"
    else if((latitude >= 37.0 && latitude <= 49.0) || (longitude >= -109.0 && longitude <= -89.5))
      "Great Plains Zone"
    else if((latitude >= 37.0 && latitude <= 49.0) || (longitude >= -109.0 && longitude <= -102.0))
      "Rocky Mountains Zone"
    else if((latitude >= 32.5 && latitude <= 49.0) || (longitude >= -124.5 && longitude <= -114.0))
      "South Eastern Zone"
    else
      "Rest of US"
  }

  def createJson(message: KafkaMessage) = {
    val jsonMessage = Json.toJson(message.businessDetails.asInstanceOf[FinalBusinessDetails])
    message.copy(kafkaMessage = jsonMessage.toString())
  }

  def generateJsonFileName() = {
    s"${AppConstants.jsonPathPrefix}-${LocalDateTime.now().toString.substring(0, 19)
        .replace("-", "")
        .replace(":", "")}.json"
  }

  def writeJson(batch : Seq[KafkaMessage]) = {
    val writer = new PrintWriter(generateJsonFileName(), StandardCharsets.UTF_8)
    batch.foreach(msg => writer.println(msg.kafkaMessage))
    writer.close()
  }


  val decider: Supervision.Decider = {
    case e: DatabaseException if e.code.get == 11000 =>
      logger.debug("Duplicate Key Warning: " + e.printStackTrace())
      Supervision.Resume

    case e : InvalidKafkaMessageException =>
      //Write to another kafka error topic
      logger.error("Erroneous kafka message encountered."+ e.printStackTrace())
      Supervision.Resume

    case e: Exception =>
      logger.error("Error occurred: " + e.printStackTrace())
      Supervision.Stop
  }

}
