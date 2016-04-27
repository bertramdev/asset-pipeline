package asset.pipeline.grails.fs;

import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import groovy.lang.*;
import groovy.util.*;

public class SpringResourceAssetResolver
  extends asset.pipeline.fs.AbstractAssetResolver<org.springframework.core.io.Resource> {
;
public SpringResourceAssetResolver
(java.lang.String name, org.springframework.core.io.ResourceLoader resourceLoader, java.lang.String basePath) {
super ((java.lang.String)null);
}
public  asset.pipeline.AssetFile getAsset(java.lang.String relativePath, java.lang.String contentType, java.lang.String extension) { return (asset.pipeline.AssetFile)null;}
public  asset.pipeline.AssetFile getAsset(java.lang.String relativePath, java.lang.String contentType) { return (asset.pipeline.AssetFile)null;}
public  asset.pipeline.AssetFile getAsset(java.lang.String relativePath) { return (asset.pipeline.AssetFile)null;}
public  java.util.List<asset.pipeline.AssetFile> getAssets(java.lang.String basePath, java.lang.String contentType, java.lang.String extension, java.lang.Boolean recursive, asset.pipeline.AssetFile relativeFile) { return (java.util.List<asset.pipeline.AssetFile>)null;}
public  java.util.List<asset.pipeline.AssetFile> getAssets(java.lang.String basePath, java.lang.String contentType, java.lang.String extension, java.lang.Boolean recursive) { return (java.util.List<asset.pipeline.AssetFile>)null;}
public  java.util.List<asset.pipeline.AssetFile> getAssets(java.lang.String basePath, java.lang.String contentType, java.lang.String extension) { return (java.util.List<asset.pipeline.AssetFile>)null;}
public  java.util.List<asset.pipeline.AssetFile> getAssets(java.lang.String basePath, java.lang.String contentType) { return (java.util.List<asset.pipeline.AssetFile>)null;}
public  java.util.List<asset.pipeline.AssetFile> getAssets(java.lang.String basePath) { return (java.util.List<asset.pipeline.AssetFile>)null;}
public  java.lang.String getPrefixPath() { return (java.lang.String)null;}
public  void setPrefixPath(java.lang.String value) { }
public  org.springframework.core.io.ResourceLoader getResourceLoader() { return (org.springframework.core.io.ResourceLoader)null;}
public  void setResourceLoader(org.springframework.core.io.ResourceLoader value) { }
public  org.springframework.core.io.support.PathMatchingResourcePatternResolver getResourceResolver() { return (org.springframework.core.io.support.PathMatchingResourcePatternResolver)null;}
public  void setResourceResolver(org.springframework.core.io.support.PathMatchingResourcePatternResolver value) { }
public  java.util.Map<java.lang.String, org.springframework.core.io.Resource> getCache() { return (java.util.Map<java.lang.String, org.springframework.core.io.Resource>)null;}
public  void setCache(java.util.Map<java.lang.String, org.springframework.core.io.Resource> value) { }
public  asset.pipeline.AssetFile getAsset(java.lang.String relativePath, java.lang.String contentType, java.lang.String extension, asset.pipeline.AssetFile baseFile) { return (asset.pipeline.AssetFile)null;}
public  java.lang.String relativePathToResolver(org.springframework.core.io.Resource file, java.lang.String scanDirectoryPath) { return (java.lang.String)null;}
public  org.springframework.core.io.Resource getRelativeFile(java.lang.String relativePath, java.lang.String name) { return (org.springframework.core.io.Resource)null;}
public  groovy.lang.Closure<java.io.InputStream> createInputStreamClosure(org.springframework.core.io.Resource file) { return (groovy.lang.Closure<java.io.InputStream>)null;}
public  java.lang.String getFileName(org.springframework.core.io.Resource file) { return (java.lang.String)null;}
public  java.util.List<asset.pipeline.AssetFile> getAssets(java.lang.String basePath, java.lang.String contentType, java.lang.String extension, java.lang.Boolean recursive, asset.pipeline.AssetFile relativeFile, asset.pipeline.AssetFile baseFile) { return (java.util.List<asset.pipeline.AssetFile>)null;}
public  java.util.Collection<asset.pipeline.AssetFile> scanForFiles(java.util.List<java.lang.String> excludePatterns, java.util.List<java.lang.String> includePatterns) { return (java.util.Collection<asset.pipeline.AssetFile>)null;}
public  void cacheAllResources() { }
}
