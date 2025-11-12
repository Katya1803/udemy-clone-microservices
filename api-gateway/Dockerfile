# ---------- BUILD STAGE ----------
FROM maven:3.9.5-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy toàn bộ mã nguồn để Maven thấy tất cả modules
COPY . .

# Build module tương ứng
RUN mvn -f pom.xml clean package -pl api-gateway -am -DskipTests

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Tạo user non-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar ra từ stage build
COPY --from=build /app/api-gateway/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
