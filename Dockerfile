# -------- Build stage --------
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 先拷贝 pom，提升后续构建缓存命中率
COPY pom.xml /app/pom.xml
COPY src /app/src

RUN mvn -DskipTests package -q

# -------- Runtime stage --------
FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache ca-certificates

WORKDIR /app

# 兼容 target 里 jar 命名变化：打包后把任意 jar 复制为 app.jar
RUN set -eux; \
    JAR="$(ls -1 /app/target/*.jar | grep -v 'original' | head -n 1)"; \
    if [ -z "$JAR" ]; then echo "Jar not found under /app/target"; exit 1; fi; \
    cp "$JAR" /app/app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

