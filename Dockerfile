FROM gradle:8-jdk17@sha256:91d559b8d55f522de5bc6882f73bcedc4e2cc7b0a58e839a9fa0ed95811a988d AS builder

WORKDIR /app

COPY . .

RUN --mount=type=secret,id=github_username,env=GH_USERNAME,required=true \
    --mount=type=secret,id=github_token,env=GH_TOKEN,required=true \
    gradle :entrypoint:shadowJar

FROM openjdk:21-slim@sha256:7072053847a8a05d7f3a14ebc778a90b38c50ce7e8f199382128a53385160688

WORKDIR /app

COPY --from=builder app/entrypoint/build/libs/*.jar app.jar

#Expose something here
# EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
