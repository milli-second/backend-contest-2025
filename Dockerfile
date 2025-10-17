# Node.js 18 버전을 기반으로 이미지를 시작합니다. (버전은 프로젝트에 맞게 변경)
FROM node:18-alpine

# 컨테이너 내부에서 작업할 디렉토리를 생성합니다.
WORKDIR /usr/src/app

# package.json 과 package-lock.json 파일을 먼저 복사하여 의존성을 설치합니다.
# (소스코드가 바뀌어도 매번 모든 의존성을 새로 설치하지 않도록 하기 위함입니다.)
COPY package*.json ./
RUN npm install

# 프로젝트 소스 코드를 전부 복사합니다.
COPY . .

# 애플리케이션이 외부와 통신할 포트를 지정합니다. (예: 3000)
EXPOSE 3000

# 컨테이너가 시작될 때 최종적으로 실행할 명령어를 정의합니다.
CMD [ "node", "app.js" ]