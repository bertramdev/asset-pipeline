package asset.pipeline

import asset.pipeline.fs.AssetResolver

/**
 * Holder for Asset Pipeline's configuration
 * Also Provides Helper methods for loading in config from properties
 */
class AssetPipelineConfigHolder {
	static Collection<AssetResolver> resolvers = []
	static Properties manifest
	static Map config = [:]

	public static registerResolver(AssetResolver resolver) {
		resolvers << resolver
	}

}
