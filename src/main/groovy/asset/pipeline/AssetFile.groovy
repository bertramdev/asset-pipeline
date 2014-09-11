/*
 * Copyright 2014 the original author or authors.
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

package asset.pipeline

import asset.pipeline.fs.AssetResolverInterface

/**
* Provides an interface for referencing files resolved by the Asset-Pipeline AssetResolvers
* @author David Estes
*/
interface AssetFile {
    static contentType
    static List extensions
    static String compiledExtension
    static List processors



    AssetFile getBaseFile()
    String getEncoding()
    void setEncoding(String encoding)
    void setBaseFile(AssetFile baseFile)

    InputStream getInputStream()
    public String getParentPath()
    public String getPath()
    public String getName()
    public AssetResolverInterface getSourceResolver()


    String processedStream(precompiler)

    String directiveForLine(String line)

}
