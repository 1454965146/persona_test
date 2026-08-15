FROM openjdk:8-jre-alpine
WORKDIR /app
COPY target/persona-api-*.jar app.jar
RUN mkdir -p /app/logs
EXPOSE 8080
ENV MYSQL_USER="" \
    MYSQL_PASSWORD="" \
    AI_API_KEY="" \
    WECHAT_APP_ID="" \
    WECHAT_APP_SECRET=""
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
