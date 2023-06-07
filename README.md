# business-insights
The applications ingests data related to different businesses in US from kafka and data is ingested in real time using akka streams via alpakka connector and data is written to mongo DB and data is later written to json files to be ingested in snowflake for reporting purposes.

# Running the app

Install docker and run 'docker-compose up -d' or download the Docker plugin on Intellij to spin the docker containers.
Install sbt and run command 'sbt run' to launch the application.
