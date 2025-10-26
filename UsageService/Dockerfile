FROM gradle:8.5.0-jdk21-jammy AS build
WORKDIR /home/gradle/project

COPY build.gradle settings.gradle ./
COPY UsageService ./UsageService/

RUN gradle :UsageService:build -x test --no-daemon

FROM --platform=linux/amd64 eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /home/gradle/project/UsageService/build/libs/*[!plain].jar app.jar

EXPOSE 8082

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENTRYPOINT ["java", "-Xmx400M", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar", "--spring.profiles.active=docker"]
