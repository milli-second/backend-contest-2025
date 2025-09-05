# 1. 빌드 환경: Gradle을 사용하여 애플리케이션을 빌드합니다.
FROM gradle:8.5.0-jdk21-alpine AS build
WORKDIR /home/gradle/src

# 프로젝트 루트의 build.gradle과 settings.gradle을 복사
COPY ../build.gradle ../settings.gradle /home/gradle/
COPY build.gradle /home/gradle/src/InsightService/
COPY . /home/gradle/src/InsightService/

# 종속성 다운로드를 위해 빌드 실행 (캐시 최적화)
RUN cd /home/gradle && gradle :InsightService:build --no-daemon -x test || true

# 실제 빌드 실행
RUN cd /home/gradle && gradle :InsightService:build -x test --no-daemon

# 2. 실행 환경: 빌드된 JAR 파일을 실행합니다.
FROM eclipse-temurin:21-jre
WORKDIR /app

# 빌드 환경에서 생성된 JAR 파일을 실행 환경으로 복사합니다.
COPY --from=build /home/gradle/InsightService/build/libs/*.jar app.jar

# 컨테이너가 8082 포트를 사용한다고 외부에 알립니다.
EXPOSE 8082

# 시간대 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 컨테이너가 시작될 때 실행될 명령어
ENTRYPOINT ["java", "-Xmx400M", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar", "--spring.profiles.active=docker"]
