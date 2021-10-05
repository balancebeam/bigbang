FROM openjdk:8-jdk-alpine
MAINTAINER djt
ADD build/libs/*.jar /opt/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/app.jar"]

