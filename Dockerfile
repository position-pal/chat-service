FROM eclipse-temurin:21@sha256:1bd9ac00d3acc92a137cf9aca442a42e6f777736524b87cc376342ef5402e2d4

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]