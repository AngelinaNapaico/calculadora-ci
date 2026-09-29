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
        SLACK_CHANNEL = '#calculador-pwd'
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
                    env.INICIADO_POR = env.BUILD_USER_ID ?: 'automatico (webhook/SCM)'
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
                    junit testResults: 'target/surefire-reports/TEST-*.xml',
                          allowEmptyResults: true,
                          keepLongStdio: true
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
                def resumen = resumenPruebas()
                enviarSlack(construirPayloadSlack(currentBuild.currentResult, resumen))
            }
            cleanWs(notify: false)
        }
    }
}

def formatearDuracion(long milisegundos) {
    long segundos = milisegundos.intdiv(1000)
    if (segundos < 60) { return "${segundos} s" }
    long minutos = segundos.intdiv(60)
    long resto = segundos % 60
    if (minutos < 60) { return "${minutos} min ${resto} s" }
    return "${minutos.intdiv(60)} h ${minutos % 60} min"
}

@NonCPS
def parsearSurefire(String xml) {
    def suite = new XmlSlurper().parseText(xml)
    def r = [total: 0, fallidos: 0, errores: 0, omitidos: 0, fallos: []]
    r.total    = (suite.@tests.text()    ?: '0') as int
    r.fallidos = (suite.@failures.text() ?: '0') as int
    r.errores  = (suite.@errors.text()   ?: '0') as int
    r.omitidos = (suite.@skipped.text()  ?: '0') as int
    suite.testcase.each { caso ->
        if (caso.failure.size() > 0 || caso.error.size() > 0) {
            r.fallos << (caso.@classname.text() + '.' + caso.@name.text())
        }
    }
    return r
}

def resumenPruebas() {
    def resumen = [total: 0, fallidos: 0, errores: 0, omitidos: 0, fallos: []]
    def archivos = findFiles(glob: 'target/surefire-reports/TEST-*.xml')
    if (!archivos) {
        echo 'No se encontraron reportes de Surefire.'
        return resumen
    }
    for (archivo in archivos) {
        def r = parsearSurefire(readFile(archivo.path))
        resumen.total    += r.total
        resumen.fallidos += r.fallidos
        resumen.errores  += r.errores
        resumen.omitidos += r.omitidos
        resumen.fallos.addAll(r.fallos)
    }
    return resumen
}

def construirPayloadSlack(String estado, Map resumen) {
    def correcto = (estado == 'SUCCESS')
    def color = correcto ? '#2eb886' : '#d40b0d'
    def icono = correcto ? ':white_check_mark:' : ':x:'
    def duracion = formatearDuracion(System.currentTimeMillis() - currentBuild.startTimeInMillis)
    def entradas = [
        [type: 'mrkdwn', text: "*Estado:*\n${icono} `${estado}`"],
        [type: 'mrkdwn', text: "*Pruebas:*\n${resumen.total} ejecutadas, ${resumen.omitidos} omitidas"],
        [type: 'mrkdwn', text: "*Con error:*\n${resumen.fallidos + resumen.errores}"],
        [type: 'mrkdwn', text: "*Duracion:*\n${duracion}"],
        [type: 'mrkdwn', text: "*Iniciado por:*\n${env.INICIADO_POR ?: 'automatico'}"],
        [type: 'mrkdwn', text: "*Hora de inicio:*\n${currentBuild.getTimestampString()}"]
    ]
    def bloques = [
        [type: 'header', text: [type: 'plain_text', text: "Calculadora CI - Build #${env.BUILD_NUMBER}", emoji: true]],
        [type: 'section', fields: entradas],
        [type: 'section', text: [type: 'mrkdwn',
                                 text: "<${env.BUILD_URL}|Ver build completo en Jenkins>"]]
    ]
    if (resumen.fallos) {
        def detalle = resumen.fallos.take(5).collect { "• `${it}`" }.join('\n')
        if (resumen.fallos.size() > 5) {
            detalle += "\n• ... y ${resumen.fallos.size() - 5} mas"
        }
        bloques << [type: 'section', text: [type: 'mrkdwn', text: "*Pruebas con error:*\n${detalle}"]]
    }
    bloques << [type: 'context', elements: [[type: 'mrkdwn',
                text: "Proyecto calculadora-ci | Canal ${env.SLACK_CHANNEL} | Equipo 5"]]]
    return [
        text: "Calculadora CI - Build #${env.BUILD_NUMBER}: ${estado} (${resumen.total} pruebas)",
        blocks: bloques,
        attachments: [[color: color]]
    ]
}

def enviarSlack(Map payload) {
    def json = groovy.json.JsonOutput.toJson(payload)
    writeFile(file: 'slack-payload.json', text: json)
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