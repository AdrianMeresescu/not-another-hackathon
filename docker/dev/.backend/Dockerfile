FROM devplant/not-another-hackathon-base:latest

COPY src /src
COPY pom.xml /pom.xml

RUN mvn install

CMD java -jar /target/backend-4.2-SNAPSHOT.jar