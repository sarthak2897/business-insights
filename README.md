# business-insights
The applications ingests data related to different businesses in US from kafka and data is ingested in real time using akka streams via alpakka connector and data is written to mongo DB and data is later written to json files to be ingested in snowflake via snowpipe for reporting purposes.

# Running the app

Install docker and run 'docker-compose up -d' or download the Docker plugin on Intellij to spin the docker containers.
Install sbt and run command 'sbt run' to launch the application.

# Application pipeline architecture diagram


![Business_data_pipeline drawio](https://github.com/sarthak2897/business-insights/assets/60536515/52fac525-7123-41c4-8e62-9155365c70f9)
