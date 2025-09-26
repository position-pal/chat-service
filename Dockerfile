FROM eclipse-temurin:25@sha256:21f6c51087c4fa7775b802c9d2ca7e3319eedf32e95ee94cac44a6d0f543a83f

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]