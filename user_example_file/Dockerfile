# ---------- 1단계: 빌드 ----------
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# 의존성 캐시
COPY pom.xml .
RUN mvn dependency:go-offline

# 소스 복사 후 빌드
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- 2단계: 런타임 ----------
FROM eclipse-temurin:21
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8082
ENTRYPOINT ["java","-Xmx400M","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar","--spring.profiles.active=docker"]
