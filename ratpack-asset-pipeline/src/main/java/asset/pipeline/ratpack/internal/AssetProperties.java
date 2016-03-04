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

package asset.pipeline.ratpack.internal;

import java.util.Date;

public class AssetProperties {
	private final String path;
	private final String indexedPath;
	private final String format;
	private final String encoding;
	private final Date lastModified;

	public AssetProperties(String path, String indexedPath, String format, String encoding, Date lastModified) {
		this.encoding = encoding;
		this.indexedPath = indexedPath;
		this.format = format;
		this.path = path;
		this.lastModified = lastModified;
	}

	public String getPath() {
		return path;
	}

	public String getIndexedPath() {
		return indexedPath;
	}

	public String getFormat() {
		return format;
	}

	public String getEncoding() {
		return encoding;
	}

	public Date getLastModified() { return lastModified; }
}
