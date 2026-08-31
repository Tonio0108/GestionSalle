pipeline {
    agent any

    triggers {
        pollSCM('H/2 * * * *')
    }

    environment {
        APP_NAME = 'gestion-salles'
        APP_VERSION = '1.0.0'

        SONAR_HOST_URL = 'http://localhost:9000'
        SONAR_TOKEN = credentials('sonar-token')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Compile') {
            steps {
                bat 'mvn clean compile -B'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test -B'
            }

            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat 'mvn sonar:sonar -Dsonar.host.url=%SONAR_HOST_URL% -Dsonar.token=%SONAR_TOKEN% -B'
                }
            }
        }

        stage('Package JAR') {
            steps {
                bat 'mvn package -DskipTests -B'
            }

            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar',
                                     fingerprint: true
                }
            }
        }
    }

    post {
        success {
            echo 'Build terminé avec succès!'
        }

        failure {
            echo 'Build échoué!'
        }
    }
}
