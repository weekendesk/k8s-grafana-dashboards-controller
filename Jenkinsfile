node {
    ws("workspace/${env.JOB_NAME}/${env.BUILD_NUMBER}".replace('%2F', '_')) {
        def mvn = tool("M3") + "/bin/mvn"

        def ARTIFACT_VERSION;
        def DOCKER_IMAGE_NAME;
        def DOCKER_IMAGE;
		
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
            }

            stage("Create version number") {
                def version = readMavenPom().version.replaceAll("-SNAPSHOT", "").trim()
                ARTIFACT_VERSION = "${version}-${env.BUILD_NUMBER}-${TIMESTAMP}".trim()

                if (GIT_BRANCH_NAME == "master") {
                    ARTIFACT_VERSION = "release-${ARTIFACT_VERSION}"
                } else {
                    ARTIFACT_VERSION = "pr-${GIT_BRANCH_NAME.replaceAll('/', '_')}-${ARTIFACT_VERSION}"
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
                DOCKER_IMAGE_NAME = "949398260329.dkr.ecr.eu-west-1.amazonaws.com/${GIT_REPOSITORY_NAME}:${ARTIFACT_VERSION}";

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
                DOCKER_IMAGE.push(ARTIFACT_VERSION)
                dockerFingerprintFrom dockerfile: 'Dockerfile', image: DOCKER_IMAGE_NAME

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
