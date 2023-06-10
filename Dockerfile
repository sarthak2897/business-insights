FROM openjdk:11
#ADD ./target/universal/stage/bin/business_insights business-insights
EXPOSE 9000
#Install sbt

#Create executable
#RUN sbt clean stage
RUN mkdir -p business-insights-app
WORKDIR business-insights-app
COPY /target/universal/stage .
COPY conf/states.csv states.csv
RUN ["chmod", "u+x,g+x", "bin/business_insights"]
ENTRYPOINT ["bin/business_insights"]
