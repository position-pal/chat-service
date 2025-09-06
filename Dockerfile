FROM eclipse-temurin:21@sha256:34579903d57acb954e7bc6eadf4337fa9f99d1f5ed01129ceb8a12863bf91f8e

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]