package asset.pipeline.grails


import static asset.pipeline.grails.UrlBase.*

class AssetMethodTagLib {

	static namespace = 'g'
	static returnObjectForTags = ['assetPath']

	def assetProcessorService

	def assetPath = {final def attrs ->
		final def     src
		final UrlBase urlBase

		if (attrs instanceof Map) {
			src     = attrs.src
			urlBase = attrs.absolute ? SERVER_BASE_URL : CONTEXT_PATH
		}
		else {
			src     = attrs
			urlBase = CONTEXT_PATH
		}

		return assetProcessorService.assetBaseUrl(request, urlBase) + assetProcessorService.getAssetPath(Objects.toString(src))
	}
}
