def repoName = env.JOB_NAME.tokenize('/')[0]   // extract trigger repo name

def scriptPath = ""
switch (repoName) {
    case "Leap1":
        scriptPath = "build.groovy"
        break
    case "Leap2":
        scriptPath = "test.groovy"
        break
    case "Leap3":
        scriptPath = "deploy.groovy"
        break
    default:
        error("No pipeline defined for repo: ${repoName}")
}

echo "Loading ${scriptPath} for ${repoName}"
load(scriptPath)
