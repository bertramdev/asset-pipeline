package asset.pipeline.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.fs.FileSystemAssetResolver

class AssetPipelinePlugin implements Plugin<Project> {
	void apply(Project project) {
		project.extensions.create('assets', AssetPipelineExtension)
		project.task('asset-precompile') << {
			def resolver = new FileSystemAssetResolver('application','assets')
			AssetPipelineConfigHolder.registerResolver(resolver)

			def assetCompiler = new AssetCompiler(project.assets.toMap(),new GradleEventListener())
			assetCompiler.compile()
		}
	}
}
