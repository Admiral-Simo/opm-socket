# === Stage 1: Build ===
FROM maven:3.8-eclipse-temurin-17 AS builder

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY src ./src

RUN ./mvnw clean package -DskipTests

# === Stage 2: Runtime ===
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
