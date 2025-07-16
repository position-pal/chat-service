FROM eclipse-temurin:21@sha256:ddf4a5020434ffa2fa46cc06bfa69cb36dd20014d495fc61c18507eda4c2dca2

WORKDIR /app

COPY ./entrypoint/build/libs/*-all.jar app.jar

# WS
EXPOSE 8080

# GRPC
EXPOSE 5052

# Management
EXPOSE 8558

ENTRYPOINT ["java", "-jar", "app.jar"]