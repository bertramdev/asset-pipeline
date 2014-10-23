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

import asset.pipeline.*
import java.util.regex.Pattern

/**
* The abstract class for any helper methods in resolving files
* @author David Estes
*/
abstract class AbstractAssetResolver implements AssetResolverInterface {
	String name

	public def getAsset(String relativePath, String contentType = null, String extension = null) {

	}

	public def getAssets(String basePath, String contentType = null, String extension = null,  Boolean recursive = true) {

	}

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

	protected isFileMatchingPatterns(filePath, patterns) {
		for(pattern in patterns) {
			if(filePath =~ pattern) {
				return true
			}
		}
		return false
	}

}
