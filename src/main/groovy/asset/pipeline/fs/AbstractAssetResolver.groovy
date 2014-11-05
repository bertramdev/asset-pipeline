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

package asset.pipeline.fs

import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import asset.pipeline.GenericAssetFile
import groovy.transform.CompileStatic

import java.util.jar.JarEntry
import java.util.regex.Pattern
import java.util.zip.ZipEntry

/**
* The abstract class for any helper methods in resolving files
 *
* @author David Estes
*/
abstract class AbstractAssetResolver<T> implements AssetResolver {
	String name

    AbstractAssetResolver(String name) {
        this.name = name
    }

    protected abstract String relativePathToResolver(T file, String scanDirectoryPath)

    protected abstract T getRelativeFile(String relativePath, String name)

    protected abstract Closure<InputStream> createInputStreamClosure(T file)


    protected AssetFile resolveAsset(specs, String prefixPath, String normalizedPath, AssetFile baseFile, String extension) {
        if (specs) {
            for (fileSpec in specs) {
                def fileName = normalizedPath
                if (fileName.endsWith(".${fileSpec.compiledExtension}")) {
                    fileName = fileName.substring(0, fileName.lastIndexOf(".${fileSpec.compiledExtension}"))
                }
                for (ext in fileSpec.extensions) {
                    def tmpFileName = fileName
                    if (!tmpFileName.endsWith("." + ext)) {
                        tmpFileName += "." + ext
                    }
                    def file = getRelativeFile(prefixPath, tmpFileName)
                    def inputStreamClosure = createInputStreamClosure(file)

                    if (inputStreamClosure) {
                        return fileSpec.newInstance(inputStreamSource: inputStreamClosure, baseFile: baseFile, path: relativePathToResolver(file, prefixPath), sourceResolver: this)
                    }
                }
            }
        } else {
            def fileName = normalizedPath
            if (extension) {
                if (!fileName.endsWith(".${extension}")) {
                    fileName += ".${extension}"
                }
            }
            def file = getRelativeFile(prefixPath, fileName)
            def inputStreamClosure = createInputStreamClosure(file)
            if (inputStreamClosure) {
                return new GenericAssetFile(inputStreamSource: inputStreamClosure, path: relativePathToResolver(file, prefixPath))
            }
        }
        return null
    }

    @CompileStatic
	public Pattern convertGlobToRegEx(String line)
	{
		line = line.trim();
		int strLen = line.length();
		StringBuilder sb = new StringBuilder(strLen);
		// Remove beginning and ending * globs because they're useless
		if (line.startsWith("*"))
		{
			line = line.substring(1);
			strLen--;
		}
		if (line.endsWith("*"))
		{
			line = line.substring(0, strLen-1);
			strLen--;
		}
		boolean escaping = false;
		int inCurlies = 0;
		for (char currentChar : line.toCharArray())
		{
			switch (currentChar)
			{
				case '*':
				if (escaping)
				sb.append("\\*");
				else
				sb.append(".*");
				escaping = false;
				break;
				case '?':
				if (escaping)
				sb.append("\\?");
				else
				sb.append('.');
				escaping = false;
				break;
				case '.':
				case '(':
				case ')':
				case '+':
				case '|':
				case '^':
				case '$':
				case '@':
				case '%':
				sb.append('\\');
				sb.append(currentChar);
				escaping = false;
				break;
				case '\\':
				if (escaping)
				{
					sb.append("\\\\");
					escaping = false;
				}
				else
				escaping = true;
				break;
				case '{':
				if (escaping)
				{
					sb.append("\\{");
				}
				else
				{
					sb.append('(');
					inCurlies++;
				}
				escaping = false;
				break;
				case '}':
				if (inCurlies > 0 && !escaping)
				{
					sb.append(')');
					inCurlies--;
				}
				else if (escaping)
				sb.append("\\}");
				else
				sb.append("}");
				escaping = false;
				break;
				case ',':
				if (inCurlies > 0 && !escaping)
				{
					sb.append('|');
				}
				else if (escaping)
				sb.append("\\,");
				else
				sb.append(",");
				break;
				default:
				escaping = false;
				sb.append(currentChar);
			}
		}
		return Pattern.compile(sb.toString());
	}

    protected AssetFile assetForFile(T file, String contentType, AssetFile baseFile=null, String sourceDirectory) {
        if(file == null) {
            return null
        }

        if(contentType == null) {
            return new GenericAssetFile(inputStreamSource: createInputStreamClosure(file), path: relativePathToResolver(file,sourceDirectory))
        }

        def possibleFileSpecs = AssetHelper.getPossibleFileSpecs(contentType)
        for(fileSpec in possibleFileSpecs) {
            for(extension in fileSpec.extensions) {
                def fileName = getFileName(file)
                if(fileName.endsWith(".$extension" )) {
                    return fileSpec.newInstance(inputStreamSource: createInputStreamClosure(file), baseFile: baseFile, path: relativePathToResolver(file,sourceDirectory), sourceResolver: this)
                }
            }
        }
        return new GenericAssetFile(inputStreamSource: createInputStreamClosure(file), path: relativePathToResolver(file,sourceDirectory))
    }

    protected abstract String getFileName(T file)

    @CompileStatic
	protected boolean isFileMatchingPatterns(String filePath, List<Pattern> patterns) {
		for(pattern in patterns) {
			if(filePath =~ pattern) {
				return true
			}
		}
		return false
	}

}
