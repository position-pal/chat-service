FROM eclipse-temurin:21@sha256:1c37779fe2f338d42a7bc8ac439920ef2bf7cebb7deb0970f5733219b17e9868

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]