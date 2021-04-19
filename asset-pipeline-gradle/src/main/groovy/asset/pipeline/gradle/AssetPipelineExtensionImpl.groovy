package asset.pipeline.gradle

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 * Allows configuration of the Gradle plugin
 *
 * @author David Estes
 * @author Graeme Rocher
 */
class AssetPipelineExtensionImpl implements AssetPipelineExtension {
    boolean minifyJs = true
    boolean enableSourceMaps = true
    boolean minifyCss = true
    boolean enableDigests = true
    boolean skipNonDigests = false
    boolean enableGzip = true
    boolean packagePlugin=false
    boolean developmentRuntime=true
    boolean verbose = true
    Integer maxThreads=null
    String compileDir = 'build/assets'
    String assetsPath = 'src/assets'
	String jarTaskName
    Map minifyOptions
    Map configOptions

    List excludesGzip
    List excludes = []
    List includes = []
    List<String> resolvers = []

    void from(String resolverPath) {
        resolvers += resolverPath
    }

    Map toMap() {
        return [minifyJs: minifyJs, minifyCss: minifyCss, minifyOptions: minifyOptions, compileDir: compileDir, enableGzip: enableGzip, skipNonDigests: skipNonDigests, enableDigests: enableDigests, excludesGzip: excludesGzip, enableSourceMaps: enableSourceMaps, maxThreads: maxThreads]
    }
}
