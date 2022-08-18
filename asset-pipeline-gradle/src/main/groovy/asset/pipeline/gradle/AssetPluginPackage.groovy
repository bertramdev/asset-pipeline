package asset.pipeline.gradle

import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.fs.FileSystemAssetResolver
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.CacheableTask

/**
 * Created by davydotcom on 4/21/16.
 */
@CompileStatic
@CacheableTask  
class AssetPluginPackage extends DefaultTask {
    private String destinationDirectoryPath
    @Delegate(methodAnnotations = true) private AssetPipelineExtension pipelineExtension = new AssetPipelineExtensionImpl()

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    File getAssetsDir() {
        def path = pipelineExtension.assetsPath
        return path ? new File(path) : null
    }

    void setAssetsDir(File assetsDir) {
        pipelineExtension.assetsPath = assetsDir.path
    }

    @OutputDirectory
    File getDestinationDir() {
        destinationDirectoryPath ? new File(destinationDirectoryPath) : null
    }

    void setDestinationDir(File dir) {
        destinationDirectoryPath = dir.canonicalPath
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    FileTree getSource() {
        FileTree src = getProject().files(this.assetsDir).getAsFileTree();
        return src
    }

    @TaskAction
    @CompileDynamic
    void compile() {
        AssetPipelineConfigHolder.config = configOptions

        FileSystemAssetResolver fsResolver = new FileSystemAssetResolver("manifest",assetsDir.canonicalPath)

        Collection<AssetFile> fileList = fsResolver.scanForFiles([],[])
        def manifestNames = []
        File assetsDir =  new File(destinationDir,"assets")
        if(assetsDir.exists()) {
            assetsDir.deleteDir()
            assetsDir.mkdirs()
        } else {
            assetsDir.mkdirs()
        }

        fileList.eachWithIndex { AssetFile assetFile, index ->
            "Packaging File ${index+1} of ${fileList.size()} - ${assetFile.path}"
            manifestNames << assetFile.path
            File outputFile = new File(assetsDir,assetFile.path)
            if(!outputFile.exists()) {
                outputFile.parentFile.mkdirs()
                outputFile.createNewFile()
            }
            InputStream sourceStream
            OutputStream outputStream
            try {
                sourceStream = assetFile.inputStream
                outputStream = outputFile.newOutputStream()

                outputStream << sourceStream
            } finally {
                try {
                    sourceStream.close()
                } catch(ex1) {
                    //silent fail
                }
                try {
                    outputStream.flush()
                    outputStream.close()
                } catch(ex) {
                    //silent fail
                }

            }

        }
        File assetList = new File(destinationDir,"assets.list")
        if(!assetList.exists()) {
            assetList.parentFile.mkdirs()
            assetList.createNewFile()
        }
        OutputStream assetListOs
        try {
            assetListOs = assetList.newOutputStream()
            assetListOs <<  manifestNames.join("\n");
        } finally {
            assetListOs.flush()
            assetListOs.close()
        }

    }
}
