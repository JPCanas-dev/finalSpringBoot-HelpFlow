# =========================================================================
# Etapa 1: Build con Maven y JDK 21
# =========================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar definicion de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copiar codigo fuente y compilar artefacto
COPY src ./src
RUN mvn clean package -DskipTests

# =========================================================================
# Etapa 2: Runtime con JRE 21 Slim
# =========================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Crear usuario sin privilegios root
RUN addgroup -S helpflow && adduser -S helpflow -G helpflow
USER helpflow

COPY --from=build /app/target/technician-dispatch-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
