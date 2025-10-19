pipeline {
  agent any

  environment {
    REGISTRY = 'host.docker.internal:5050'      // Nexus 레지스트리 주소 (포트 맞게 수정)
    CREDS = 'nexus-credentials'       // Jenkins에 등록한 자격증명 ID
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
        sh './gradlew :UserService:build :UsageService:build :gateway:build -x test'
      }
    }

    stage('Docker Build & Push (all services)') {
      steps {
        script {
          def modules = [
            [name: 'userservice', path: 'UserService'],
            [name: 'usageservice', path: 'UsageService'],
            [name: 'gateway', path: 'gateway']
          ]

          docker.withRegistry("http://${REGISTRY}", CREDS) {
            for (svc in modules) {
              echo "🚀 Building Docker image for ${svc.name}..."

              def image = docker.build("${REGISTRY}/screentime/${svc.name}:${BUILD_NUMBER}", svc.path)

              echo "📤 Pushing image ${REGISTRY}/screentime/${svc.name}:${BUILD_NUMBER}..."
              image.push()
              image.push("latest")
            }
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
