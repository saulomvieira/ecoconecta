FROM eclipse-temurin:21-jdk-alpine

WORKDIR /workspace

# Instalar Maven
RUN apk add --no-cache maven

CMD ["sh"]
