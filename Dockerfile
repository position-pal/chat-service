FROM eclipse-temurin:21@sha256:b997045cddee5aa5460aaec871775d8f24e5bea1af1592d8741bd5d4ff793e27

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]