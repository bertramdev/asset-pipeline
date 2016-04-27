package asset.pipeline.grails.fs

import org.springframework.core.io.Resource

class EmptyResource implements Resource {

	boolean exists() {
		return false
	}

	Resource createRelative(String relativePath) {}

	String getDescription() {}

	File getFile() {}

	String getFilename() {}

	URI getURI() {}

	URL getURL() {}

	InputStream getInputStream() {}

	boolean isOpen() {
		return false
	}
	boolean isReadable() {
		return false
	}

	long contentLength() {
		return 0
	}

	long lastModified() {
		return 0
	}
}
