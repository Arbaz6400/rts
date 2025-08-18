def repoName = env.JOB_NAME.tokenize('/')[0]   // extract trigger repo name

def scriptPath = ""
switch (repoName) {
    case "service-a":
        scriptPath = "build.groovy"
        break
    case "service-b":
        scriptPath = "test.groovy"
        break
    case "service-c":
        scriptPath = "deploy.groovy"
        break
    default:
        error("No pipeline defined for repo: ${repoName}")
}

echo "Loading ${scriptPath} for ${repoName}"
load(scriptPath)
