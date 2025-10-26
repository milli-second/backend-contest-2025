pipeline {
  agent any
  
  environment {
    REGISTRY = "nexus.cicd.svc.cluster.local:5000"
    NEXUS_REPO_NAME = "screentimeai-repo" 
    CREDS = 'nexus-credentials'
    DOCKER_HOST = 'tcp://localhost:2375'
    PATH = "/tmp:${env.PATH}"
  }

  stages {
    stage('Checkout') {
      steps {
        echo "📦 Pulling latest code from Git..."
        checkout scm
      }
    }

    stage('Gradle Build') {
      steps {
        echo "⚙️ Running Gradle build..."
        sh './gradlew :UserService:build :UsageService:build :gateway:build -x test --no-daemon'
      }
    }

    stage('Wait for Docker') {
      steps {
        script {
          echo "⏳ Waiting for Docker daemon..."
          sh '''
            for i in {1..30}; do
              if docker info > /dev/null 2>&1; then
                echo "✅ Docker daemon is ready!"
                docker version
                break
              fi
              echo "Waiting... ($i/30)"
              sleep 2
            done
          '''
        }
      }
    }

    stage('Docker Build & Push') {
      steps {
        script {
          def modules = [
            [name: 'userservice', path: 'UserService'],
            [name: 'usageservice', path: 'UsageService'],
            [name: 'gateway', path: 'gateway']
          ]

          withCredentials([usernamePassword(credentialsId: CREDS, usernameVariable: 'U', passwordVariable: 'P')]) {
            sh 'echo "$P" | docker login ${REGISTRY} -u "$U" --password-stdin'

            for (svc in modules) {
              def imagePath = "${REGISTRY}/${NEXUS_REPO_NAME}/${svc.name}"
              def imageTag = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
              
              echo "🚀 Building ${svc.name}..."
              sh "docker build -t ${imagePath}:${imageTag} ${svc.path}"
              
              echo "📤 Pushing ${imagePath}:${imageTag}..."
              sh "docker push ${imagePath}:${imageTag}"
            }

            sh "docker logout ${REGISTRY}"
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
