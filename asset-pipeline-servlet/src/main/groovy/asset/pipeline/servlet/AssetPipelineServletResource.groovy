package asset.pipeline.servlet

interface AssetPipelineServletResource {
    Long getLastModified()
    InputStream getInputStream()
}
