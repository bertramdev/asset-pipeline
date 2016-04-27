package asset.pipeline.grails

import org.grails.web.mapping.DefaultLinkGenerator
import grails.core.support.GrailsApplicationAware
import grails.core.GrailsApplication
import asset.pipeline.AssetHelper
import asset.pipeline.AssetPipelineConfigHolder
import groovy.util.logging.Commons

@Commons
class LinkGenerator extends DefaultLinkGenerator implements GrailsApplicationAware {
	GrailsApplication grailsApplication
	def assetProcessorService


	LinkGenerator(final String serverUrl) {
		super(serverUrl)
	}


	@Override
	String resource(final Map attrs) {
		asset(attrs) ?: super.resource(attrs)
	}

	/**
	 * Finds an Asset from the asset-pipeline based on the file attribute.
	 * @param attrs [file]
	 */
	String asset(final Map attrs) {
		assetProcessorService.asset(attrs, this)
	}

	@Override
	String makeServerURL() {
		assetProcessorService.makeServerURL(this)
	}
}
