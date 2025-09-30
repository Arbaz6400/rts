def call() {
    echo "→ Starting exception list check"

    // Step 1: Checkout the exceptions repo
    dir('exceptions') {
        checkout([
            $class: 'GitSCM',
            branches: [[name: '*/main']],
            userRemoteConfigs: [[
                url: 'https://github.com/Arbaz6400/exception-list.git',
                credentialsId: 'git-creds-id'  // your GitHub credential ID
            ]]
        ])
    }

    // Step 2: Read exceptions YAML
    def exceptionsYaml = readYaml file: 'exceptions/exceptions.yaml'
    echo "→ Exceptions loaded: ${exceptionsYaml}"

    // Step 3: Determine current repo
    def repoName = env.JOB_NAME.split('/')[1]   // Adjust this if your job name format is different
    echo "→ Current repo: ${repoName}"

    // Step 4: Check if current repo is in exception list
    if (exceptionsYaml.contains(repoName)) {
        echo "→ Repo '${repoName}' is in exceptions → skipping scan"
    } else {
        echo "→ Repo '${repoName}' is NOT in exceptions → running scan"
        runScan(repoName) // Call your existing scan function here
    }

    echo "→ Exception list check finished"
}

// Helper function to run your scan (adjust as per your existing scan logic)
def runScan(repo) {
    echo "Running scan for repo: ${repo}"
    // Insert your Sonar scan or other scanning commands here
    sh "echo 'Scan logic goes here for ${repo}'"
}
