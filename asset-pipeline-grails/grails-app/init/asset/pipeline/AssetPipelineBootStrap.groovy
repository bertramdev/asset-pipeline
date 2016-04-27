package asset.pipeline

import asset.pipeline.AssetPipelineConfigHolder

class AssetPipelineBootStrap {

    def grailsApplication

    def init = { servletContext ->
        def storagePath = grailsApplication.config.grails.assets.storagePath
        if (!storagePath) {
            return
        }
        def manifest = AssetPipelineConfigHolder.manifest

        if(manifest) {
            def storageFile = new File(storagePath)
            storageFile.mkdirs()
            manifest.stringPropertyNames().each { propertyName ->
                def propertyValue = manifest.getProperty(propertyName)
                def res = grailsApplication.getParentContext().getResource("classpath:assets/${propertyValue}")
                
                def fileBytes = res.inputStream.bytes

                def outputFile = new File(storagePath, propertyName)
                def parentFile = new File(outputFile.parent)
                parentFile.mkdirs()
                outputFile.bytes = fileBytes
                def outputDigestFile = new File(storagePath, propertyValue)
                outputDigestFile.bytes = fileBytes
                def gzRes = grailsApplication.getParentContext().getResource("classpath:assets/${propertyValue}.gz")
                if(gzRes.exists()) {
                    def gzBytes = gzRes.inputStream.bytes
                    def outputGzFile = new File(storagePath, "${propertyName}.gz")
                    outputGzFile.bytes = gzBytes
                    def outputGzDigestFile = new File(storagePath, "${propertyValue}.gz")
                    outputGzDigestFile.bytes = gzBytes    
                }
            }
            def manifestFile = new File(storagePath,'manifest.properties')
            manifest.store(manifestFile.newWriter(),"")
        }
    }
}
