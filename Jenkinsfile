properties([
        pipelineTriggers([
                [$class: 'GitHubPushTrigger'], pollSCM('*/1 * * * *')
        ])
])

node {
    ws("workspace/${env.JOB_NAME}/${env.BUILD_NUMBER}".replace('%2F', '_')) {
        def mvn = tool("M3") + "/bin/mvn"

        def ARTIFACT_VERSION;
        def DOCKER_IMAGE_NAME;
        def DOCKER_IMAGE;
        def IS_FEATURE_BRANCH = false
		
        def GIT_REPOSITORY;
		def GIT_BRANCH_NAME;
		def GIT_COMMIT_ID;

        def TIMESTAMP;

        try {

            stage("Checkout source") {
                def scmVars = checkout scm
                GIT_REPOSITORY = scmVars.GIT_URL
                GIT_REPOSITORY_NAME = gitRepositoryName(scmVars)
                GIT_BRANCH_NAME = scmVars.GIT_BRANCH
                GIT_COMMIT_ID = scmVars.GIT_COMMIT

                TIMESTAMP = new java.text.SimpleDateFormat("yyMMddHHmmss").format(new Date())
                IS_FEATURE_BRANCH = GIT_BRANCH_NAME.startsWith("feature/")
                IS_RELEASE_BRANCH = GIT_BRANCH_NAME.startsWith("release/")
                IS_HOTFIX_BRANCH = GIT_BRANCH_NAME.startsWith("hotfix/")
                IS_MASTER_BRANCH = GIT_BRANCH_NAME == "master"
                IS_DEVELOP_BRANCH = GIT_BRANCH_NAME == "develop"
            }

            stage("Create version number") {
                def version = readMavenPom().version.replaceAll("-SNAPSHOT", "").trim()

                if (IS_MASTER_BRANCH) {
                    ARTIFACT_VERSION = "${version}-${env.BUILD_NUMBER}-${TIMESTAMP}".trim()
                } else if (IS_DEVELOP_BRANCH) {
                    ARTIFACT_VERSION = "${version}-dev-${env.BUILD_NUMBER}-${TIMESTAMP}".trim()
                } else if (IS_RELEASE_BRANCH || IS_HOTFIX_BRANCH) {
                    ARTIFACT_VERSION = "${version}-rc-${env.BUILD_NUMBER}-${TIMESTAMP}".trim()
                } else {
                    ARTIFACT_VERSION = "${version}-${GIT_BRANCH_NAME.replaceAll("\\W", "_")}.${env.BUILD_NUMBER}-${TIMESTAMP}".trim()
                }

                sh """
                    git branch ${ARTIFACT_VERSION}
                    ${mvn} --errors \\
                        versions:set \\
                        -DnewVersion=\"${ARTIFACT_VERSION}\" \\
                        -DupdateMatchingVersions=true \\
                        -DgenerateBackupPoms=false \\
                        -DartifactId='*' \\
                        -DgroupId='*'
                """
            }

            stage("Build and test") {
                withCredentials([file(credentialsId: 'MVN_SETTINGS_FILE', variable: 'settingsFile')]) {
                    sh """
                        ${mvn} -X --batch-mode \\
                            --show-version \\
                            --errors \\
                            --update-snapshots \\
                            --settings ${settingsFile} \\
                            verify \\
                    """
                }
            }

            stage("Build docker image") {
                DOCKER_IMAGE_NAME = GIT_REPOSITORY_NAME + ":" + ARTIFACT_VERSION;

                DOCKER_IMAGE = docker.build(
                    DOCKER_IMAGE_NAME, 
                    [
                        [
                            "GIT_REPOSITORY=${GIT_REPOSITORY}",
                            "GIT_BRANCH_NAME=${GIT_BRANCH_NAME}",
                            "GIT_COMMIT_ID=${GIT_COMMIT_ID}",
                            "ARTIFACT_VERSION=${ARTIFACT_VERSION}"
                        ].collect { "--build-arg ${it.toString()}" }.join(" "),
                        "-f Dockerfile",
                        "."
                    ].join(" ")
                )
            }

            stage("Publish version") {
                // docker
                docker.withRegistry(env.PRIVATE_DOCKER_REGISTRY_URL, 'DOCKER_REGISTRY_USER') {
                    DOCKER_IMAGE.push(ARTIFACT_VERSION)
                    dockerFingerprintFrom dockerfile: 'Dockerfile', image: DOCKER_IMAGE_NAME
                }

                // jenkins
                currentBuild.displayName = ARTIFACT_VERSION
            }

        } finally {
            stage("Clean workspace") {
                deleteDir()
            }
        }
    }
}

def gitRepositoryName(scmVars) {    
    tokens = scmVars.GIT_URL.tokenize('/')
    lastToken = tokens.get(tokens.size() - 1)
    return lastToken.substring(0, lastToken.length() - 4)
}
