#
# Build
#
FROM eclipse-temurin:17.0.9_9-jre-alpine as buildtime

WORKDIR /build
COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17.0.9_9-jre-alpine as builder

COPY --from=buildtime /build/target/*.jar application.jar

RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17.0.9_9-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ADD --chown=spring:spring https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.1.1/applicationinsights-agent-3.1.1.jar /applicationinsights-agent.jar
COPY --chown=spring:spring docker/applicationinsights.json ./applicationinsights.json

COPY --chown=spring:spring --from=builder dependencies/ ./
COPY --chown=spring:spring --from=builder snapshot-dependencies/ ./
# https://github.com/moby/moby/issues/37965#issuecomment-426853382
RUN true
COPY --chown=spring:spring --from=builder spring-boot-loader/ ./
COPY --chown=spring:spring --from=builder application/ ./

EXPOSE 8080

COPY --chown=spring:spring docker/run.sh ./run.sh
RUN chmod +x ./run.sh
ENTRYPOINT ["./run.sh"]
