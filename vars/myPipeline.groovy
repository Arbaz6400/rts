#!/usr/bin/env groovy

import org.enbd.common.GradleWrapper
import org.enbd.common.NexusRest

/**
 * Main pipeline entrypoint for application repos.
 */
def call() {
    pipeline {
        agent any
        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Build') {
                steps {
                    script {
                        def gradle = new GradleWrapper(this)
                        gradle.build("", "clean build", "nexusUser", "nexusPass", "${env.WORKSPACE}/.gradle")
                    }
                }
            }

            stage('Publish') {
                steps {
                    script {
                        def nexus = new NexusRest(this)
                        nexus.publish(
                            "org.enbd",
                            "my-app",
                            "1.0.0-SNAPSHOT",
                            "build/libs/my-app-1.0.0-SNAPSHOT.jar",
                            "http://nexus.company.com",
                            "nexusUser",
                            "nexusPass"
                        )
                    }
                }
            }
        }
    }
}
