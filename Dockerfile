FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.source="https://github.com/tvinke/algorilla"
LABEL org.opencontainers.image.description="Algorithmic complexity anti-pattern detector"
LABEL org.opencontainers.image.licenses="Apache-2.0"

COPY cli/build/libs/algorilla-*.jar /opt/algorilla/algorilla.jar

ENTRYPOINT ["java", "-jar", "/opt/algorilla/algorilla.jar"]
