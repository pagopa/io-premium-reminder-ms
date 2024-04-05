#
# Build
#
FROM openjdk:17-oracle as buildtime

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz \
    | tar -xzC /usr/share/maven --strip-components=1 \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

WORKDIR /build
COPY . .

RUN mvn clean package -DskipTests

FROM openjdk:17-oracle as builder

COPY --from=buildtime /build/target/*.jar application.jar

RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:17-oracle

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
