package modules

import MongoDao.BusinessRepository
import akka.actor.ActorSystem
import akka.kafka.CommitterSettings
import akka.kafka.ConsumerMessage.CommittableOffset
import akka.kafka.scaladsl.Committer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorAttributes, Materializer, OverflowStrategy}
import config.AppConfig
import kafka.KafkaConsumer
import models.{FinalBusinessDetails, KafkaMessage}
import modules.flows.AppFlows.{decider, insertBusinessRecords, processKafkaMessage, transformBusinessDetails}
import play.api.Logger
import play.api.libs.json.Json

import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessDetailsApplication @Inject() (implicit ac : ActorSystem,
                                            implicit val ec : ExecutionContext,
                                            implicit val mat : Materializer,
                                            val kafkaConsumer: KafkaConsumer,
                                            val businessRepository : BusinessRepository,
                                            val appConfig: AppConfig) {

  // Creating an akka stream to ingest business details data from kafka using alpakka

  final val logger = Logger.apply(classOf[BusinessDetailsApplication])
  val parallelism = 10

  logger.info("Consuming business events from kafka")

  def createJson(message : KafkaMessage) = {
    val jsonMessage = Json.toJson(message.businessDetails.asInstanceOf[FinalBusinessDetails])
    message.copy(kafkaMessage = jsonMessage.toString())
  }

  def writetoJsonFile(batch : Seq[KafkaMessage]) = {
    val writer = new PrintWriter(s"C:\\business_play\\business_insights\\conf\\output-${LocalDateTime.now()
      .toString.substring(0,19).replace("-","").replace(":","")}.json",StandardCharsets.UTF_8)
    batch.foreach(msg => writer.println(msg.kafkaMessage))
    writer.close()
    Source(batch.map(_.kafkaOffset))
      .runWith(Committer.sink(CommitterSettings(ac)))
  }

  kafkaConsumer.kafkaSource
    .buffer(500, OverflowStrategy.backpressure)
    .mapAsync(parallelism)(processKafkaMessage)
    .mapAsync(parallelism)(msg => transformBusinessDetails(msg,appConfig))
    .mapAsync(parallelism)(msg => insertBusinessRecords(msg,businessRepository))
    .map(createJson)
    .groupedWithin(10000, 10.seconds)
    .map(msg => writetoJsonFile(msg))
    //.toMat(Committer.sink(CommitterSettings(ac)))(DrainingControl.apply)
    .toMat(Sink.ignore)(DrainingControl.apply)
    .withAttributes(ActorAttributes.supervisionStrategy(decider))
    .run()

}
