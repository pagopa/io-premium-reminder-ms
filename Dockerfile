#
# Build
#
FROM eclipse-temurin:17.0.10_7-jdk-alpine@sha256:9909002ad26c12ac3be05d258f6424cd25620042ab682358a5dfbfe866885846 as buildtime

RUN apk --no-cache add curl

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz \
    | tar -xzC /usr/share/maven --strip-components=1 \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

WORKDIR /build
COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17.0.10_7-jdk-alpine@sha256:9909002ad26c12ac3be05d258f6424cd25620042ab682358a5dfbfe866885846 as builder

COPY --from=buildtime /build/target/*.jar application.jar

RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17.0.10_7-jdk-alpine@sha256:9909002ad26c12ac3be05d258f6424cd25620042ab682358a5dfbfe866885846

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ADD --chown=spring:spring https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.5.2/applicationinsights-agent-3.5.2.jar /applicationinsights-agent.jar
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
