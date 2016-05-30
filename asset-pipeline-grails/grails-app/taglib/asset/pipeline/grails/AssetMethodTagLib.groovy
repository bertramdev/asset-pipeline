package asset.pipeline.grails


import asset.pipeline.AssetPaths

import static org.apache.commons.lang.StringUtils.trimToEmpty


class AssetMethodTagLib {

	static namespace           = 'g'
	static returnObjectForTags = ['assetPath']


	def assetProcessorService
	def grailsLinkGenerator


	def assetPath = {final def attrs ->
		final def    src
		final String baseUrl

		if (attrs instanceof Map) {
			src     = attrs.src
			baseUrl = attrs.absolute ? (grailsLinkGenerator.serverBaseURL ?: '') : trimToEmpty(grailsLinkGenerator.contextPath)
		}
		else {
			src     = attrs
			baseUrl = trimToEmpty(grailsLinkGenerator.contextPath)
		}

		return assetProcessorService.assetBaseUrl(request, baseUrl) + AssetPaths.getAssetPath(Objects.toString(src))
	}
}
