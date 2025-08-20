import com.mycompany.quality.QualityGate

def call(String projectKey, String sonarToken, String sonarUrl) {
    def gate = new QualityGate(this)
    gate.check(projectKey, sonarToken, sonarUrl)
}
