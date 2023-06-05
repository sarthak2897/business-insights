package models

import akka.kafka.ConsumerMessage.CommittableOffset

case class KafkaMessage(kafkaMessage: String,
                        businessDetails: BusinessDetails,
                        kafkaOffset : CommittableOffset)
