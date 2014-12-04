package asset.pipeline

class AssetPipelineResponseBuilder {
	String uri
	String ifNoneMatchHeader
	Integer statusCode = 200

	def headers = [:]

	AssetPipelineResponseBuilder(String uri, String ifNoneMatchHeader=null) {
		this.uri = uri
		this.ifNoneMatchHeader = ifNoneMatchHeader

		if(checkETag()) {
			headers['Vary'] = 'Accept-Encoding'
			headers['Cache-Control'] = 'public, max-age=31536000'
		}
	}

	String getCurrentETag() {
		def manifestPath = uri
		if(uri.startsWith('/')) {
			manifestPath = uri.substring(1) //Omit forward slash
		}

		def manifest = AssetPipelineConfigHolder.manifest

		return manifest?.getProperty(manifestPath) ?: manifestPath
	}

	Boolean checkETag() {
		String etagName = getCurrentETag()
		if(ifNoneMatchHeader && ifNoneMatchHeader == etagName) {
			statusCode = 304
			return false
		}
		headers["ETag"] = etagName
		return true
	}
}
