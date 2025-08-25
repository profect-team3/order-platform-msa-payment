FROM gradle:8.8-jdk17 AS builder
WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle ./
COPY gradle ./gradle
COPY order-platform-msa-payment ./order-platform-msa-payment
COPY order-platform-msa-payment/build.cloud.gradle ./order-platform-msa-payment/build.gradle

RUN ./gradlew :order-platform-msa-payment:bootJar -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /workspace/order-platform-msa-payment/build/libs/*.jar /app/application.jar

EXPOSE 8085
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/application.jar"]
