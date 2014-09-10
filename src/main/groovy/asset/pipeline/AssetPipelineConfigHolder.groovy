package asset.pipeline

class AssetPipelineConfigHolder {
	static def resolvers = []
	static def manifest
	static def config =[:]

	public static registerResolver(resolver) {
		resolvers << resolver
	}

}
