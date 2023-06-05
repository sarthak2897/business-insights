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
import models.KafkaMessage
import modules.flows.AppFlows.{createJson, decider, insertBusinessRecords, processKafkaMessage, transformBusinessDetails, writeJson}
import play.api.Logger
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

  def writeJsonAndOffsetCommit(batch: Seq[KafkaMessage]) = {
    writeJson(batch)
    //Commit kafka offsets
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
    .map(writeJsonAndOffsetCommit)
    .toMat(Sink.ignore)(DrainingControl.apply)
    .withAttributes(ActorAttributes.supervisionStrategy(decider))
    .run()

}
