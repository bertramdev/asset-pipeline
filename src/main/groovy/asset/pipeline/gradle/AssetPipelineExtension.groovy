package asset.pipeline.gradle

class AssetPipelineExtension {
	def Boolean minifyJs = true
	def Boolean minifyCss = true
	def String  compileDir = 'build/assets'

	Map toMap() {
		return [minifyJs: minifyJs, minifyCss: minifyCss, compileDir:compileDir]
	}
}
