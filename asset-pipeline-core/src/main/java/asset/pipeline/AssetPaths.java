package asset.pipeline;


import static asset.pipeline.AssetPipelineConfigHolder.manifest;
import static com.google.common.base.Strings.isNullOrEmpty;


/**
 * @author Ross Goldberg
 */
public final class AssetPaths {

	public static String getAssetPath(final String path) {
		final String relativePath = trimLeadingSlash(path);
		if(manifest == null) {
			return relativePath;
		}
		final String manifestPath = manifest.getProperty(relativePath);
		return isNullOrEmpty(manifestPath) ? relativePath : manifestPath;
	}


	public static String getResolvedAssetPath(final String path) {
		final String relativePath = trimLeadingSlash(path);
		if(manifest == null) {
			return AssetHelper.fileForFullName(relativePath) != null ? relativePath : null;
		}
		return manifest.getProperty(relativePath);
	}


	public static boolean isAssetPath(final String path) {
		final String relativePath = trimLeadingSlash(path);
		return !isNullOrEmpty(relativePath) && (manifest == null ? AssetHelper.fileForFullName(relativePath) != null : !isNullOrEmpty(manifest.getProperty(relativePath)));
	}


	private static String trimLeadingSlash(final String s) {
		if(isNullOrEmpty(s) || s.charAt(0) != '/') {
			return s;
		}
		return s.substring(1);
	}


	private AssetPaths() {}
}
