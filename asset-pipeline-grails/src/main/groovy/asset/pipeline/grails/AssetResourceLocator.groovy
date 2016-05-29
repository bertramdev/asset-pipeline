package asset.pipeline.grails


import asset.pipeline.AssetHelper
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.DirectiveProcessor
import asset.pipeline.GenericAssetFile
import org.grails.core.io.DefaultResourceLocator
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource


class AssetResourceLocator extends DefaultResourceLocator {

	Resource findResourceForURI(String uri) {
		Resource resource = super.findResourceForURI(uri)
		if(!resource) {
			resource = findAssetForURI(uri)
		}
		return resource
	}

	Resource findAssetForURI(String uri) {
		Resource resource
		if(warDeployed) {
			def manifest = AssetPipelineConfigHolder.manifest
			uri = manifest?.getProperty(uri, uri)

			def assetUri = "assets/${uri}"
			Resource defaultResource = defaultResourceLoader.getResource(assetUri)
			if (!defaultResource?.exists()) {
				defaultResource = defaultResourceLoader.getResource("classpath:${assetUri}")
			}
			if (defaultResource?.exists()) {
				resource = defaultResource
			}
		} else {
			def contentTypes = AssetHelper.assetMimeTypeForURI(uri)
			def contentType
			if(contentTypes) {
				contentType = contentTypes[0]
			}

			def extension = AssetHelper.extensionFromURI(uri)
			def name      = AssetHelper.nameWithoutExtension(uri)
			def assetFile = AssetHelper.fileForUri(name, contentType, extension)
			if(assetFile) {
				if(assetFile instanceof GenericAssetFile) {
					resource = new ByteArrayResource(assetFile.bytes)
				} else {
					def directiveProcessor = new DirectiveProcessor(contentType, null, this.class.classLoader)
					def fileContents = directiveProcessor.compile(assetFile)
					def encoding = assetFile.encoding
					if(encoding) {
						resource = new ByteArrayResource(fileContents.getBytes(encoding))
					} else {
						resource = new ByteArrayResource(fileContents.bytes)
					}
				}
			}
		}
		return resource
	}
}
