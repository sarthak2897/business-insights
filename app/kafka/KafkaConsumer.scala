package kafka

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import config.AppConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import javax.inject.{Inject, Singleton}

@Singleton
class KafkaConsumer @Inject() (implicit ac : ActorSystem,
                               appConfig: AppConfig) {

  val kafkaConfig = ac.settings.config.getConfig("akka.kafka.consumer")

  val consumerSettings =
    ConsumerSettings(kafkaConfig,new StringDeserializer,new StringDeserializer)
      .withBootstrapServers(appConfig.bootstrapServers)
      .withGroupId(appConfig.consumerGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest")

  val kafkaSource = Consumer.committableSource(consumerSettings,
    Subscriptions.topics(appConfig.kafkaTopic))
}
