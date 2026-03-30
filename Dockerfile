# -------- Build stage --------
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml /app/pom.xml
COPY src /app/src

RUN mvn -DskipTests package -q

# 把最终可运行 jar 统一命名为 app.jar（在 builder 里做）
RUN set -eux; \
    JAR="$(ls -1 /app/target/*.jar | grep -v 'original' | head -n 1)"; \
    if [ -z "$JAR" ]; then echo "Jar not found under /app/target"; exit 1; fi; \
    cp "$JAR" /app/app.jar

# -------- Runtime stage --------
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache ca-certificates
WORKDIR /app

# 从 builder 拷贝产物进来
COPY --from=builder /app/app.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=""
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
