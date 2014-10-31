package asset.pipeline

import asset.pipeline.fs.AssetResolver

/**
 * Holder for Asset Pipeline's configuration
 *
 */
class AssetPipelineConfigHolder {
	static Collection<AssetResolver> resolvers = []
	static def manifest
	static def config =[:]

	public static registerResolver(AssetResolver resolver) {
		resolvers << resolver
	}

}
