# Build: mvn package -DskipTests
# Then:  docker build -t email-service .

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY target/quarkus-app/lib/           /app/lib/
COPY target/quarkus-app/*.jar          /app/
COPY target/quarkus-app/app/           /app/app/
COPY target/quarkus-app/quarkus/       /app/quarkus/

EXPOSE 8080

ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

CMD ["java", "-Dquarkus.http.host=0.0.0.0", \
             "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", \
             "-jar", "/app/quarkus-run.jar"]
