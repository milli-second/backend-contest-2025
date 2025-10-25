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
    kubernetes {
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: docker
    image: docker:24
    command:
    - cat
    tty: true
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
      type: Socket
"""
    }
  }
  steps {
    container('docker') {
      script {
        def modules = [
          [name: 'userservice', path: 'UserService'],
          [name: 'usageservice', path: 'UsageService'],
          [name: 'gateway', path: 'gateway']
        ]

        withCredentials([usernamePassword(credentialsId: CREDS, usernameVariable: 'U', passwordVariable: 'P')]) {
          sh """
            echo "\$P" | docker login ${REGISTRY} -u "\$U" --password-stdin
          """

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
