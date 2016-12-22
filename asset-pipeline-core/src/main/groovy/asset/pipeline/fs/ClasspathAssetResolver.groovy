package asset.pipeline.fs

import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import asset.pipeline.GenericAssetFile
import groovy.util.logging.Commons
import groovy.transform.CompileStatic

/**
* Implementation of the {@link AssetResolver} interface for resolving files on the classpath.
* It is important to note that recursive scanning does not function unless an assets.list file is supplied
*
* @author David Estes
*/
@Commons
public class ClasspathAssetResolver extends AbstractAssetResolver<URL> {
    static String NATIVE_FILE_SEPARATOR = File.separator
    static String DIRECTIVE_FILE_SEPARATOR = '/'

    ClassLoader classLoader
    String prefixPath
    String assetListPath
    Collection<String> assetList = []

    ClasspathAssetResolver(String name, String basePath, String assetListPath=null, ClassLoader classLoader = Thread.currentThread().contextClassLoader) {
        super(name)

        this.classLoader = classLoader
        this.prefixPath = basePath
        this.assetListPath = assetListPath
        loadAssetList()
    }


    private loadAssetList() {
        if (!assetListPath) {
            return
        }
        def resources = classLoader.getResources(assetListPath)
        resources.each { URL res ->
            assetList += res?.text?.tokenize("\n") ?: []
        }
    }

    AssetFile getAsset(String relativePath, String contentType = null, String extension = null, AssetFile baseFile = null) {
        if (!relativePath) {
            return null
        }
        def normalizedPath = AssetHelper.normalizePath(relativePath.replace(NATIVE_FILE_SEPARATOR, DIRECTIVE_FILE_SEPARATOR))
        def specs

        if (contentType) {
            specs = AssetHelper.getPossibleFileSpecs(contentType)
        } else {
            if (!extension) {
                extension = AssetHelper.extensionFromURI(relativePath)
            }
            specs = AssetHelper.assetFileClasses().findAll { it.extensions.contains(extension) }
        }

        AssetFile assetFile = resolveAsset(specs, prefixPath, normalizedPath, baseFile, extension)

        return assetFile
    }

    protected Closure<InputStream> createInputStreamClosure(URL file) {
        if (file) {
            return { -> new BufferedInputStream(file.openStream(), 512) }
        }
        return null
    }

    String relativePathToResolver(URL file, String scanDirectoryPath) {

        if (!file) {
            return null
        }
        def filePath = file.path
        if (filePath.contains(scanDirectoryPath)) {
            def i = filePath.indexOf(scanDirectoryPath)
            return filePath.substring(i + scanDirectoryPath.size() + 1)
        } else {
            throw new RuntimeException("File was not sourced from the same ScanDirectory ${filePath}")
        }
    }

    @CompileStatic
    URL getRelativeFile(String relativePath, String name) {
        if (name.startsWith('/')) {
            name = name.substring(1)
        }

        URL file = classLoader.getResource("$relativePath/$name")
        if (file?.getProtocol()?.equals("file")) {
            if(new File(file.getPath()).isDirectory()) {
                return null
            }
        } else if(file?.getPath()?.endsWith("/")) {
            return null
        }
        return file
    }

    @Override
    @CompileStatic
    protected String getFileName(URL url) {
        String path = url.path
        String name = path
        if (path.lastIndexOf('/'))
            name = path.substring(path.lastIndexOf('/'))
        return name
    }

    @CompileStatic
    List<AssetFile> getAssets(String basePath, String contentType = null, String extension = null, Boolean recursive = true, AssetFile relativeFile = null, AssetFile baseFile = null) {
        def specs
        if (contentType) {
            specs = AssetHelper.getPossibleFileSpecs(contentType)
        }
        if (!assetList) {
            return []
        }

        def extensions = []
        if (extension) {
            extensions << extension
        } else if (specs) {
            for (spec in specs) {
                if(spec.extensions) {
                    extensions.addAll(spec.extensions)
                }
            }
        }

        String translatedBasePath = basePath
        if (!basePath.startsWith('/') && relativeFile != null) {
            List<String> pathArgs = relativeFile.parentPath ? relativeFile.parentPath.split(DIRECTIVE_FILE_SEPARATOR).toList() : new ArrayList<String>()
            //(path should be relative not canonical)
            String[] basePathArgs = basePath.split(DIRECTIVE_FILE_SEPARATOR)
            List<String> parentPathArgs = pathArgs ? pathArgs[0..(pathArgs.size() - 1)] as List<String> : [] as List<String>
            parentPathArgs.addAll(basePathArgs.toList() as List<String>)
            translatedBasePath = (parentPathArgs).join(DIRECTIVE_FILE_SEPARATOR)
            translatedBasePath = AssetHelper.normalizePath(translatedBasePath)
            translatedBasePath = translatedBasePath ? (translatedBasePath + "/") : null
        }

        List<AssetFile> tree = []
        for (String filePath in assetList) {
            if (!translatedBasePath || filePath.startsWith(translatedBasePath)) {
                String[] mimeType = AssetHelper.assetMimeTypeForURI(filePath)
                def url = classLoader.getResource("$prefixPath/$filePath")
                if (url && mimeType && contentType in mimeType) {
                    tree << assetForFile(url, contentType, baseFile, prefixPath)
                }

            }
        }
        return tree
    }

    /**
     * Uses file globbing to scan for files that need precompiled
     */
    public Collection<AssetFile> scanForFiles(List<String> excludePatterns, List<String> includePatterns) {
        def fileList = []
        List<String> excludedPatternList = excludePatterns ? excludePatterns : new ArrayList<String>()
        List<String> includedPatternList = includePatterns ? includePatterns : new ArrayList<String>()

        for (String relativePath in assetList) {
            def entry = classLoader.getResource("$prefixPath/$relativePath")

            if (entry && !isFileMatchingPatterns(relativePath, excludedPatternList) || isFileMatchingPatterns(relativePath, includedPatternList)) {

                def assetFileClass = AssetHelper.assetForFileName(relativePath)
                if (assetFileClass) {
                    fileList << assetFileClass.newInstance(inputStreamSource: createInputStreamClosure(entry), path: relativePath, sourceResolver: this)
                } else {
                    fileList << new GenericAssetFile(inputStreamSource: createInputStreamClosure(entry), path: relativePath)
                }
            }
        }
        return fileList.unique { AssetFile a, AssetFile b -> a.path <=> b.path }
    }

}
