# Build stage
FROM maven:3.9.5-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy parent pom and build-parent
COPY pom.xml .
COPY build-parent ./build-parent
COPY common ./common

# Copy user-service source
COPY user-service/pom.xml ./user-service/
COPY user-service/src ./user-service/src

# Build the application
RUN mvn clean package -pl user-service -am -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/user-service/target/*.jar app.jar

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]