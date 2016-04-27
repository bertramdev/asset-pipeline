import asset.pipeline.AssetPipelineConfigHolder
import javax.servlet.ServletContext
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource


class AssetPipelineBootStrap {

	def assetProcessorService
	def grailsApplication


	def init = {final ServletContext servletContext ->
		final ConfigObject conf = grailsApplication.config.grails.assets

		final def storagePath = conf.storagePath
		if (! storagePath) {
			return
		}

		final Properties manifest = AssetPipelineConfigHolder.manifest

		if (manifest) {
			final boolean enableDigests  = true
			final boolean skipNonDigests = true

			if (enableDigests || ! skipNonDigests) {
				final File storageDir = new File((String) storagePath)
				storageDir.mkdirs()

				final ApplicationContext parentContext = grailsApplication.parentContext

				manifest.stringPropertyNames().each {final String propertyName ->
					final File outputFile = new File(storageDir, propertyName)

					new File(outputFile.parent).mkdirs()

					final String propertyValue = manifest.getProperty(propertyName)

					final String assetPath = "assets/${enableDigests ? propertyValue : propertyName}"

					final byte[] fileBytes = parentContext.getResource(assetPath).inputStream.bytes

					outputFile.bytes = fileBytes

					if (enableDigests) {
						new File(storageDir, propertyValue).bytes = fileBytes
					}

					final Resource gzRes = parentContext.getResource("${assetPath}.gz")
					if (gzRes.exists()) {
						final byte[] gzBytes = gzRes.inputStream.bytes

						new File(storageDir, "${propertyName}.gz" ).bytes = gzBytes

						if (enableDigests) {
							new File(storageDir, "${propertyValue}.gz").bytes = gzBytes
						}
					}
				}

				manifest.store(new File(storageDir, 'manifest.properties').newWriter(), '')
			}
		}
	}
}
