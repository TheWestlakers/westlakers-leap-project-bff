pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw -B -DskipTests clean package'
                    } else {
                        bat 'mvnw.cmd -B -DskipTests clean package'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw -B test'
                    } else {
                        bat 'mvnw.cmd -B test'
                    }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
    }
}
