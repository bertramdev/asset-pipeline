package asset.pipeline

import asset.pipeline.fs.AssetResolver

/**
 * Holder for Asset Pipeline's configuration
 * Also Provides Helper methods for loading in config from properties
 * @author David Estes
 */
class AssetPipelineConfigHolder {
    public static Collection<AssetResolver> resolvers = []
    public static Properties manifest
    public static Map config = [:]

    private static Integer configHashCode
    private static Integer resolverHashCode
    private static String digestString
    private static Object configMutexLock = new Object()

    public static registerResolver(AssetResolver resolver) {
        resolvers << resolver
    }

    public static Properties getManifest() {
        return this.manifest
    }

    public static void setManifest(Properties manifest) {
        this.manifest = manifest
    }

    public static Map getConfig() {
        return this.config
    }

    public static void setConfig(Map config) {
        synchronized(configMutexLock) {
            this.config = config;
        }
    }

    public static Collection<AssetResolver> getResolvers() {
        return this.resolvers;
    }

    public static void setResolvers(Collection<AssetResolver> resolvers) {
        this.resolvers = resolvers
    }

    public static String getDigestString() {
        synchronized(configMutexLock) {
            //check if the maps or arrays have changed to reset the digest
            if(resolvers?.hashCode() != resolverHashCode || config?.hashCode() != configHashCode) {
                digestString = AssetHelper.getByteDigest([config: config?.sort(),resolvers: resolvers?.collect{AssetResolver resolver -> resolver.name}?.sort()].sort().toString().bytes)
                resolverHashCode = resolvers?.hashCode()
                configHashCode = config?.hashCode()
            }
            return digestString
        }
        
    }
}
