package asset.pipeline.gradle

import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.fs.FileSystemAssetResolver
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Created by davydotcom on 4/21/16.
 */
@CompileStatic
class AssetPluginPackage extends DefaultTask {
    private String destinationDirectoryPath
    @Delegate AssetPipelineExtension pipelineExtension = new AssetPipelineExtension()

    @Input
    File getAssetsDir() {
        def path = pipelineExtension.assetsPath
        return path ? new File(path) : null
    }

    void setAssetsDir(File assetsDir) {
        pipelineExtension.assetsPath = assetsDir.absolutePath
    }

    @OutputDirectory
    File getDestinationDir() {
        destinationDirectoryPath ? new File(destinationDirectoryPath) : null
    }

    void setDestinationDir(File dir) {
        destinationDirectoryPath = dir.canonicalPath
    }

    @InputFiles
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
