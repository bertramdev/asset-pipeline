package asset.pipeline.servlet

interface AssetPipelineServletResourceRepository {
    AssetPipelineServletResource getResource(String path)
    AssetPipelineServletResource getGzippedResource(String path)
}
