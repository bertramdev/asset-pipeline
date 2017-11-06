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

package asset.pipeline.ratpack;

public class AssetAttributes {
	private Boolean gzipExists = false;
	private Boolean brExists = false;
	private boolean exists = false;
	private boolean isDirectory = false;
	private Long fileSize;
	private Long gzipFileSize;
	private Long brFileSize;

	public AssetAttributes(boolean exists, Boolean gzipExists, Boolean brExists, Boolean isDirectory, Long fileSize, Long gzipFileSize,Long brFileSize) {
		this.gzipExists = gzipExists;
		this.brExists = brExists;
		this.exists = exists;
		this.fileSize = fileSize;
		this.isDirectory = isDirectory;
		this.gzipFileSize = gzipFileSize;
		this.brFileSize = brFileSize;
	}

	public boolean exists() {
		return this.exists;
	}

	public boolean isDirectory() {
		return this.isDirectory;
	}

	public Boolean gzipExists() {
		return this.gzipExists;
	}

	public Boolean brExists() {
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