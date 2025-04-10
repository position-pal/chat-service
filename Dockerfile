FROM eclipse-temurin:21@sha256:3a5b7c233c489fb0ba5f5a2e633980c036c86097bc388b35adc820892b46f294

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]