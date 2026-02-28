# Primera etapa: Construcción con Maven y Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Descargar las dependencias y dejar cache listo
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar la aplicación (sin tests para agilizar la build en prod)
COPY src ./src
RUN mvn package -DskipTests -DskipITs

# Segunda etapa: Imagen mínima de Quarkus para correr la app
FROM registry.access.redhat.com/ubi9/openjdk-21:1.23

ENV LANGUAGE='en_US:en'

# Copiamos la app construida desde la etapa anterior aprovechando las capas de Quarkus Fast-jar
COPY --from=build --chown=185 /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /app/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /app/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /app/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185

# Ejecutamos en entorno de producción (%prod) y vinculamos el host HTTP interno a 0.0.0.0
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
