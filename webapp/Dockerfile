S
FROM maven:latest

COPY src /
COPY pom.xml /

RUN mvn install

CMD java -jar /target/webapp-4.2-SNAPSHOT.jar