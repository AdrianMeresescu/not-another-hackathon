FROM maven:latest
COPY app1/pom.xml /pom.xml
RUN mvn dependency:resolve -T 4
COPY app2/pom.xml /pom.xml
RUN mvn dependency:resolve -T 4


