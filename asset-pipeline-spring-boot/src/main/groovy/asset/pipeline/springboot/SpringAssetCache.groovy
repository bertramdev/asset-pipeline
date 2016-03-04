package asset.pipeline.springboot

class SpringAssetCache extends Thread {
	private SpringResourceAssetResolver resolver

	SpringAssetCache(SpringResourceAssetResolver resolver) {
		this.resolver = resolver
	}

	void run() {
		resolver.cacheAllResources()
	}
}
