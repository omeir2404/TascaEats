FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN apk add --no-cache netcat-openbsd
RUN ./mvnw clean package -DskipTests --quiet
CMD ["java", "-jar", "target/delivery-service-0.0.1-SNAPSHOT.jar"]
