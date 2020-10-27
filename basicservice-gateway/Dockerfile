FROM openjdk:8-jdk-alpine
MAINTAINER tiarang
ADD build/libs/gateway*.jar /opt/apps/gateway.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/apps/gateway.jar"]

