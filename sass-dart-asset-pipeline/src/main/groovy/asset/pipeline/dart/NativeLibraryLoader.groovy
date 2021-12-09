package asset.pipeline.dart

import asset.pipeline.AssetPipelineConfigHolder
import com.caoccao.javet.enums.JSRuntimeType
import com.caoccao.javet.interop.loader.JavetLibLoader
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.util.jar.JarEntry
import java.util.jar.JarFile

import static com.caoccao.javet.utils.JavetOSUtils.*

@Slf4j
@CompileStatic
class NativeLibraryLoader {
    static final String JAVET_VERSION = NativeLibraryLoader.class.classLoader.getResource("javet-version.txt").openStream().text
    static final String BASE_URL = AssetPipelineConfigHolder.config?.javetBaseUrl ?: "https://repo1.maven.org/maven2/com/caoccao/javet"
    static final String LIBRARY_HOME = AssetPipelineConfigHolder.config?.javetLibraryHome ?: TEMP_DIRECTORY

    final JavetLibLoader javetLibLoader

    NativeLibraryLoader(JSRuntimeType jsRuntimeType) {
        javetLibLoader = new JavetLibLoader(jsRuntimeType)
    }

    private URL getJavetJARUrl() {
        new URL("${BASE_URL}/${javetPackageName}/${JAVET_VERSION}/${getJavetFileName()}")
    }

    private String getJavetPackageName() {
        IS_MACOS ? "javet-macos" : "javet"
    }

    private String getJavetFileName() {
        "${javetPackageName}-${JAVET_VERSION}.jar"
    }

    /**
     * The native libraries are packed in the Javet platform specific jars. First we need to download the correct JAR
     * for this platform and then we will extract the native library from it and into a temporary directory for loading.
     *
     * @return the downloaded Javet jar file for this platform
     */
    private File downloadJavetJar() {
        File jarFile = new File(LIBRARY_HOME, javetFileName)
        if (jarFile.exists()) {
            log.debug("Jar file ${jarFile.path} exists, skipping download")
            return jarFile
        }

        // Download the file
        URL jarUrl = getJavetJARUrl()
        log.info("Downloading sass-dart native library integration: ${jarUrl}")

        copyToFile(jarUrl.openStream(), jarFile)

        log.debug("Downloaded $jarUrl to $jarFile (${jarFile.size()} bytes)")
        jarFile
    }

    private void copyToFile(InputStream inputStream, File targetFile) {
        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream)
        FileOutputStream fileOutputStream = new FileOutputStream(targetFile)
        FileChannel fileChannel = fileOutputStream.getChannel()
        fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
    }

    /**
     * A file reference to the platform and arch specific native library needed.
     * @return the required native library for v8 integration
     */
    File extractNativeLibrary() {
        File file = downloadJavetJar()
        if (!file.exists()) {
            throw new IllegalStateException("Javet jar file $file does not exist, perhaps the download failed")
        }

        String libraryName = javetLibLoader.libFileName
        File jniLibrary = new File(LIBRARY_HOME, libraryName)
        if (jniLibrary.exists()) {
            log.debug("Native library $libraryName already exists, skipping extract")
            return jniLibrary
        }

        // Extract from the JAR file, should be in the root
        JarFile jarFile = new JarFile(file)
        JarEntry jarEntry = jarFile.getJarEntry(libraryName)
        if (!jarEntry) {
            throw new IllegalStateException("Could not load native library: $libraryName")
        }

        copyToFile(jarFile.getInputStream(jarEntry), jniLibrary)

        log.debug("Extracted native library $libraryName to ${jniLibrary.path}")
        jniLibrary
    }
}
