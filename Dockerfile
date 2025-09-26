FROM eclipse-temurin:25@sha256:284a5db779923fde52120978df1dc7596ddc6a0a8a46d214465e6c0fa9f40df2

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]