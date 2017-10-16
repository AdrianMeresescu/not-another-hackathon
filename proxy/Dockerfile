FROM maven:latest

COPY src /src
COPY pom.xml /pom.xml

RUN mvn install

CMD java -jar /target/proxy-4.2-SNAPSHOT.jar