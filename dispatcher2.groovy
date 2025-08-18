println "Dispatcher script started"

// Initialize GroovyShell to load external scripts
def shell = new GroovyShell()

// List of scripts to run
def scriptsToRun = [
    'C:/jenkins-scripts/run.groovy',
    'C:/jenkins-scripts/build.groovy'
]

// Loop and execute each script
scriptsToRun.each { path ->
    println "\n--- Running script: ${path} ---"
    def scriptFile = new File(path)
    def parsedScript = shell.parse(scriptFile)
    parsedScript.run()    // Or .call() or .main() depending on how it's written
}

println "\nDispatcher script DONE"
