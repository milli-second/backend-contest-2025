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

    stage('Gradle Build (all)') {
      steps {
        echo "⚙️ Running Gradle build for all modules..."
        sh './gradlew :UserService:build :UsageService:build :gateway:build -x test --no-daemon'
      }
    }

    stage('Build & Push with Kaniko') {
      steps {
        script {
          def modules = [
            [name: 'userservice', path: 'UserService'],
            [name: 'usageservice', path: 'UsageService'],
            [name: 'gateway', path: 'gateway']
          ]

          withCredentials([usernamePassword(credentialsId: CREDS, usernameVariable: 'U', passwordVariable: 'P')]) {
            for (svc in modules) {
              def imagePath = "${REGISTRY}/${NEXUS_REPO_NAME}/${svc.name}"
              def imageTag = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
              
              echo "🚀 Building and pushing ${svc.name} with Kaniko..."
              
              sh """
                kubectl run kaniko-${svc.name}-${env.BUILD_NUMBER} \
                  --rm -i --restart=Never \
                  --image=gcr.io/kaniko-project/executor:latest \
                  --overrides='{
                    "spec": {
                      "containers": [{
                        "name": "kaniko",
                        "image": "gcr.io/kaniko-project/executor:latest",
                        "args": [
                          "--context=dir:///workspace",
                          "--dockerfile=/workspace/${svc.path}/Dockerfile",
                          "--destination=${imagePath}:${imageTag}",
                          "--insecure",
                          "--skip-tls-verify"
                        ],
                        "volumeMounts": [{
                          "name": "workspace",
                          "mountPath": "/workspace"
                        }, {
                          "name": "docker-config",
                          "mountPath": "/kaniko/.docker"
                        }]
                      }],
                      "volumes": [{
                        "name": "workspace",
                        "hostPath": {
                          "path": "${WORKSPACE}",
                          "type": "Directory"
                        }
                      }, {
                        "name": "docker-config",
                        "configMap": {
                          "name": "docker-config"
                        }
                      }]
                    }
                  }'
              """
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
