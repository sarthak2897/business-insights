package config

import com.typesafe.config.ConfigFactory
import modules.flows.AppFlows.fetchStates

import javax.inject.Singleton

@Singleton
class AppConfig {

  //Fetch kafka related configurations
  val config = ConfigFactory.load()
  val bootstrapServers = config.getString("kafka.bootstrap.servers")
  val kafkaTopic = config.getString("kafka.topic")
  val consumerGroup = config.getString("kafka.consumer-group")

  //Fetch US states full names and abbreviations via CSV file
  val statesMap: Map[String, String] = fetchStates()
}
