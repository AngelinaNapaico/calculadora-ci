pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 15, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    triggers {
        // Revisa GitHub cada ~2 minutos (no requiere exponer Jenkins a internet)
        pollSCM('H/2 * * * *')
    }

    environment {
        MAVEN_CLI_OPTS = '-B -ntp -Dstyle.color=never'
        SLACK_CHANNEL  = '#calculador-pwd'
    }

    stages {
        stage('Preparacion') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
            }
        }

        stage('Codigo Fuente') {
            steps {
                checkout scm
                script {
                    env.INICIADO_POR = env.BUILD_USER_ID ?: 'automatico (SCM)'
                }
            }
        }

        stage('Compilacion') {
            steps {
                sh 'mvn $MAVEN_CLI_OPTS clean compile'
            }
        }

        stage('Pruebas Unitarias') {
            steps {
                sh 'mvn $MAVEN_CLI_OPTS test'
            }
            post {
                always {
                    script {
                        def r = junit testResults: 'target/surefire-reports/TEST-*.xml',
                                      allowEmptyResults: true
                        env.T_TOTAL = "${r.totalCount}"
                        env.T_FAIL  = "${r.failCount}"
                        env.T_SKIP  = "${r.skipCount}"
                    }
                }
            }
        }

        stage('Empaquetado') {
            steps {
                sh 'mvn $MAVEN_CLI_OPTS -DskipTests package'
            }
        }

        stage('Publicar Evidencias') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar, target/surefire-reports/**',
                                 allowEmptyArchive: true,
                                 fingerprint: true
            }
        }
    }

    post {
        always {
            script {
                enviarSlack(currentBuild.currentResult)
            }
            cleanWs(notFailBuild: true)
        }
    }
}

def enviarSlack(String estado) {
    def correcto = (estado == 'SUCCESS')
    def color    = correcto ? '#2eb886' : '#d40b0d'
    def icono    = correcto ? ':white_check_mark:' : ':x:'
    def duracion = currentBuild.durationString.replace(' and counting', '')
    def hora     = sh(returnStdout: true,
                      script: "date -d @\$(( ${currentBuild.startTimeInMillis} / 1000 )) '+%d/%m/%Y %H:%M:%S'").trim()

    def payload = [
        text: "Calculadora CI - Build #${env.BUILD_NUMBER}: ${estado}",
        attachments: [[
            color: color,
            blocks: [
                [type: 'header', text: [type: 'plain_text', text: "Calculadora CI - Build #${env.BUILD_NUMBER}", emoji: true]],
                [type: 'section', fields: [
                    [type: 'mrkdwn', text: "*Estado:*\n${icono} `${estado}`"],
                    [type: 'mrkdwn', text: "*Pruebas:*\n${env.T_TOTAL ?: '0'} ejecutadas, ${env.T_SKIP ?: '0'} omitidas"],
                    [type: 'mrkdwn', text: "*Con fallo:*\n${env.T_FAIL ?: '0'}"],
                    [type: 'mrkdwn', text: "*Duracion:*\n${duracion}"],
                    [type: 'mrkdwn', text: "*Iniciado por:*\n${env.INICIADO_POR ?: 'automatico'}"],
                    [type: 'mrkdwn', text: "*Hora de inicio:*\n${hora}"]
                ]],
                [type: 'section', text: [type: 'mrkdwn', text: "<${env.BUILD_URL}|Ver build completo en Jenkins>"]],
                [type: 'context', elements: [[type: 'mrkdwn', text: "Proyecto calculadora-ci | Canal ${env.SLACK_CHANNEL} | Equipo 5"]]]
            ]
        ]]
    ]

    writeJSON(file: 'slack-payload.json', json: payload)

    sh(label: 'Notificando a Slack', script: '''
        set +e
        if [ -z "${SLACK_WEBHOOK_URL}" ]; then
            echo "AVISO: SLACK_WEBHOOK_URL no configurado; se omite la notificacion."
            exit 0
        fi
        codigo=$(curl -sS -o slack-response.txt -w "%{http_code}" -X POST \
                 -H "Content-Type: application/json" \
                 --data @slack-payload.json "${SLACK_WEBHOOK_URL}")
        if [ "${codigo}" = "200" ]; then
            echo "Slack notificado correctamente."
        else
            echo "AVISO: Slack respondio HTTP ${codigo}"
            cat slack-response.txt
        fi
    ''')
}