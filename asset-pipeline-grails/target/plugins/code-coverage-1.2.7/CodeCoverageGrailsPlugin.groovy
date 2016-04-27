class CodeCoverageGrailsPlugin {
	def version = '1.2.7'

	def grailsVersion = '1.2 > *'

	def pluginExcludes = [
		"grails-app/conf/mavenInfo.groovy"
	]

	def developers = [
      [ name: "Mike Hugo", email: "mike@piragua.com" ],
      [ name: "Jeff Beck" ]
	]

	def title = "Test Code Coverage Plugin"
	def description = "Creates Code Coverage reports for your code"
	def documentation = "https://github.com/beckje01/grails-code-coverage"
	def license = "APACHE"
	def issueManagement = [system: "github", url: "https://github.com/beckje01/grails-code-coverage/issues"]
	def scm = [url: "https://github.com/beckje01/grails-code-coverage"]
}
