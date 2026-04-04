# -------- Build stage --------
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml /app/pom.xml
COPY src /app/src

RUN mvn -DskipTests package -q \
    && JAR="$(ls -1 /app/target/*.jar | grep -v 'original' | head -n 1)" \
    && test -n "$JAR" \
    && cp "$JAR" /app/app.jar

# -------- Runtime stage --------
FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache ca-certificates

WORKDIR /app

COPY --from=builder /app/app.jar /app/app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
