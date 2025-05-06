FROM eclipse-temurin:21@sha256:459772aa6b6e65953bac0496e319b8d4d719e1c05826f8495c99796cef5d2f49

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]