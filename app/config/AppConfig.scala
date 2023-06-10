package config

import com.typesafe.config.ConfigFactory
import modules.flows.AppFlows.fetchStates

import java.time.LocalDateTime
import javax.inject.Singleton

@Singleton
class AppConfig {

  //Fetch kafka related configurations
  val config = ConfigFactory.load()
  val bootstrapServers = config.getString("kafka.bootstrap.servers")
  val kafkaTopic = config.getString("kafka.topic")
  val consumerGroup = config.getString("kafka.consumer-group") + "-" + LocalDateTime.now()

  //Fetch Azure Blob Storage related configurations
  val azureStorageConnectionString = config.getString("azure.storage.connection.string")
  val azureStorageContainerName = config.getString("azure.storage.container.name")

  //Fetch US states full names and abbreviations via CSV file
  val statesMap: Map[String, String] = fetchStates()
}
