# Capa 1: Compilación con Gradle (Usa Java 21)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
# Compilamos el proyecto (ignorando los tests para ir más rápido)
RUN ./gradlew build -x test

# Capa 2: Imagen ligera para ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copiamos el .jar generado en la capa anterior
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar
# Exponemos el puerto por defecto de Spring
EXPOSE 8080
# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]