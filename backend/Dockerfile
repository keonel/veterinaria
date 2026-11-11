# Un solo Dockerfile para todos los servicios: se elige cuál con --build-arg MODULE=<servicio>
# Etapa 1: compila todos los módulos con Maven
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /src
COPY . .
RUN mvn -B -DskipTests package

# Etapa 2: imagen final liviana, solo con Java y el JAR del servicio elegido
FROM eclipse-temurin:21-jre
ARG MODULE
WORKDIR /app
# /app/data guarda las claves RSA de usuarios (se monta como volumen)
RUN mkdir /app/data && chown 10001:10001 /app/data
COPY --from=build /src/${MODULE}/target/${MODULE}-1.0.0.jar /app/app.jar
# No se ejecuta como root
USER 10001
ENTRYPOINT ["java","-jar","/app/app.jar"]
