@Library('jenkins-shared-library') _

try {
    mavenPipeline {
        javaVersion = 11
        mavenArgs = "-B clean package"
        deployToSi1WhenMaster = true
    }
} catch (all) {
    office365ConnectorSend message: "${JOB_NAME} failed",
            status: "FAILURE",
            webhookUrl: "https://outlook.office.com/webhook/84c62b4c-81e6-41dc-beb7-4170468b4e9e@82ff090d-4ac0-439f-834a-0c3f3d5f33ce/IncomingWebhook/d0d0b01b57e84b6a87e29bff567ea6dc/4d216574-6175-45b1-90f0-040ddcf0578b"
    throw all
}