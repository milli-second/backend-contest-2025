pipeline {
  agent any
  
  environment {
    REGISTRY = "nexus.cicd.svc.cluster.local:5000"
    NEXUS_REPO_NAME = "screentimeai-repo" 
    CREDS = 'nexus-credentials'
  }

  stages {
    stage('Checkout') {
      steps {
        echo "📦 Pulling latest code from Git..."
        checkout scm
      }
    }

    stage('Build & Push to Nexus with Jib') {
      steps {
        script {
          def modules = ['UserService', 'UsageService', 'gateway']
          
          withCredentials([usernamePassword(credentialsId: CREDS, usernameVariable: 'U', passwordVariable: 'P')]) {
            for (module in modules) {
              def imageTag = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
              
              echo "🚀 Building and pushing ${module} to Nexus..."
              
              sh """
                export IMAGE_TAG=${imageTag}
                ./gradlew :${module}:jib \
                  -Djib.to.auth.username=\$U \
                  -Djib.to.auth.password=\$P \
                  -x test \
                  --no-daemon
              """
              
              echo "✅ ${module} pushed to ${REGISTRY}/${NEXUS_REPO_NAME}/${module.toLowerCase()}:${imageTag}"
            }
          }
        }
      }
    }
  }

  post {
    success {
      echo "✅ All images pushed to Nexus successfully!"
      echo "📦 Images are available at: ${REGISTRY}/${NEXUS_REPO_NAME}"
    }
    failure {
      echo "❌ Build or push failed. Check logs for details."
    }
  }
}
