FROM maven:3.9.4-eclipse-temurin-17 AS builder

WORKDIR /usr/work

COPY . .

RUN mvn clean package -Dmaven.test.skip

FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && \
    apt-get install -y --no-install-recommends postgresql-client python3 python3-pip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/work

COPY --from=builder /usr/work/target/collect-refactoring-jar-with-dependencies.jar /usr/work/collect-refactoring.jar
COPY --from=builder /usr/work/target/collect-metrics-for-refactoring-jar-with-dependencies.jar /usr/work/collect-metrics-for-refactoring.jar

COPY --from=builder /usr/work/settings /usr/work/settings


ENTRYPOINT ["java", "--add-opens java.base/java.lang=ALL-UNNAMED", "-jar", "${JAR_TO_RUN}"]