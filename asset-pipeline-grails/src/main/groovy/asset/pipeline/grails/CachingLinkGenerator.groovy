package asset.pipeline.grails

import grails.core.support.GrailsApplicationAware
import grails.core.GrailsApplication
import asset.pipeline.AssetHelper
import asset.pipeline.AssetPipelineConfigHolder
import groovy.util.logging.Commons
import org.grails.web.servlet.mvc.GrailsWebRequest

import static asset.pipeline.grails.utils.net.HttpServletRequests.getBaseUrlWithScheme
import static org.grails.web.servlet.mvc.GrailsWebRequest.lookup

@Commons
class CachingLinkGenerator extends org.grails.web.mapping.CachingLinkGenerator implements GrailsApplicationAware {
	GrailsApplication grailsApplication

	def assetProcessorService

	CachingLinkGenerator(final String serverUrl) {
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

	@Override
	protected String makeKey(final String prefix, final Map attrs) {
		final StringBuilder sb = new StringBuilder()
		sb.append(prefix)
		if (configuredServerBaseURL == null && isAbsolute(attrs)) {
			final Object base = attrs.base
			if (base != null) {
				sb.append(base)
			} else {
				final GrailsWebRequest req = lookup()
				if (req != null) {
					sb.append(getBaseUrlWithScheme(req.currentRequest).toString())
				}
			}
		}
		appendMapKey(sb, attrs)
		sb.toString()
	}
}
