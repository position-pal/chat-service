FROM gradle:8-jdk17 AS builder

WORKDIR /app

COPY . .

RUN --mount=type=secret,id=GITHUB_USERNAME,env=GH_USERNAME,required=true \
    --mount=type=secret,id=GITHUB_TOKEN,env=GH_TOKEN,required=true \
    gradle :entrypoint:shadowJar

FROM openjdk:21-slim@sha256:7072053847a8a05d7f3a14ebc778a90b38c50ce7e8f199382128a53385160688

WORKDIR /app

COPY --from=builder app/entrypoint/build/libs/*.jar app.jar

#Expose something here
# EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
