pipeline {
  agent any

  environment {
    REGISTRY = 'host.docker.internal:5050'
    CREDS = 'nexus-credentials'
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

          withCredentials([usernamePassword(credentialsId: CREDS, usernameVariable: 'U', passwordVariable: 'P')]) {
            // ✅ 명시적 로그인 (http로)
            sh "echo $P | docker login ${REGISTRY} -u $U --password-stdin"

            for (svc in modules) {
              echo "🚀 Building Docker image for ${svc.name}..."
              sh "docker build -t ${REGISTRY}/screentime/${svc.name}:${BUILD_NUMBER} ${svc.path}"

              echo "📤 Pushing image ${REGISTRY}/screentime/${svc.name}:${BUILD_NUMBER}..."
              sh """
                docker push ${REGISTRY}/screentime/${svc.name}:${BUILD_NUMBER}
                docker tag ${REGISTRY}/screentime/${svc.name}:${BUILD_NUMBER} ${REGISTRY}/screentime/${svc.name}:latest
                docker push ${REGISTRY}/screentime/${svc.name}:latest
              """
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
