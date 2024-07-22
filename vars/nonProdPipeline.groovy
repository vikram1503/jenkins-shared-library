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
        def releaseExists = ""
    }
    parameters{
        // which  component you want to deploy
        // which environment
    }
    stages {
        stage('Deploy'){
             steps{
                script{
                //deploy to specific environment like qa,uat,perf,etc.
                }
            }
        }
        stage('Integration tests') {
            steps {
                script{
                //run integration tests
                }
            }
        }
                
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