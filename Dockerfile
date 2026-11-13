# One image with the Angular UI and the Spring Boot API, served from a single port.

# 1) Build the Angular UI
FROM node:22-alpine AS ui
WORKDIR /ui
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# 2) Build the Spring Boot jar with the UI bundled as static files
FROM maven:3.9-eclipse-temurin-21 AS api
WORKDIR /api
COPY backend/pom.xml ./
RUN mvn -B -ntp dependency:go-offline
COPY backend/src ./src
COPY --from=ui /ui/dist/frontend/browser ./src/main/resources/static
RUN mvn -B -ntp -DskipTests package

# 3) Small runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN mkdir -p /app/data
COPY --from=api /api/target/salary-management.jar app.jar
ENV SALARY_DB_PATH=/app/data/salary.db
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
