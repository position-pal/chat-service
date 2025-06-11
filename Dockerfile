FROM eclipse-temurin:21@sha256:2cc80b4288f6240734ecd1acae01d20afa56f86327b9ae207a9468363422c974

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]