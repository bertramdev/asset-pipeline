/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package asset.pipeline.grails;

import org.springframework.core.io.Resource;
import java.util.Date;

public class AssetAttributes {
	private boolean gzipExists = false;
	private boolean brExists = false;
	private boolean exists = false;
	private Resource resource;
	private Resource gzipResource;
	private boolean isDirectory = false;
	private Long fileSize;
	private Long gzipFileSize;
	private Long brFileSize;
	private Resource brResource;
	private Date lastModified;

	public AssetAttributes(boolean exists, Boolean gzipExists, Boolean brExists, Boolean isDirectory, Long fileSize, Long gzipFileSize, Long brFileSize, Date lastModified, Resource resource, Resource gzipResource, Resource brResource) {
		this.gzipExists = gzipExists;
		this.brExists = brExists;
		this.exists = exists;
		this.fileSize = fileSize;
		this.isDirectory = isDirectory;
		this.gzipFileSize = gzipFileSize;
		this.brFileSize = brFileSize;
		this.resource = resource;
		this.lastModified = lastModified;
		this.gzipResource = gzipResource;
		this.brResource = brResource;
	}

	public Date getLastModified() {
		return this.lastModified;
	}

	public Resource getResource() {
		return this.resource;
	}

	public Resource getGzipResource() {
		return this.gzipResource;
	}

	public Resource getBrResource() {
		return this.brResource;
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

	public boolean brExists() {
		return this.brExists;
	}

	public Long getFileSize() {
		return this.fileSize;
	}

	public Long getGzipFileSize() {
		return this.gzipFileSize;
	}

	public Long getBrFileSize() {
		return this.brFileSize;
	}
}