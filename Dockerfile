FROM eclipse-temurin:21@sha256:6634936b2e8d90ee16eeb94420d71cd5e36ca677a4cf795a9ee1ee6e94379988

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]