# Etapa 1: Construção (Build)
FROM maven:3.9.14-eclipse-temurin-17 AS builder
WORKDIR /workspace
# Copia apenas o pom.xml primeiro para otimizar o cache do Docker
COPY pom.xml .
RUN mvn dependency:go-offline
# Copia o código-fonte
COPY src ./src
# Compila o projeto ignorando os testes nesta fase para ser mais rápido
RUN mvn clean package -DskipTests

# Etapa 2: Execução (Runtime)
FROM docker.io/eclipse-temurin:17
VOLUME /tmp
RUN mkdir -p /app/
RUN mkdir -p /app/logs/
RUN apt-get update \
    && apt-get install -y --no-install-recommends netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# O artifactId do teu web/pom.xml é tascaeats-web e a versão é 1.0
COPY --from=builder /workspace/target/tascaeats-web-1.0.jar /app/app.jar

# Expõe a porta 8080 (o Docker Compose mapeia depois para a 8081 no browser)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
