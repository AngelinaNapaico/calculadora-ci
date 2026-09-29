FROM jenkins/jenkins:lts-jdk17

USER root
RUN apt-get update \
    && apt-get install -y --no-install-recommends git maven curl \
    && rm -rf /var/lib/apt/lists/*

ENV TZ="America/Lima"

USER jenkins

# Plugins minimos que necesita el Jenkinsfile
RUN jenkins-plugin-cli --plugins \
    workflow-aggregator \
    git \
    junit \
    ws-cleanup \
    pipeline-utility-steps \
    timestamper \
    pipeline-stage-view