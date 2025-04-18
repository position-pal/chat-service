FROM eclipse-temurin:21@sha256:1d70e8e91f3916f56bd73581f6ba293077490a7ac273af2afead66cfecfee7cf

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]