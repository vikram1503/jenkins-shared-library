def call(Map configMap){
    pipeline {
        agent {
            label 'AGENT-1'
        }
        options {
            // Timeout counter starts AFTER agent is allocated
            timeout(time: 30, unit: 'MINUTES')
            disableConcurrentBuilds()
            ansiColor('xterm')
        }
        environment{
            def appVersion = ''  //variable declaration
            nexusUrl = pipelineGlobals.nexusURL()
            region = pipelineGlobals.region()
            account_id = pipelineGlobals.account_id()
            component = configMap.get("component")
            project = configMap.get("project")
        }
        stages {
            stage('read the version'){
                steps{
                    script{
                        def packageJson = readJSON file: 'package.json'
                        appVersion = packageJson.version
                        echo "application version: $appVersion"
                    }
                }
            }
            stage('Install Dependencies') {
                steps {
                    sh """
                    npm install
                    ls -ltr
                    echo "application version: $appVersion"
                    """
                }
            }
            stage('Build'){
                steps{
                    sh """
                    zip -q -r ${component}-${appVersion}.zip * -x Jenkinsfile -x *.zip
                    ls -ltr
                    """
                }
            }

            stage('Docker build'){
                steps{
                    sh"""
                        aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${account_id}.dkr.ecr.${region}.amazonaws.com

                        docker build -t ${account_id}.dkr.ecr.${region}.amazonaws.com/${project}-${component}:${appVersion} .

                        docker push ${account_id}.dkr.ecr.${region}.amazonaws.com/${project}-${component}:${appVersion}

                    """
                }
            }

            stage('Deploy'){
                steps{
                    sh """
                        aws eks update-kubeconfig --region us-east-1 --name ${project}-dev
                        cd helm
                        sed -i 's/IMAGE_VERSION/${appVersion}/g' values.yaml
                        helm install ${component} -n ${project} .
                    """
                }
            }

            /* stage('Nexus Artifact upload'){
                steps{
                    script{
                        nexusArtifactUploader(
                            nexusVersion: 'nexus3',
                            protocol: 'http',
                            nexusUrl: "${nexusUrl}",
                            groupId: 'com.${project}',
                            version: "${appVersion}",
                            repository: "${component}",
                            credentialsId: 'nexus-auth',
                            artifacts: [
                                [artifactId: "${component}",
                                classifier: '',
                                file: "${component}-" + "${appVersion}" + '.zip',
                                type: 'zip']
                            ]
                        )
                    }
                }
            } */
        }    
        post { 
            always { 
                echo 'I will always say Hello again!'
                deleteDir()
            }
            success {
                echo 'I will run when pipeline is success'
            }
            failure {
                echo 'I will run when pipeline is failed'
            }
        }
    }
}
