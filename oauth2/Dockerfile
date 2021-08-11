FROM openjdk:8-jdk-alpine
MAINTAINER bigbang
ADD oauth2-service/build/libs/*.jar /opt/apps/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/apps/app.jar"]

