def call(dockerRepoName, imageName, portNum) {
    pipeline {
        agent any
        // parameters {
        //     booleanParam(defaultValue: false, description: 'Deploy the App', name: 'DEPLOY')
        // }
        stages {
            stage('Build') {
                steps {
                    sh 'pip install -r requirements.txt'
                }
            }
            stage('Lint') {
                steps {
                    sh 'pylint-fail-under --fail_under 5 *.py'
                }
            }
            stage('Package') {
                when {
                    expression { env.GIT_BRANCH == 'origin/main' }
                }
                steps {
                    withCredentials([string(credentialsId: 'maryamtaer', variable: 'TOKEN')]) {
                        sh "docker login -u 'maryamtaer' -p '$TOKEN' docker.io"
                        sh "docker build -t ${dockerRepoName}:latest --tag maryamtaer/${dockerRepoName}:${imageName} ."
                        sh "docker push maryamtaer/${dockerRepoName}:${imageName}"
                    }
                }
            }
            stage('Zip Artifacts') {
                steps {
                    sh 'zip artifact.zip *.py'
                    archiveArtifacts artifacts: 'app.zip'
                }
           }
            // stage('Deliver') {
            //     when {
            //         expression { params.DEPLOY }
            //     }
            //     steps {
            //         sh "docker stop ${dockerRepoName} || true && docker rm ${dockerRepoName} || true"
            //         sh "docker run -d -p ${portNum}:${portNum} --name ${dockerRepoName} ${dockerRepoName}:latest || true"
            //         sh "docker ps"
            //     }
            // }
        }
    }
}
