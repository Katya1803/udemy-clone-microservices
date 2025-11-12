# Build stage
FROM maven:3.9.5-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy parent pom and build-parent
COPY pom.xml .
COPY build-parent ./build-parent
COPY common ./common

# Copy api-gateway source
COPY api-gateway/pom.xml ./api-gateway/
COPY api-gateway/src ./api-gateway/src

# Build the application
RUN mvn clean package -pl api-gateway -am -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/api-gateway/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]