package asset.pipeline.micronaut;

import java.net.URL;
import java.util.Date;

public class AssetAttributes {
	private boolean gzipExists = false;
	private boolean exists = false;
	private URL resource;
	private URL gzipResource;
	private boolean isDirectory = false;
	private Long fileSize;
	private Long gzipFileSize;
	private Date lastModified;

	public AssetAttributes(boolean exists, Boolean gzipExists, Boolean isDirectory, Long fileSize, Long gzipFileSize, Date lastModified, URL resource, URL gzipResource) {
		this.gzipExists = gzipExists;
		this.exists = exists;
		this.fileSize = fileSize;
		this.isDirectory = isDirectory;
		this.gzipFileSize = gzipFileSize;
		this.resource = resource;
		this.lastModified = lastModified;
		this.gzipResource = gzipResource;
	}

	public Date getLastModified() {
		return this.lastModified;
	}

	public URL getResource() {
		return this.resource;
	}

	public URL getGzipResource() {
		return this.gzipResource;
	}

	public boolean exists() {
		return this.exists;
	}

	public boolean isDirectory() {
		return this.isDirectory;
	}

	public boolean gzipExists() {
		return this.gzipExists;
	}

	public Long getFileSize() {
		return this.fileSize;
	}

	public Long getGzipFileSize() {
		return this.gzipFileSize;
	}
}