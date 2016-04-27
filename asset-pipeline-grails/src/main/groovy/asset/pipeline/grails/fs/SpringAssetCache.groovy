package asset.pipeline.grails.fs

class SpringAssetCache extends Thread {
	private SpringResourceAssetResolver resolver

	SpringAssetCache(SpringResourceAssetResolver resolver) {
		this.resolver = resolver
	}

	void run() {
		resolver.cacheAllResources()
	}
}
