package asset.pipeline

interface AssetFile {
    static contentType
    static List extensions
    static String compiledExtension
    static List processors



    AssetFile getBaseFile()
    String getEncoding()
    void setEncoding(String encoding)
    void setBaseFile(AssetFile baseFile)


    String processedStream(precompiler)

    String directiveForLine(String line)

}
