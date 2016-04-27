grails {
	project {
		source.level = 1.7
		target.level = 1.7

		work.dir = 'target'

		dependency {
			resolver = 'maven'

			resolution = {
				inherits 'global'
				log      'warn'

				repositories {
					mavenLocal()
					grailsCentral()
					mavenCentral()
					jcenter()
					mavenRepo 'http://dl.bintray.com/bertramlabs/asset-pipeline'
				}

				dependencies {
					// Temporary inclusion due to bug in 2.4.2
					compile group: 'cglib',                   name: 'cglib-nodep',         version: '2.2.2', {export = false}
					compile group: 'com.bertramlabs.plugins', name: 'asset-pipeline-core', version: '2.8.0'
					runtime group: 'org.mozilla',             name: 'rhino',               version: '1.7R4'
				}

				plugins {
					test    name: 'code-coverage',       version: '1.2.7',  {export = false}
					build   name: 'release',             version: '3.1.2',  {export = false}
					build   name: 'rest-client-builder', version: '2.0.1',  {export = false}
					compile name: 'webxml',              version: '1.4.1'
				}
			}
		}
	}
}
