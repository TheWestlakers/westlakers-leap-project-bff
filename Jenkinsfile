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
                        sh './mvnw -B -Pcoverage test'
                    } else {
                        bat 'mvnw.cmd -B -Pcoverage test'
                    }
                }
            }
        }

        stage('Test Coverage Report') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw -B -Pcoverage jacoco:report'
                    } else {
                        bat 'mvnw.cmd -B -Pcoverage jacoco:report'
                    }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'target/jacoco.exec,target/site/jacoco/**'
        }
    }
}
