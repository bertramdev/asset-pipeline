package asset.pipeline.dart

import asset.pipeline.AssetPipelineConfigHolder
import com.caoccao.javet.enums.JSRuntimeType
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.jar.JarEntry
import java.util.jar.JarFile

@Slf4j
@CompileStatic
class NativeLibraryUtil {
    static final String JAVET_VERSION = NativeLibraryUtil.class.classLoader.getResource("javet-version.txt").openStream().text
    static final String BASE_URL = AssetPipelineConfigHolder.config?.javetBaseUrl ?: "https://repo1.maven.org/maven2/com/caoccao/javet/javet-macos/${JAVET_VERSION}/"

    static final String TMP_DIR = System.getProperty("java.io.tmpdir")
    static final String OS_ARCH = System.getProperty("os.arch")
    static final String OS_NAME = System.getProperty("os.name")

    static final boolean IS_LINUX   = OS_NAME.startsWith("Linux")
    static final boolean IS_MACOS   = OS_NAME.startsWith("Mac OS")
    static final boolean IS_WINDOWS = OS_NAME.startsWith("Windows")
    static final boolean IS_ARM64   = OS_ARCH.startsWith("arm64") || OS_ARCH.startsWith("armv8") || OS_ARCH == "aarch64"
    static final boolean IS_X86_64  = OS_ARCH.matches(/^(x8664|amd64|ia32e|em64t|x64|x86_64)$/)

    static final String ARCH_NAME = IS_ARM64 ? 'arm64' : 'x86_64'

    static URL getJavetJARUrl() {
        new URL(BASE_URL + getJavetFileName())
    }

    static String getJavetFileName() {
        IS_MACOS ? "javet-macos-${JAVET_VERSION}.jar" : "javet-${JAVET_VERSION}.jar"
    }

    static String getLibraryName(JSRuntimeType runtimeType) {
        if (IS_MACOS) {
            return "libjavet-${runtimeType.name}-macos-${ARCH_NAME}.v.${JAVET_VERSION}.dylib"
        }
        if (IS_LINUX) {
            return "libjavet-${runtimeType.name}-linux-${ARCH_NAME}.v.${JAVET_VERSION}.so"
        }
        if (IS_WINDOWS) {
            return "libjavet-${runtimeType.name}-windows-${ARCH_NAME}.v.${JAVET_VERSION}.dll"
        }

        throw new IllegalStateException("Platform type not supported: ${OS_NAME} (${OS_ARCH}), expected Mac OS, Linux, or Windows")
    }

    static File downloadJavetJar(JSRuntimeType runtimeType) {
        File jarFile = new File(TMP_DIR, javetFileName)
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

    static File extractNativeLibrary(JSRuntimeType runtimeType) {
        File file = downloadJavetJar(runtimeType)
        if (!file.exists()) {
            throw new IllegalStateException("Javet jar file $file does not exist, perhaps the download failed")
        }

        String libraryName = getLibraryName(runtimeType)
        File jniLibrary = new File(TMP_DIR, libraryName)
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

    static private void copyToFile(InputStream inputStream, File targetFile) {
        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream)
        FileOutputStream fileOutputStream = new FileOutputStream(targetFile)
        FileChannel fileChannel = fileOutputStream.getChannel()
        fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
    }
}
