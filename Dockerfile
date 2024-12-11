FROM gradle:8-jdk17 AS builder

WORKDIR /app

COPY . .

RUN --mount=type=secret,id=github_username,target=/run/secrets/github_username,required=true \
    --mount=type=secret,id=github_token,target=/run/secrets/github_token,required=true \
    GH_USERNAME=$(if [ -f "/run/secrets/github_username" ]; then \
        grep -E "^GH_USERNAME=" /run/secrets/github_username | cut -d '=' -f2; \
    else \
        echo "GH_USERNAME"; \
    fi) \
    GH_TOKEN=$(if [ -f "/run/secrets/github_token" ]; then \
        grep -E "^GH_TOKEN=" /run/secrets/github_token | cut -d '=' -f2; \
    else \
        echo "GH_TOKEN"; \
    fi) \
    gradle :entrypoint:shadowJar

FROM openjdk:21-slim@sha256:7072053847a8a05d7f3a14ebc778a90b38c50ce7e8f199382128a53385160688

WORKDIR /app

COPY --from=builder app/entrypoint/build/libs/*.jar app.jar

# Expose something here
# EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
