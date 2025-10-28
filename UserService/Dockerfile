# 1. 빌드 환경
FROM gradle:8.5.0-jdk21-jammy AS build
WORKDIR /home/gradle/project

# 프로젝트 루트 파일 복사
COPY build.gradle settings.gradle ./

# UserService 전체 복사
COPY UserService ./UserService/

# 빌드 실행
RUN gradle :UserService:build -x test --no-daemon

# 2. 실행 환경
FROM --platform=linux/amd64 eclipse-temurin:21-jre-jammy
WORKDIR /app

# plain.jar를 제외한 실행 가능한 JAR만 복사
COPY --from=build /home/gradle/project/UserService/build/libs/*[!plain].jar app.jar

EXPOSE 8081

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENTRYPOINT ["java", "-Xmx400M", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar", "--spring.profiles.active=docker"]