# 1. 빌드 환경: Gradle을 사용하여 애플리케이션을 빌드합니다.
FROM gradle:8.5.0-jdk21-jammy AS build
WORKDIR /home/gradle/project

# (Jenkins 워크스페이스 루트에서 실행되므로 '../' 없이 바로 COPY)
COPY build.gradle settings.gradle ./

COPY InsightService/build.gradle ./InsightService/

# 종속성만 먼저 다운로드 (이후 코드가 변경되어도 이 단계는 캐시됨)
# InsightService의 build 파일을 복사했으므로 dependencies 태스크 실행 가능
RUN gradle :InsightService:dependencies --no-daemon || true

# InsightService 모듈 전체를 복사
COPY InsightService ./InsightService/

# :InsightService:clean 태스크는 워크스페이스가 깨끗하므로 제거
RUN gradle :InsightService:build -x test --no-daemon

# 2. 실행 환경: 빌드된 JAR 파일을 실행합니다.
FROM --platform=linux/amd64 eclipse-temurin:21-jre-jammy
WORKDIR /app

# [*[!plain].jar]를 사용하여 Spring Boot 실행 가능 JAR 파일만 복사합니다.
COPY --from=build /home/gradle/project/InsightService/build/libs/*[!plain].jar app.jar

# 컨테이너가 8083 포트를 사용한다고 외부에 알립니다.
EXPOSE 8083

# 시간대 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 컨테이너가 시작될 때 실행될 명령어
ENTRYPOINT ["java", "-Xmx400M", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar", "--spring.profiles.active=docker"]