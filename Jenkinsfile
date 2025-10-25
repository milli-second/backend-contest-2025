pipeline {
  agent any

  // tools{
  //   // jenkins Tools 설정에서 정의한 이름(jdk21)과 일치해야함
  //   jdk 'jdk21'
  // }

  environment {
    REGISTRY = "nexus.cicd.svc.cluster.local:5000"
    NEXUS_REPO_NAME = "screentimeai-repo" 
    CREDS = 'nexus-credentials'

    // 배포 환경 변수
    // K8S_NAMESPACE = "screentimeai-test"
    // KUBECONFIG_CREDENTIALS_ID = 'kubernetes-sa' // Jenkins Credentials ID (미리 등록됨)
  }

  stages {
    stage('Checkout') {
      steps {
        echo "📦 Pulling latest code from Git..."
        checkout scm
      }
    }

    stage('Gradle Build (all)') {
      steps {
        echo "⚙️ Running Gradle build for all modules..."
        sh './gradlew :UserService:build :UsageService:build :gateway:build -x test --no-daemon'
      }
    }

    stage('Docker Build & Push (all services)') {
      agent {
        docker {
          image 'docker:dind' // Docker-in-Docker 이미지를 사용
          args '-v /var/run/docker.sock:/var/run/docker.sock --privileged' // 호스트 Docker 소켓 마운트 및 특권 모드 활성화
        }
      }
      steps {
        script {
          def modules = [
            [name: 'userservice', path: 'UserService'],
            [name: 'usageservice', path: 'UsageService'],
            [name: 'gateway', path: 'gateway']
          ]

          withCredentials([usernamePassword(credentialsId: CREDS, usernameVariable: 'U', passwordVariable: 'P')]) {
            // Docker Login: Nexus 내부 주소와 5000번 포트를 사용 (HTTP)
            sh """
                echo "$P" | docker login ${REGISTRY} -u "$U" --password-stdin
            """

            for (svc in modules) {
              def imagePath = "${NEXUS_REGISTRY}/${NEXUS_REPO_NAME}/${svc.name}"
              def imageTag = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"

              echo "🚀 Building Docker image for ${svc.name}..."
              // ⭐️ 빌드 시 태그에 Repository 이름 추가
              sh "docker build -t ${imagePath}:${imageTag} ${svc.path}"

              echo "📤 Pushing image ${imagePath}:${imageTag}..."
              sh "docker push ${imagePath}:${imageTag}"

              // latest 태그 푸시는 선택 사항이므로 주석 처리하거나 제거 (테스트에서는 특정 빌드 번호 태그 사용 권장)
              // sh "docker tag ${imagePath}:${imageTag} ${imagePath}:latest"
              // sh "docker push ${imagePath}:latest"

              // 다음 스테이지에서 사용할 이미지 주소 저장
              //env."IMAGE_NAME_${svc.name.toUpperCase()}" = "${imagePath}:${imageTag}"
            }

            sh "docker logout http://${REGISTRY}"
          }
        }
      }
    }
  }

  post {
    success {
      echo "✅ All images pushed to Nexus successfully!"
    }
    failure {
      echo "❌ Build or push failed. Check logs for details."
    }
  }
}
