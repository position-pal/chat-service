FROM eclipse-temurin:21@sha256:9d2cd32945b17b8687f196c1a9ab664e00726782384f28e61fa99ca310b2697c

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]