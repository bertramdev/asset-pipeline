package asset.pipeline

import asset.pipeline.fs.AssetResolver

/**
 * Holder for Asset Pipeline's configuration
 * Also Provides Helper methods for loading in config from properties
 * @author David Estes
 */
class AssetPipelineConfigHolder {
	public static Collection<AssetResolver> resolvers = []
	public static Properties manifest
	public static Map config = [:]

	public static registerResolver(AssetResolver resolver) {
		resolvers << resolver
	}

	public static Properties getManifest() {
		return this.manifest
	}

	public static void setManifest(Properties manifest) {
		this.manifest = manifest
	}

	public static Map getConfig() {
		return this.config
	}

	public static void setConfig(Map config) {
		this.config = config;
	}

	public static Collection<AssetResolver> getResolvers() {
		return this.resolvers;
	}

	public static void setResolvers(Collection<AssetResolver> resolvers) {
		this.resolvers = resolvers
	}

}
