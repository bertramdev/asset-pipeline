package asset.pipeline;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import asset.pipeline.fs.AssetResolver;

/**
 * Holder for Asset Pipeline's configuration
 * Also Provides Helper methods for loading in config from properties
 * @author David Estes
 */
public class AssetPipelineConfigHolder {
	public static Collection<AssetResolver> resolvers = new ArrayList<AssetResolver>();
	public static Properties manifest;
	public static Map<String,Object> config = new HashMap<String,Object>();

	public static void registerResolver(AssetResolver resolver) {
		resolvers.add(resolver);
	}

	public static Properties getManifest() {
		return manifest;
	}

	public static void setManifest(Properties manifest) {
		AssetPipelineConfigHolder.manifest = manifest;
	}

	public static Map<String,Object> getConfig() {
		return config;
	}

	public static void setConfig(Map<String, Object> config) {
		AssetPipelineConfigHolder.config = config;
	}

	public static Collection<AssetResolver> getResolvers() {
		return resolvers;
	}

	public static void setResolvers(Collection<AssetResolver> resolvers) {
		AssetPipelineConfigHolder.resolvers = resolvers;
	}

}
