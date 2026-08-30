pipeline {
    agent any

    environment {
        APP_NAME = 'gestion-salles'
        APP_VERSION = '1.0.0'
        // Outils : on utilise MAVEN_HOME et JAVA_HOME définis sur l'hôte.
        // Pour un contrôle précis, configurer des outils nommés dans
        // "Global Tool Configuration" puis décommenter le bloc tools ci-dessous.
        // tools {
        //     maven 'Maven-3.9'
        //     jdk 'JDK-21'
        // }
        // À activer sur un nœud Windows avec WiX Toolset installé :
        // BUILD_EXE = 'true'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn compile -B'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -B'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package JAR') {
            steps {
                sh 'mvn package -DskipTests -B'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Package EXE') {
            when {
                environment name: 'BUILD_EXE', value: 'true'
            }
            steps {
                sh 'mvn jpackage:jpackage -B'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.exe', fingerprint: true
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
