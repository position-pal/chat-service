FROM eclipse-temurin:21@sha256:b37f7053b1b366c62bd8d332508852a1281b59656af5bd6cf27a008d91111214

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]