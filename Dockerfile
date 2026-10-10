# ============================================================
#  Imagen para desplegar el API en Render (o cualquier Docker)
# ============================================================

# ---------- Etapa 1: compilar el .jar con Gradle ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Primero solo los archivos de Gradle, para que Docker reutilice
# la descarga de dependencias si no cambiaron
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
# gradlew puede venir con saltos de linea de Windows (CRLF): se convierten a LF
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Codigo fuente y compilacion (las pruebas se corren en local, no en el deploy)
COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test
RUN find build/libs -name "*.jar" ! -name "*-plain.jar" -exec cp {} /app/app.jar \;

# ---------- Etapa 2: imagen final, solo con Java y el .jar ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/app.jar app.jar

# Ajustes de memoria para el plan gratis de Render (512 MB de RAM)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"

EXPOSE 8080

# - Render asigna el puerto en la variable PORT (en local se usa 8080)
# - forward-headers-strategy: Render atiende por HTTPS delante de la app;
#   sin esto Swagger arma URLs http:// y "Try it out" falla
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT:-8080} -Dserver.forward-headers-strategy=framework -jar app.jar"]