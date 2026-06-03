FROM maven:3.9.14-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true --quiet

FROM docker.io/eclipse-temurin:17
VOLUME /tmp
RUN mkdir -p /app/ /app/logs/ \
    && apt-get update \
    && apt-get install -y --no-install-recommends netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*
COPY --from=builder /workspace/target/tascaeats-1.0.jar /app/app.jar
EXPOSE 8080 9090
ENTRYPOINT ["java","-jar","/app/app.jar"]
