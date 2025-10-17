# --- 1단계: 빌드 환경 ---
# Java 17 JDK가 포함된 이미지를 빌드용으로 사용합니다.
FROM eclipse-temurin:17-jdk-jammy as builder

# 작업 디렉토리 설정
WORKDIR /workspace/app

# Maven Wrapper와 pom.xml 파일을 먼저 복사합니다.
# (소스코드가 바뀌어도 매번 의존성을 새로 받지 않도록 캐싱을 활용합니다)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline

# 나머지 소스 코드를 복사합니다.
COPY src src

# Maven으로 애플리케이션을 빌드합니다. (테스트는 생략)
RUN ./mvnw package -DskipTests


# --- 2단계: 실행 환경 ---
# 실제 실행은 가벼운 JRE(Java 실행 환경) 이미지를 사용합니다.
FROM eclipse-temurin:17-jre-jammy

# 작업 디렉토리 설정
WORKDIR /app

# 1단계(builder)에서 빌드된 최종 jar 파일을 복사해옵니다.
COPY --from=builder /workspace/app/target/*.jar app.jar

# 애플리케이션이 8080 포트를 사용함을 명시합니다.
EXPOSE 8080

# 컨테이너가 시작될 때 jar 파일을 실행합니다.
ENTRYPOINT ["java","-jar","app.jar"]