FROM eclipse-temurin:21@sha256:22d40ca9ace636f5e3dfe2d597bbb26292e7a301e239ca2556bc24b5d3d8a128

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]