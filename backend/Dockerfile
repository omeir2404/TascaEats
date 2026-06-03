FROM maven:3.9.14-eclipse-temurin-17 AS builder
ADD pom.xml .
RUN mvn dependency:go-offline
COPY . .
RUN mvn clean package -Dmaven.test.skip=true --quiet
FROM docker.io/eclipse-temurin:17
VOLUME /tmp
RUN mkdir -p /app/
RUN mkdir -p /app/logs/
RUN apt-get update \
    && apt-get install -y --no-install-recommends netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*
COPY --from=builder /target/tascaeats-1.0.jar /app/app.jar
EXPOSE 8080 9090
ENTRYPOINT ["java","-jar", "/app/app.jar"]
