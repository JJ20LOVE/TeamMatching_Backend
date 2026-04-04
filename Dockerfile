# 重新创建 Dockerfile
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache ca-certificates
WORKDIR /app
# 核心：直接拷贝 GitHub Actions 编译出的 jar 包
COPY target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]